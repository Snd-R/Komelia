#include "komelia_ort_rf_detr.h"
#include "komelia_matrix_ops.h"
#include <math.h>

static float MEANS[3] = {0.485f, 0.456f, 0.406f};
static float STDS[3] = {0.229f, 0.224f, 0.225f};
static float confidence_threshold = 0.5f;

static void hwc_to_chw(
    const uint8_t *input,
    size_t height,
    size_t width,
    /*,size_t channels,*/
    float *output
) {
    size_t stride_size = height * width;
#pragma omp parallel for shared(stride_size, input, output, MEANS, STDS) default(none) collapse(2)
    for (size_t i = 0; i != stride_size; ++i) {
        for (size_t c = 0; c != 3; ++c) {
            const float value = input[i * 3 + c];
            const float normalized = ((value / 255.0f) - MEANS[c]) / STDS[c];
            output[c * stride_size + i] = normalized;
        }
    }
}

static void hwc_to_chw_f16(
    const uint8_t *input,
    size_t height,
    size_t width,
    /*,size_t channels,*/
    _Float16 *output
) {
    size_t stride_size = height * width;
#pragma omp parallel for shared(stride_size, input, output, MEANS, STDS) default(none) collapse(2)
    for (size_t i = 0; i != stride_size; ++i) {
        for (size_t c = 0; c != 3; ++c) {
            const float value = input[i * 3 + c];
            const float normalized = ((value / 255.0f) - MEANS[c]) / STDS[c];
            output[c * stride_size + i] = (_Float16)normalized;
        }
    }
}

