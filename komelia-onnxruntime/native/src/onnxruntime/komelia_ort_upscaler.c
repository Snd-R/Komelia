#include "komelia_ort_upscaler.h"

#include "komelia_error.h"
#include "komelia_matrix_ops.h"

static int tile_threshold = 512 * 512;

static void copy_animation_metadata(
    VipsImage *input,
    VipsImage *output
) {
    int input_height = vips_image_get_height(input);
    int input_page_height = vips_image_get_page_height(input);
    int input_pages_loaded = input_height / input_page_height;

    int out_page_height = vips_image_get_height(output) / input_pages_loaded;

    vips_image_set_int(output, VIPS_META_N_PAGES, input_pages_loaded);
    vips_image_set_int(output, VIPS_META_PAGE_HEIGHT, out_page_height);
    if (vips_image_get_typeof(input, "delay") == VIPS_TYPE_ARRAY_INT) {
        int out_delays_size = 0;
        int *out_delays = nullptr;
        vips_image_get_array_int(input, "delay", &out_delays, &out_delays_size);
        vips_image_set_array_int(output, "delay", out_delays, out_delays_size);
    }
}

static void hwc_to_chw(
    const uint8_t *input,
    size_t height,
    size_t width,
    size_t channels,
    float *output
) {
    size_t stride_size = height * width;
#pragma omp parallel for shared(stride_size, channels, input, output) default(none) collapse(2)
    for (size_t i = 0; i != stride_size; ++i) {
        for (size_t c = 0; c != channels; ++c) {
            output[c * stride_size + i] = (float)input[i * channels + c] / 255.0f;
        }
    }
}

static void hwc_to_chw_f16(
    const uint8_t *input,
    size_t height,
    size_t width,
    size_t channels,
    _Float16 *output
) {
    size_t stride_size = height * width;
#pragma omp parallel for shared(stride_size, channels, input, output) default(none) collapse(2)
    for (size_t i = 0; i != stride_size; ++i) {
        for (size_t c = 0; c != channels; ++c) {
            output[c * stride_size + i] = (_Float16)input[i * channels + c] / 255.0f16;
        }
    }
}

static KomeliaOrtInputTensor *create_tensor(
    ONNXTensorElementDataType data_type,
    VipsImage *input_image
) {
    KomeliaOrtInputTensor *tensor = malloc(sizeof(KomeliaOrtInputTensor));
    int input_height = vips_image_get_height(input_image);
    int input_width = vips_image_get_width(input_image);

    int64_t *tensor_shape = malloc(sizeof(int64_t) * 4);
    tensor_shape[0] = 1;
    tensor_shape[1] = 3;
    tensor_shape[2] = input_height;
    tensor_shape[3] = input_width;
    const size_t tensor_shape_len = 4;
    // const size_t tensor_shape_len = sizeof(tensor_shape) / sizeof(tensor_shape[0]);
    const size_t tensor_input_ele_count = input_height * input_width * 3;
    unsigned char *image_input_data = (unsigned char *)vips_image_get_data(input_image);

    size_t tensor_data_len;
    void *tensor_data;
    if (data_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT) {
        tensor_data_len = tensor_input_ele_count * sizeof(float);
        tensor_data = malloc(tensor_data_len);
        hwc_to_chw(image_input_data, input_height, input_width, 3, tensor_data);
    } else {
        tensor_data_len = tensor_input_ele_count * sizeof(_Float16);
        tensor_data = malloc(tensor_data_len);
        hwc_to_chw_f16(image_input_data, input_height, input_width, 3, tensor_data);
    }
    tensor->data = tensor_data;
    tensor->data_len = tensor_data_len;
    tensor->shape = tensor_shape;
    tensor->shape_len = tensor_shape_len;
    return tensor;

    // return ort_api->CreateTensorWithDataAsOrtValue(memory_info, *tensor_data, tensor_data_len,
    //                                                tensor_shape, tensor_shape_len,
    //                                                ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT, tensor);
}