static KomeliaOrtInputTensor *create_tensor(
    VipsImage *input_image,
    int resize_width,
    int resize_height,
    ONNXTensorElementDataType data_type,
    GError **error
) {
    VipsImage *transformed = input_image;
    g_object_ref(transformed);

    const VipsInterpretation interpretation = vips_image_get_interpretation(input_image);
    const int input_width = vips_image_get_width(input_image);
    const int input_height = vips_image_get_height(input_image);
    const int input_bands = vips_image_get_bands(input_image);

    // convert to rgba image
    if (interpretation != VIPS_INTERPRETATION_sRGB) {
        VipsImage *srgb_image = nullptr;
        int vips_error =
            vips_colourspace(input_image, &srgb_image, VIPS_INTERPRETATION_sRGB, nullptr);
        g_object_unref(transformed);

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
        transformed = srgb_image;
    }

    // remove alpha
    if (input_bands == 4) {
        VipsImage *without_alpha = nullptr;
        int vips_error = vips_flatten(transformed, &without_alpha, nullptr);
        g_object_unref(transformed);

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

    if (input_width != resize_width || input_height != resize_height) {
        VipsImage *resized = nullptr;
        // double hshrink = (double)input_width / resize_width;
        // double vshrink = (double)input_height / resize_height;
        // int resize_error = vips_shrink(transformed, &resized, hshrink, vshrink, nullptr);
        int resize_error = vips_thumbnail_image(
            transformed,
            &resized,
            resize_width,
            "height",
            resize_height,
            "size",
            VIPS_SIZE_FORCE,
            nullptr
        );
        g_object_unref(transformed);
        if (resize_error) {
            g_set_error_literal(
                error,
                KOMELIA_ORT_ERROR,
                KOMELIA_ORT_ERROR_VIPS,
                vips_error_buffer()
            );
            vips_error_clear();
            return nullptr;
        }
        transformed = resized;
    }

    KomeliaOrtInputTensor *tensor = malloc(sizeof(KomeliaOrtInputTensor));
    int64_t *tensor_shape = malloc(sizeof(int64_t) * 4);
    tensor_shape[0] = 1;
    tensor_shape[1] = 3;
    tensor_shape[2] = resize_width;
    tensor_shape[3] = resize_height;
    const size_t tensor_shape_len = 4;
    const size_t tensor_input_ele_count = resize_height * resize_width * 3;
    unsigned char *image_input_data = (unsigned char *)vips_image_get_data(transformed);

    size_t tensor_data_len;
    void *tensor_data;
    if (data_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT) {
        tensor_data_len = tensor_input_ele_count * sizeof(float);
        tensor_data = malloc(tensor_data_len);
        hwc_to_chw(image_input_data, resize_height, resize_width, tensor_data);
    } else {
        tensor_data_len = tensor_input_ele_count * sizeof(_Float16);
        tensor_data = malloc(tensor_data_len);
        hwc_to_chw_f16(image_input_data, resize_height, resize_width, tensor_data);
    }
    g_object_unref(transformed);

    tensor->data = tensor_data;
    tensor->data_len = tensor_data_len;
    tensor->shape = tensor_shape;
    tensor->shape_len = tensor_shape_len;
    return tensor;
}

KomeliaRfDetr *komelia_ort_rfdetr_create(KomeliaOrt *ort) {
    KomeliaRfDetr *rf_detr = malloc(sizeof(KomeliaRfDetr));
    rf_detr->komelia_ort = ort;
    rf_detr->execution_provider = CPU;
    rf_detr->device_id = 0;
    rf_detr->model_path = nullptr;
    rf_detr->session = nullptr;
    pthread_mutex_init(&rf_detr->mutex, nullptr);
    return rf_detr;
}

void komelia_ort_rfdetr_destroy(KomeliaRfDetr *rf_detr) {
    free(rf_detr->model_path);
    pthread_mutex_destroy(&rf_detr->mutex);
    if (rf_detr->session != nullptr) {
        komelia_ort_close_session(rf_detr->komelia_ort, rf_detr->session);
    }
    free(rf_detr);
}

void komelia_ort_rfdetr_set_model_path(
    KomeliaRfDetr *rf_detr,
    const char *path
) {
    pthread_mutex_lock(&rf_detr->mutex);
    if (rf_detr->model_path != nullptr && strcmp(rf_detr->model_path, path) == 0) {
        pthread_mutex_unlock(&rf_detr->mutex);
        return;
    }

    char *model_path_copy = strdup(path);
    if (rf_detr->model_path != nullptr) {
        free(rf_detr->model_path);
    }
    rf_detr->model_path = model_path_copy;

    if (rf_detr->session != nullptr) {
        komelia_ort_close_session(rf_detr->komelia_ort, rf_detr->session);
        rf_detr->session = nullptr;
    }

    pthread_mutex_unlock(&rf_detr->mutex);
}
void komelia_ort_rfdetr_set_execution_provider(
    KomeliaRfDetr *rf_detr,
    KomeliaOrtExecutionProvider execution_provider,
    int device_id
) {
    pthread_mutex_lock(&rf_detr->mutex);
    if (rf_detr->execution_provider != execution_provider || rf_detr->device_id != device_id) {
        rf_detr->execution_provider = execution_provider;
        rf_detr->device_id = device_id;
        if (rf_detr->session != nullptr) {
            komelia_ort_close_session(rf_detr->komelia_ort, rf_detr->session);
            rf_detr->session = nullptr;
        }
    }

    pthread_mutex_unlock(&rf_detr->mutex);
}

static void sigmoid(
    const float *input,
    size_t input_len,
    float *output
) {
    for (int i = 0; i < input_len; ++i) {
        output[i] = 1.0f / (1.0f + expf(-input[i]));
    }
}

static void get_output_data(
    KomeliaRfDetr *rf_detr,
    InferenceResult *inference_result,
    float **pred_boxes,
    size_t *pred_boxes_len,
    float **pred_logits,
    size_t *pred_logits_len,
    GError **error
) {
    if (inference_result->output_len != 2) {
        g_set_error_literal(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_INFERENCE,
            "invalid output count"
        );
    }

    float *boxes_data = nullptr;
    float *logits_data = nullptr;
    const OrtApi *ort_api = rf_detr->komelia_ort->ort_api;

    size_t pred_boxes_dim_len;
    size_t pred_logits_dim_len;
    OrtStatus *ort_status =
        ort_api->GetDimensionsCount(inference_result->out_tensors_info[0], &pred_boxes_dim_len);
    if (ort_status != nullptr)
        goto on_error;
    ort_status =
        ort_api->GetDimensionsCount(inference_result->out_tensors_info[1], &pred_logits_dim_len);
    if (ort_status != nullptr)
        goto on_error;

    if (pred_boxes_dim_len != 3 || pred_logits_dim_len != 3) {
        g_set_error_literal(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_INFERENCE,
            "invalid output tensor shape"
        );
        return;
    }

    int64_t pred_boxes_dims[3];
    int64_t pred_logits_dims[3];
    ort_status = ort_api->GetDimensions(inference_result->out_tensors_info[0], pred_boxes_dims, 3);
    if (ort_status != nullptr)
        goto on_error;
    ort_status = ort_api->GetDimensions(inference_result->out_tensors_info[1], pred_logits_dims, 3);
    if (ort_status != nullptr)
        goto on_error;

    size_t pred_boxes_data_size = pred_boxes_dims[0] * pred_boxes_dims[1] * pred_boxes_dims[2];
    size_t pred_logits_data_size = pred_logits_dims[0] * pred_logits_dims[1] * pred_logits_dims[2];
    void *pred_boxes_tensor = nullptr;
    void *pred_logits_tensor = nullptr;

    ort_status =
        ort_api->GetTensorMutableData(inference_result->output_tensors[0], &pred_boxes_tensor);
    if (ort_status != nullptr)
        goto on_error;
    ort_status =
        ort_api->GetTensorMutableData(inference_result->output_tensors[1], &pred_logits_tensor);
    if (ort_status != nullptr)
        goto on_error;

    boxes_data = malloc(sizeof(float) * pred_boxes_data_size);
    logits_data = malloc(sizeof(float) * pred_logits_data_size);

    if (rf_detr->session->input_data_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT) {
        memcpy(boxes_data, pred_boxes_tensor, sizeof(float) * pred_boxes_data_size);
        memcpy(logits_data, pred_logits_tensor, sizeof(float) * pred_logits_data_size);
    } else if (rf_detr->session->input_data_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16) {
        // convert f16 tensor data to f32
        const _Float16 *box_data_f16 = pred_boxes_tensor;
        for (size_t i = 0; i < pred_boxes_data_size; ++i) {
            boxes_data[i] = (float)box_data_f16[i];
        }
        const _Float16 *logits_data_f16 = pred_logits_tensor;
        for (size_t i = 0; i < pred_logits_data_size; ++i) {
            logits_data[i] = (float)logits_data_f16[i];
        }
    } else {
        g_set_error(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_INFERENCE,
            "invalid output tensor format %i, only float16 and float32 types are support",
            rf_detr->session->input_data_type
        );
    }
    *pred_boxes = boxes_data;
    *pred_boxes_len = pred_boxes_data_size;
    *pred_logits = logits_data;
    *pred_logits_len = pred_logits_data_size;
    return;
on_error:
    wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_INFERENCE, error);
    if (boxes_data != nullptr)
        free(boxes_data);
    if (logits_data != nullptr)
        free(logits_data);
}

struct ValueIndexPair {
    float value;
    int index;
};

struct BBox {
    float x;
    float y;
    float width;
    float height;
};

static int comparePairs(
    const void *a,
    const void *b
) {
    struct ValueIndexPair *pair1 = *(struct ValueIndexPair **)a;
    struct ValueIndexPair *pair2 = *(struct ValueIndexPair **)b;

    if (pair1->value > pair2->value) {
        return -1;
    }
    if (pair1->value < pair2->value) {
        return 1;
    }
    return 0;
}

static KomeliaRfDetrResults *get_results_from_tensor(
    KomeliaRfDetr *rf_detr,
    InferenceResult *inference_result,
    int original_width,
    int original_height,
    GError **error
) {
    const OrtApi *ort_api = rf_detr->komelia_ort->ort_api;
    float *boxes_data = nullptr;
    size_t boxes_data_len;
    float *logits_data = nullptr;
    size_t logits_data_len;
    GError *output_error = nullptr;
    get_output_data(
        rf_detr,
        inference_result,
        &boxes_data,
        &boxes_data_len,
        &logits_data,
        &logits_data_len,
        &output_error
    );
    if (output_error != nullptr) {
        g_propagate_error(error, output_error);
        return nullptr;
    }
    int64_t pred_boxes_dims[3];
    int64_t pred_logits_dims[3];
    OrtStatus *ort_status =
        ort_api->GetDimensions(inference_result->out_tensors_info[0], pred_boxes_dims, 3);
    if (ort_status != nullptr)
        goto on_error;
    ort_status = ort_api->GetDimensions(inference_result->out_tensors_info[1], pred_logits_dims, 3);
    if (ort_status != nullptr)
        goto on_error;

    sigmoid(logits_data, logits_data_len, logits_data);

    struct ValueIndexPair **value_index_pairs =
        malloc(sizeof(struct ValueIndexPair *) * logits_data_len);
    int value_index_pairs_size = 0;
    for (int i = 0; i < logits_data_len; ++i) {

        struct ValueIndexPair *pair = malloc(sizeof(struct ValueIndexPair));
        pair->value = logits_data[i];
        pair->index = i;
        value_index_pairs[i] = pair;
        value_index_pairs_size += 1;
    }
    qsort(value_index_pairs, logits_data_len, sizeof(struct ValueIndexPair *), comparePairs);
    KomeliaRfDetrResult **predictions = malloc(sizeof(KomeliaRfDetrResult *) * 100);
    int topk = min(100, (int)logits_data_len);
    int results_size = 0;
    for (int i = 0; i < topk; ++i) {
        struct ValueIndexPair *pair = value_index_pairs[i];
        if (pair->value < confidence_threshold)
            continue;

        int bbox_idx = (int)(pair->index / pred_logits_dims[2]);
        float *bbox_data = boxes_data + bbox_idx * 4;
        float width = bbox_data[2];
        float height = bbox_data[3];
        // offset from center xy to top left
        float x = bbox_data[0] - 0.5f * width;
        float y = bbox_data[1] - 0.5f * height;

        KomeliaRect *bbox = malloc(sizeof(KomeliaRect));
        bbox->x = (int)nearbyintf(x * (float)original_width);
        bbox->y = (int)nearbyintf(y * (float)original_height);
        bbox->width = (int)nearbyintf(width * (float)original_width);
        bbox->height = (int)nearbyintf(height * (float)original_height);

        KomeliaRfDetrResult *res = malloc(sizeof(KomeliaRfDetrResult));
        res->class_id = (int)(pair->index % pred_logits_dims[2]);
        res->confidence = pair->value;
        res->box = bbox;
        predictions[i] = res;
        results_size += 1;
    }

    KomeliaRfDetrResults *results = malloc(sizeof(KomeliaRfDetrResults));
    results->data = predictions;
    results->results_size = results_size;

    for (int i = 0; i < logits_data_len; ++i) {
        free(value_index_pairs[i]);
    }
    free(value_index_pairs);
    free(boxes_data);
    free(logits_data);

    return results;
on_error:
    wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_INFERENCE, error);
    if (boxes_data != nullptr)
        free(boxes_data);
    if (logits_data != nullptr)
        free(logits_data);
    return nullptr;
}