static VipsImage *get_image_from_tensor(
    KomeliaOrtUpscaler *upscaler,
    InferenceResult *result,
    GError **error
) {
    const OrtApi *ort_api = upscaler->komelia_ort->ort_api;

    size_t dim_length;
    OrtStatus *ort_status = ort_api->GetDimensionsCount(result->out_tensors_info[0], &dim_length);
    if (ort_status != nullptr) {
        wrap_ort_error(
            upscaler->komelia_ort->ort_api,
            ort_status,
            KOMELIA_ORT_ERROR_INFERENCE,
            error
        );
        return nullptr;
    }
    if (dim_length != 4) {
        g_set_error_literal(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_INFERENCE,
            "Unexpect number of output dimensions"
        );
        return nullptr;
    }

    int64_t dim_values[dim_length];
    ort_status = ort_api->GetDimensions(result->out_tensors_info[0], dim_values, dim_length);
    if (ort_status != nullptr) {
        wrap_ort_error(
            upscaler->komelia_ort->ort_api,
            ort_status,
            KOMELIA_ORT_ERROR_INFERENCE,
            error
        );
        return nullptr;
    }
    int output_width = (int)dim_values[dim_length - 1];
    int output_height = (int)dim_values[dim_length - 2];
    int output_size = output_height * output_width * 3;

    void *output_tensor_data = nullptr;
    ort_status = ort_api->GetTensorMutableData(result->output_tensors[0], &output_tensor_data);
    if (ort_status != nullptr) {
        wrap_ort_error(
            upscaler->komelia_ort->ort_api,
            ort_status,
            KOMELIA_ORT_ERROR_INFERENCE,
            error
        );
        return nullptr;
    }

    ONNXTensorElementDataType output_element_type;
    ort_status = ort_api->GetTensorElementType(result->out_tensors_info[0], &output_element_type);
    if (ort_status != nullptr) {
        wrap_ort_error(
            upscaler->komelia_ort->ort_api,
            ort_status,
            KOMELIA_ORT_ERROR_INFERENCE,
            error
        );
        return nullptr;
    }

    uint8_t *output_image_data = malloc(output_size);
    if (output_element_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT) {
        komelia_chw_to_hwc(output_tensor_data, output_height, output_width, 3, output_image_data);
    } else {
        komelia_chw_to_hwc_f16(
            output_tensor_data,
            output_height,
            output_width,
            3,
            output_image_data
        );
    }

    VipsImage *inferred_image = vips_image_new_from_memory_copy(
        output_image_data,
        output_size,
        output_width,
        output_height,
        3,
        VIPS_FORMAT_UCHAR
    );
    free(output_image_data);

    if (inferred_image == nullptr) {
        g_set_error_literal(error, KOMELIA_ORT_ERROR, KOMELIA_ORT_ERROR_VIPS, vips_error_buffer());
        vips_error_clear();
        return nullptr;
    }

    return inferred_image;
}

static VipsImage *upscale_tile(
    KomeliaOrtUpscaler *upscaler,
    VipsImage *input_image,
    VipsRect *region_rect,
    GError **error
) {

    int image_bands = vips_image_get_bands(input_image);
    VipsRegion *region = vips_region_new(input_image);
    int prepare_error = vips_region_prepare(region, region_rect);
    if (prepare_error) {
        g_object_unref(region);
        g_set_error_literal(error, KOMELIA_ORT_ERROR, KOMELIA_ORT_ERROR_VIPS, vips_error_buffer());
        vips_error_clear();
        return nullptr;
    }

    size_t region_size = 0;
    VipsPel *region_data = vips_region_fetch(
        region,
        region_rect->left,
        region_rect->top,
        region_rect->width,
        region_rect->height,
        &region_size
    );
    VipsImage *unformatted_image = vips_image_new_from_memory(
        region_data,
        region_size,
        region_rect->width,
        region_rect->height,
        image_bands,
        VIPS_FORMAT_UCHAR
    );
    if (!unformatted_image) {
        g_object_unref(region);
        g_free(region_data);
        g_set_error_literal(error, KOMELIA_ORT_ERROR, KOMELIA_ORT_ERROR_VIPS, vips_error_buffer());
        vips_error_clear();
        return nullptr;
    }

    VipsImage *formatted_region_image = nullptr;
    vips_copy(
        unformatted_image,
        &formatted_region_image,
        "bands",
        vips_image_get_bands(input_image),
        "format",
        vips_image_get_format(input_image),
        "coding",
        vips_image_get_coding(input_image),
        "interpretation",
        vips_image_get_interpretation(input_image),
        nullptr
    );

    if (!formatted_region_image) {
        g_object_unref(region);
        g_object_unref(unformatted_image);
        g_free(region_data);
        g_set_error_literal(error, KOMELIA_ORT_ERROR, KOMELIA_ORT_ERROR_VIPS, vips_error_buffer());
        vips_error_clear();
        return nullptr;
    }

    KomeliaOrtInputTensor *input_tensor =
        create_tensor(upscaler->session->input_data_type, formatted_region_image);
    GError *inference_error = nullptr;
    InferenceResult *inference_result = komelia_ort_run_inference(
        upscaler->komelia_ort,
        upscaler->session,
        input_tensor,
        &inference_error
    );
    free(input_tensor->data);
    free(input_tensor->shape);
    free(input_tensor);

    if (inference_error != nullptr) {
        g_object_unref(region);
        g_object_unref(unformatted_image);
        g_object_unref(formatted_region_image);
        g_free(region_data);
        g_propagate_error(error, inference_error);
        return nullptr;
    }

    GError *image_tensor_error = nullptr;
    VipsImage *upscaled_image =
        get_image_from_tensor(upscaler, inference_result, &image_tensor_error);

    if (image_tensor_error != nullptr) {
        g_propagate_error(error, image_tensor_error);
    }
    g_object_unref(region);
    g_object_unref(unformatted_image);
    g_object_unref(formatted_region_image);
    g_free(region_data);
    komelia_ort_release_inference_result(upscaler->komelia_ort, inference_result);

    return upscaled_image;
}

static VipsImage *do_tiled_inference(
    KomeliaOrtUpscaler *upscaler,
    VipsImage *input_image,
    GError **error
) {
    int tile_size = upscaler->tile_size;
    int image_width = vips_image_get_width(input_image);
    int image_height = vips_image_get_height(input_image);

    int row_tiles = ceil((double)image_width / tile_size);
    int column_tiles = ceil((double)image_height / tile_size);

    int tile_count = 0;
    VipsImage *upscaled_tiles[row_tiles * column_tiles];

    int y_taken = 0;
    while (y_taken != image_height) {
        int x_taken = 0;
        int tile_height = y_taken + tile_size < image_height ? tile_size : image_height - y_taken;
        while (x_taken != image_width) {
            GError *tile_upscale_error = nullptr;

            VipsRect region_rect;
            region_rect.top = y_taken;
            region_rect.left = x_taken;
            region_rect.height = tile_height;
            region_rect.width =
                x_taken + tile_size < image_width ? tile_size : image_width - x_taken;
            VipsImage *upscaled_image =
                upscale_tile(upscaler, input_image, &region_rect, &tile_upscale_error);

            if (tile_upscale_error != nullptr) {
                for (int i = 0; i < tile_count; ++i) {
                    g_object_unref(upscaled_tiles[i]);
                }
                g_propagate_error(error, tile_upscale_error);
                return nullptr;
            }

            upscaled_tiles[tile_count] = upscaled_image;

            x_taken = x_taken + tile_size;
            if (x_taken > image_width)
                x_taken = image_width;
            ++tile_count;
        }

        y_taken = y_taken + tile_size;
        if (y_taken > image_height)
            y_taken = image_height;
    }

    int dst_width = 0;
    int dst_height = 0;
    for (int i = 0; i < row_tiles; ++i) {
        dst_width += vips_image_get_width(upscaled_tiles[i]);
    }
    for (int i = 0; i < column_tiles; ++i) {
        dst_height += vips_image_get_height(upscaled_tiles[i * row_tiles]);
    }

    VipsImage *joined = nullptr;
    vips_arrayjoin(upscaled_tiles, &joined, tile_count, "across", row_tiles, nullptr);
    for (int i = 0; i < tile_count; ++i) {
        g_object_unref(upscaled_tiles[i]);
    }
    if (joined == nullptr) {
        g_set_error_literal(error, KOMELIA_ORT_ERROR, KOMELIA_ORT_ERROR_VIPS, vips_error_buffer());
        vips_error_clear();
        return nullptr;
    }
    int joined_width = vips_image_get_width(joined);
    int joined_height = vips_image_get_height(joined);

    if (joined_width != dst_width || joined_height != dst_height) {
        VipsImage *cropped = nullptr;
        vips_crop(joined, &cropped, 0, 0, dst_width, dst_height, nullptr);
        g_object_unref(joined);

        if (cropped == nullptr) {
            g_set_error_literal(
                error,
                KOMELIA_ORT_ERROR,
                KOMELIA_ORT_ERROR_VIPS,
                vips_error_buffer()
            );
            vips_error_clear();
            return nullptr;
        }

        copy_animation_metadata(input_image, cropped);
        return cropped;
    }

    copy_animation_metadata(input_image, joined);
    return joined;
}