KomeliaRfDetrResults *komelia_ort_rfdetr(
    KomeliaRfDetr *rf_detr,
    VipsImage *image,
    GError **error
) {
    pthread_mutex_lock(&rf_detr->mutex);

    if (rf_detr->session == nullptr) {
        GError *session_init_error = nullptr;
        SessionData *session = komelia_ort_create_session(
            rf_detr->komelia_ort,
            rf_detr->execution_provider,
            rf_detr->device_id,
            rf_detr->model_path,
            &session_init_error
        );
        if (session_init_error != nullptr) {
            g_propagate_error(error, session_init_error);
            return nullptr;
        }
        rf_detr->session = session;
    }
    const OrtApi *ort_api = rf_detr->komelia_ort->ort_api;
    const SessionData *session = rf_detr->session;
    size_t tensor_shape_len;
    OrtStatus *ort_status =
        ort_api->GetDimensionsCount(session->input_tensor_info, &tensor_shape_len);
    if (ort_status != nullptr) {
        wrap_ort_error(
            rf_detr->komelia_ort->ort_api,
            ort_status,
            KOMELIA_ORT_ERROR_INFERENCE,
            error
        );
        pthread_mutex_unlock(&rf_detr->mutex);
        return nullptr;
    }
    int64_t tensor_shape[tensor_shape_len];
    ort_status = ort_api->GetDimensions(session->input_tensor_info, tensor_shape, tensor_shape_len);
    if (ort_status != nullptr) {
        wrap_ort_error(
            rf_detr->komelia_ort->ort_api,
            ort_status,
            KOMELIA_ORT_ERROR_INFERENCE,
            error
        );
        pthread_mutex_unlock(&rf_detr->mutex);
        return nullptr;
    }

    const int input_width = vips_image_get_width(image);
    const int input_height = vips_image_get_height(image);
    const int input_tensor_width = (int)tensor_shape[tensor_shape_len - 1];
    const int input_tensor_height = (int)tensor_shape[tensor_shape_len - 2];

    GError *input_tensor_error = nullptr;
    KomeliaOrtInputTensor *input_tensor = create_tensor(
        image,
        input_tensor_width,
        input_tensor_height,
        session->input_data_type,
        &input_tensor_error
    );
    if (input_tensor_error != nullptr) {
        g_propagate_error(error, input_tensor_error);
        pthread_mutex_unlock(&rf_detr->mutex);
        return nullptr;
    }

    GError *inference_error = nullptr;
    InferenceResult *inference_result = komelia_ort_run_inference(
        rf_detr->komelia_ort,
        rf_detr->session,
        input_tensor,
        &inference_error
    );
    free(input_tensor->data);
    free(input_tensor->shape);
    free(input_tensor);

    if (inference_error != nullptr) {
        g_propagate_error(error, inference_error);
        pthread_mutex_unlock(&rf_detr->mutex);
        return nullptr;
    }

    GError *detect_error = nullptr;
    KomeliaRfDetrResults *detect_result = get_results_from_tensor(
        rf_detr,
        inference_result,
        input_width,
        input_height,
        &detect_error
    );
    if (detect_error != nullptr) {
        g_propagate_error(error, detect_error);
        pthread_mutex_unlock(&rf_detr->mutex);
        return nullptr;
    }

    komelia_ort_release_inference_result(rf_detr->komelia_ort, inference_result);
    pthread_mutex_unlock(&rf_detr->mutex);
    return detect_result;
}

void komelia_ort_rfdetr_close_session(KomeliaRfDetr *rf_detr) {
    pthread_mutex_lock(&rf_detr->mutex);
    komelia_ort_close_session(rf_detr->komelia_ort, rf_detr->session);
    rf_detr->session = nullptr;
    pthread_mutex_unlock(&rf_detr->mutex);
}

void komelia_ort_rfdetr_release_result(
    KomeliaRfDetr *rf_detr,
    KomeliaRfDetrResults *result
) {
    for (int i = 0; i < result->results_size; ++i) {
        KomeliaRfDetrResult *data = result->data[i];
        free(data->box);
        free(data);
    }
    free(result->data);
    free(result);
}