static VipsImage *do_full_image_inference(
    KomeliaOrtUpscaler *upscaler,
    VipsImage *input_image,
    GError **error
) {
    KomeliaOrtInputTensor *input_tensor =
        create_tensor(upscaler->session->input_data_type, input_image);
    GError *inference_error = nullptr;
    InferenceResult *inference_result = komelia_ort_run_inference(
        upscaler->komelia_ort,
        upscaler->session,
        input_tensor,
        &inference_error
    );

    free(input_tensor->data);
    free(input_tensor->shape);
    free(input_tensor);

    if (inference_error != nullptr) {
        g_propagate_error(error, inference_error);
        return nullptr;
    }

    GError *out_tensor_error = nullptr;
    VipsImage *tensor_image = get_image_from_tensor(upscaler, inference_result, &out_tensor_error);
    if (out_tensor_error != nullptr) {
        g_propagate_error(error, inference_error);
        komelia_ort_release_inference_result(upscaler->komelia_ort, inference_result);
        return nullptr;
    }

    komelia_ort_release_inference_result(upscaler->komelia_ort, inference_result);
    return tensor_image;
}

static VipsImage *preprocess_for_inference(
    VipsImage *input_image,
    GError **error
) {
    const VipsInterpretation interpretation = vips_image_get_interpretation(input_image);
    const int input_bands = vips_image_get_bands(input_image);
    const int width = vips_image_get_width(input_image);
    const int height = vips_image_get_height(input_image);
    VipsImage *transformed = nullptr;

    if (interpretation != VIPS_INTERPRETATION_sRGB) {
        const int vips_error =
            vips_colourspace(input_image, &transformed, VIPS_INTERPRETATION_sRGB, nullptr);
        if (vips_error) {
            g_set_error_literal(
                error,
                KOMELIA_ORT_ERROR,
                KOMELIA_ORT_ERROR_VIPS,
                vips_error_buffer()
            );
            vips_error_clear();
            return nullptr;
        }
    }

    if (input_bands == 4) {
        VipsImage *without_alpha = nullptr;
        int vips_error;
        if (transformed != nullptr) {
            vips_error = vips_flatten(transformed, &without_alpha, nullptr);
            g_object_unref(transformed);
        } else {
            vips_error = vips_flatten(input_image, &without_alpha, nullptr);
        }

        if (vips_error) {
            g_set_error_literal(
                error,
                KOMELIA_ORT_ERROR,
                KOMELIA_ORT_ERROR_VIPS,
                vips_error_buffer()
            );
            vips_error_clear();
            return nullptr;
        }
        transformed = without_alpha;
    }

    int pad_width = 0;
    int pad_height = 0;
    if (width % 2 != 0) {
        pad_width = 1;
    }
    if (height % 2 != 0) {
        pad_height = 1;
    }

    if (pad_width || pad_height) {
        VipsImage *extended = nullptr;
        int vips_error;
        if (transformed != nullptr) {
            vips_error = vips_gravity(
                transformed,
                &extended,
                VIPS_COMPASS_DIRECTION_WEST,
                width + pad_width,
                height + pad_height,
                "extend",
                VIPS_EXTEND_BLACK,
                nullptr
            );
            g_object_unref(transformed);
        } else {
            vips_error = vips_gravity(
                input_image,
                &extended,
                VIPS_COMPASS_DIRECTION_WEST,
                width + pad_width,
                height + pad_height,
                "extend",
                VIPS_EXTEND_BLACK,
                nullptr
            );
        }
        if (vips_error) {
            g_set_error_literal(
                error,
                KOMELIA_ORT_ERROR,
                KOMELIA_ORT_ERROR_VIPS,
                vips_error_buffer()
            );
            vips_error_clear();
            return nullptr;
        }
        transformed = extended;
    }

    if (transformed == nullptr) {
        g_object_ref(input_image);
        transformed = input_image;
    }
    return transformed;
}

KomeliaOrtUpscaler *komelia_ort_upscaler_create(KomeliaOrt *ort) {
    KomeliaOrtUpscaler *upscaler = malloc(sizeof(KomeliaOrtUpscaler));
    upscaler->komelia_ort = ort;
    upscaler->execution_provider = CPU;
    upscaler->device_id = 0;
    upscaler->model_path = nullptr;
    upscaler->session = nullptr;
    upscaler->tile_size = 0;
    pthread_mutex_init(&upscaler->mutex, nullptr);
    return upscaler;
}

void komelia_ort_upscaler_destroy(KomeliaOrtUpscaler *upscaler) {
    free(upscaler->model_path);
    pthread_mutex_destroy(&upscaler->mutex);
    if (upscaler->session != nullptr) {
        komelia_ort_close_session(upscaler->komelia_ort, upscaler->session);
    }
    free(upscaler);
}

void komelia_ort_upscaler_set_tile_size(
    KomeliaOrtUpscaler *upscaler,
    const int size
) {
    pthread_mutex_lock(&upscaler->mutex);
    if (upscaler->tile_size != size) {
        upscaler->tile_size = size;
        if (upscaler->session != nullptr) {
            komelia_ort_close_session(upscaler->komelia_ort, upscaler->session);
            upscaler->session = nullptr;
        }
    }
    pthread_mutex_unlock(&upscaler->mutex);
}

void komelia_ort_upscaler_set_model_path(
    KomeliaOrtUpscaler *upscaler,
    const char *path
) {
    pthread_mutex_lock(&upscaler->mutex);
    if (upscaler->model_path != nullptr && strcmp(upscaler->model_path, path) == 0) {
        pthread_mutex_unlock(&upscaler->mutex);
        return;
    }

    char *model_path_copy = strdup(path);
    if (upscaler->model_path != nullptr) {
        free(upscaler->model_path);
    }
    upscaler->model_path = model_path_copy;

    if (upscaler->session != nullptr) {
        komelia_ort_close_session(upscaler->komelia_ort, upscaler->session);
        upscaler->session = nullptr;
    }

    pthread_mutex_unlock(&upscaler->mutex);
}

void komelia_ort_upscaler_set_execution_provider(
    KomeliaOrtUpscaler *upscaler,
    KomeliaOrtExecutionProvider execution_provider,
    int device_id
) {
    pthread_mutex_lock(&upscaler->mutex);
    if (upscaler->execution_provider != execution_provider || upscaler->device_id != device_id) {
        upscaler->execution_provider = execution_provider;
        upscaler->device_id = device_id;
        if (upscaler->session != nullptr) {
            komelia_ort_close_session(upscaler->komelia_ort, upscaler->session);
            upscaler->session = nullptr;
        }
    }

    pthread_mutex_unlock(&upscaler->mutex);
}

void komelia_ort_upscaler_close_session(KomeliaOrtUpscaler *upscaler) {
    pthread_mutex_lock(&upscaler->mutex);
    komelia_ort_close_session(upscaler->komelia_ort, upscaler->session);
    upscaler->session = nullptr;
    pthread_mutex_unlock(&upscaler->mutex);
}

VipsImage *komelia_ort_upscale(
    KomeliaOrtUpscaler *upscaler,
    VipsImage *image,
    GError **error
) {
    pthread_mutex_lock(&upscaler->mutex);
    if (upscaler->model_path == nullptr) {
        g_set_error_literal(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_INFERENCE,
            "model path is not initialized"
        );
        return nullptr;
    }

    if (upscaler->session == nullptr) {
        GError *session_init_error = nullptr;
        SessionData *session = komelia_ort_create_session(
            upscaler->komelia_ort,
            upscaler->execution_provider,
            upscaler->device_id,
            upscaler->model_path,
            &session_init_error
        );
        if (session_init_error != nullptr) {
            g_propagate_error(error, session_init_error);
            return nullptr;
        }
        upscaler->session = session;
    }

    GError *preprocessing_error = nullptr;
    VipsImage *preprocessed_image = preprocess_for_inference(image, &preprocessing_error);
    if (preprocessing_error != nullptr) {
        g_propagate_error(error, preprocessing_error);
        pthread_mutex_unlock(&upscaler->mutex);
        return nullptr;
    }

    const int input_width = vips_image_get_width(image);
    const int input_height = vips_image_get_height(image);
    VipsImage *upscaled_image;
    GError *upscale_error = nullptr;
    if (upscaler->tile_size != 0 && input_width * input_height > tile_threshold) {
        upscaled_image = do_tiled_inference(upscaler, preprocessed_image, &upscale_error);
    } else {
        upscaled_image = do_full_image_inference(upscaler, preprocessed_image, &upscale_error);
    }

    if (upscale_error != nullptr) {
        g_propagate_error(error, upscale_error);
        pthread_mutex_unlock(&upscaler->mutex);
        return nullptr;
    }

    pthread_mutex_unlock(&upscaler->mutex);
    return upscaled_image;
}