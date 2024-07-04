#include "vips_jni.h"
#include "ort_conversions.h"
#include "onnxruntime_c_api.h"
#include <pthread.h>

#ifdef _WIN32
#include <stdlib.h>
#include <string.h>
#include <wchar.h>
#include <windows.h>

wchar_t *fromUTF8(
        const char *src,
        size_t src_length,  /* = 0 */
        size_t *out_length  /* = NULL */
) {
    if (!src) { return NULL; }

    if (src_length == 0) { src_length = strlen(src); }
    int length = MultiByteToWideChar(CP_UTF8, 0, src, src_length, 0, 0);
    wchar_t *output_buffer = (wchar_t *) malloc((length + 1) * sizeof(wchar_t));
    if (output_buffer) {
        MultiByteToWideChar(CP_UTF8, 0, src, src_length, output_buffer, length);
        output_buffer[length] = L'\0';
    }
    if (out_length) { *out_length = length; }
    return output_buffer;
}
#endif

#ifdef USE_DML
#include "dml_provider_factory.h"
#endif

typedef enum {
    CUDA,
    ROCm,
    DML,
    CPU
} ExecutionProvider;

struct SessionData {
    char *session_model_path;
    OrtSessionOptions *session_options;

    OrtSession *session;
    OrtMemoryInfo *memory_info;
    OrtRunOptions *run_options;
};

struct InferenceData {
    VipsImage *preprocessed_image;
    void *model_input_data;
    char *input_name;
    char *output_name;
    OrtTypeInfo *input_info;
    OrtTensorTypeAndShapeInfo *out_tensor_info;
    OrtValue *input_tensor;
    OrtValue *output_tensor;

    uint8_t *model_output_data;
};

struct UpscaleCacheEntry {
    char *key;
    VipsImage *image;
};

const OrtApi *g_ort = NULL;
OrtEnv *ort_env = NULL;
OrtAllocator *ort_default_allocator = NULL;
ExecutionProvider execution_provider;

struct SessionData current_session = {};
pthread_mutex_t session_mutex = PTHREAD_MUTEX_INITIALIZER;

// TODO use hashmap and store on disk
struct UpscaleCacheEntry upscaled_cache[4] = {0};
int cache_next_element_index = 0;
char *temp_dir = NULL;

VipsImage *get_cache_entry(const char *key) {
    if (key == NULL) return NULL;

    for (int i = 0; i < 4; ++i) {
        if (upscaled_cache[i].key != NULL && strcmp(upscaled_cache[i].key, key) == 0)
            return upscaled_cache[i].image;
    }

    return NULL;
}

void addToCache(VipsImage *image, const char *key) {
    if (key == NULL) return;
    char *cache_key = malloc(sizeof(char *) * strlen(key));
    strcpy(cache_key, key);
    struct UpscaleCacheEntry cacheEntry = {cache_key, image};

    if (cache_next_element_index == 4) {
        struct UpscaleCacheEntry first_entry = upscaled_cache[0];
        if (first_entry.key != NULL) {
            g_object_unref(first_entry.image);
            free(first_entry.key);
        }
        upscaled_cache[0] = cacheEntry;
        cache_next_element_index = 1;
    } else {
        struct UpscaleCacheEntry next_entry = upscaled_cache[cache_next_element_index];
        if (next_entry.key != NULL) {
            g_object_unref(next_entry.image);
            free(next_entry.key);
        }

        upscaled_cache[cache_next_element_index] = cacheEntry;
        ++cache_next_element_index;
    }
}


void throw_jvm_ort_exception(JNIEnv *env, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "io/github/snd_r/OrtException"), message);
}

void release_resources(struct InferenceData resources) {
    free(resources.model_input_data);
    g_object_unref(resources.preprocessed_image);

    if (resources.input_name) { ort_default_allocator->Free(ort_default_allocator, resources.input_name); }
    if (resources.output_name) { ort_default_allocator->Free(ort_default_allocator, resources.output_name); }
    if (resources.out_tensor_info) { g_ort->ReleaseTensorTypeAndShapeInfo(resources.out_tensor_info); }
    if (resources.input_tensor) { g_ort->ReleaseValue(resources.input_tensor); }
    if (resources.output_tensor) { g_ort->ReleaseValue(resources.output_tensor); }
}

void release_session(struct SessionData *session_data) {
    free(session_data->session_model_path);
    g_ort->ReleaseSessionOptions(session_data->session_options);
    g_ort->ReleaseRunOptions(session_data->run_options);
    g_ort->ReleaseSession(session_data->session);
    g_ort->ReleaseMemoryInfo(session_data->memory_info);
}

#define ORT_RETURN_ON_ERROR(jni_env, resources, expr)                           \
  do {                                                                          \
    OrtStatus* ort_status = (expr);                                             \
    if (ort_status != NULL) {                                                   \
      const char* msg = g_ort->GetErrorMessage(ort_status);                     \
      throw_jvm_ort_exception(jni_env, g_ort->GetErrorMessage(ort_status));     \
      g_ort->ReleaseStatus(ort_status);                                         \
                                                                                \
      release_resources(resources);                                             \
      return NULL;                                                              \
    }                                                                           \
  } while (0)

JNIEXPORT void JNICALL
Java_io_github_snd_1r_VipsOnnxRuntimeDecoder_init(JNIEnv *env, jobject this, jstring provider, jstring tempDir) {
    g_ort = OrtGetApiBase()->GetApi(ORT_API_VERSION);
    if (!g_ort) {
        throw_jvm_ort_exception(env, "Failed to init ONNX Runtime engine");
        return;
    }

    OrtStatus *ort_status = g_ort->CreateEnv(ORT_LOGGING_LEVEL_WARNING, "komelia", &ort_env);
    if (ort_status != NULL) {
        const char *msg = g_ort->GetErrorMessage(ort_status);
        throw_jvm_ort_exception(env, msg);
        g_ort->ReleaseStatus(ort_status);
        return;
    }
    ort_status = g_ort->GetAllocatorWithDefaultOptions(&ort_default_allocator);
    if (ort_status != NULL) {
        const char *msg = g_ort->GetErrorMessage(ort_status);
        throw_jvm_ort_exception(env, msg);
        g_ort->ReleaseStatus(ort_status);
        return;
    }

    const char *provider_chars = (*env)->GetStringUTFChars(env, provider, 0);
    if (strcmp(provider_chars, "CUDA") == 0) execution_provider = CUDA;
    else if (strcmp(provider_chars, "ROCM") == 0) execution_provider = ROCm;
    else if (strcmp(provider_chars, "DML") == 0) execution_provider = DML;
    else if (strcmp(provider_chars, "CPU") == 0) execution_provider = CPU;
    (*env)->ReleaseStringUTFChars(env, provider, provider_chars);

    const char *temp_dir_chars = (*env)->GetStringUTFChars(env, tempDir, 0);
    temp_dir = malloc(sizeof(char *) * (*env)->GetStringLength(env, tempDir));
    strcpy(temp_dir, temp_dir_chars);

}

int preprocess_for_inference(JNIEnv *env, VipsImage *input_image, VipsImage **output_image) {

    VipsInterpretation interpretation = vips_image_get_interpretation(input_image);
    int input_bands = vips_image_get_bands(input_image);
    int width = vips_image_get_width(input_image);
    int height = vips_image_get_height(input_image);
    VipsImage *transformed = NULL;

    if (interpretation != VIPS_INTERPRETATION_sRGB) {
        int vips_error = vips_colourspace(input_image, &transformed, VIPS_INTERPRETATION_sRGB, NULL);
        if (vips_error) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            return -1;
        }
    }

    if (input_bands == 4) {
        VipsImage *without_alpha = NULL;
        int vips_error;
        if (transformed != NULL) {
            vips_error = vips_flatten(transformed, &without_alpha, NULL);
            g_object_unref(transformed);
        } else {
            vips_error = vips_flatten(input_image, &without_alpha, NULL);
        }

        if (vips_error) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            return -1;
        }
        transformed = without_alpha;
    }


    int pad_width = 0;
    int pad_height = 0;
    if (width % 2 != 0) { pad_width = 1; }
    if (height % 2 != 0) { pad_height = 1; }

    if (pad_width || pad_height) {
        VipsImage *extended = NULL;
        int vips_error;
        if (transformed != NULL) {
            vips_error = vips_gravity(transformed, &extended, VIPS_COMPASS_DIRECTION_WEST,
                                      width + pad_width, height + pad_height,
                                      "extend", VIPS_EXTEND_BLACK, NULL);
            g_object_unref(transformed);
        } else {
            vips_error = vips_gravity(input_image, &extended, VIPS_COMPASS_DIRECTION_WEST,
                                      width + pad_width, height + pad_height,
                                      "extend", VIPS_EXTEND_BLACK, NULL);

        }
        if (vips_error) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            return -1;
        }
        transformed = extended;
    }

    *output_image = transformed;

    return 0;
}

OrtStatus *create_tensor_f32(JNIEnv *env,
                             VipsImage *input_image,
                             OrtMemoryInfo *memory_info,
                             float **tensor_data,
                             OrtValue **tensor
) {
    int input_height = vips_image_get_height(input_image);
    int input_width = vips_image_get_width(input_image);

    const int64_t tensor_shape[] = {1, 3, input_height, input_width};
    const size_t tensor_input_ele_count = input_height * input_width * 3;
    const size_t tensor_shape_len = sizeof(tensor_shape) / sizeof(tensor_shape[0]);

    const size_t tensor_data_len = tensor_input_ele_count * sizeof(float);
    *tensor_data = (float *) malloc(tensor_data_len);

    unsigned char *image_input_data = (unsigned char *) vips_image_get_data(input_image);
    hwc_to_chw(image_input_data, input_height, input_width, 3, *tensor_data);

    OrtStatus *ort_status = NULL;
    ort_status = g_ort->CreateTensorWithDataAsOrtValue(memory_info,
                                                       *tensor_data, tensor_data_len,
                                                       tensor_shape, tensor_shape_len,
                                                       ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT,
                                                       tensor
    );
    if (ort_status) { return ort_status; }
    return NULL;
}

OrtStatus *create_tensor_f16(JNIEnv *env,
                             VipsImage *input_image,
                             OrtMemoryInfo *memory_info,
                             _Float16 **tensor_data,
                             OrtValue **tensor
) {
    int input_height = vips_image_get_height(input_image);
    int input_width = vips_image_get_width(input_image);

    const int64_t tensor_shape[] = {1, 3, input_height, input_width};
    const size_t tensor_input_ele_count = input_height * input_width * 3;
    const size_t tensor_shape_len = sizeof(tensor_shape) / sizeof(tensor_shape[0]);

    const size_t tensor_data_len = tensor_input_ele_count * sizeof(_Float16);
    *tensor_data = (_Float16 *) malloc(tensor_data_len);
    unsigned char *image_input_data = (unsigned char *) vips_image_get_data(input_image);
    hwc_to_chw_f16(image_input_data, input_height, input_width, 3, *tensor_data);

    OrtStatus *ort_status = NULL;
    ort_status = g_ort->CreateTensorWithDataAsOrtValue(memory_info,
                                                       *tensor_data, tensor_data_len,
                                                       tensor_shape, tensor_shape_len,
                                                       ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16,
                                                       tensor
    );
    if (ort_status) { return ort_status; }
    return NULL;
}

VipsImage *run_inference(JNIEnv *env,
                         struct SessionData *session_info,
                         VipsImage *input_image,
                         const char *cache_key
) {
    VipsImage *cache_entry = get_cache_entry(cache_key);
    if (cache_entry != NULL) return cache_entry;
    struct InferenceData inference_data = {0};

    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->SessionGetInputTypeInfo(session_info->session, 0, &inference_data.input_info)
    );
    const OrtTensorTypeAndShapeInfo *input_tensor_info;
    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->CastTypeInfoToTensorInfo(inference_data.input_info, &input_tensor_info));

    ONNXTensorElementDataType input_element_type;
    ORT_RETURN_ON_ERROR(env, inference_data, g_ort->GetTensorElementType(input_tensor_info, &input_element_type));

    int processing_error = preprocess_for_inference(env, input_image, &inference_data.preprocessed_image);
    if (processing_error) {
        release_resources(inference_data);
        return NULL;
    }
    if (inference_data.preprocessed_image == NULL) {
        inference_data.preprocessed_image = input_image;
        g_object_ref(inference_data.preprocessed_image);
    }

    switch (input_element_type) {
        case ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT:
            ORT_RETURN_ON_ERROR(env, inference_data,
                                create_tensor_f32(env, inference_data.preprocessed_image, session_info->memory_info,
                                                  (float **) &inference_data.model_input_data,
                                                  &inference_data.input_tensor
                                ));
            break;
        case ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16:
            ORT_RETURN_ON_ERROR(env, inference_data,
                                create_tensor_f16(env, inference_data.preprocessed_image, session_info->memory_info,
                                                  (_Float16 * *) & inference_data.model_input_data,
                                                  &inference_data.input_tensor)
            );
            break;
        default: {
            throw_jvm_ort_exception(env, "Unsupported model input data format. Only float32 and float16 are supported");
            return NULL;
        }
    }
    const char *input_names[1];
    const char *output_names[1];

    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->SessionGetInputName(session_info->session, 0, ort_default_allocator,
                                                   &inference_data.input_name));

    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->SessionGetOutputName(session_info->session, 0, ort_default_allocator,
                                                    &inference_data.output_name));

    input_names[0] = inference_data.input_name;
    output_names[0] = inference_data.output_name;

    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->Run(session_info->session, session_info->run_options, input_names,
                                   (const OrtValue *const *) &inference_data.input_tensor, 1,
                                   output_names, 1, &inference_data.output_tensor
                        )
    );


    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->GetTensorTypeAndShape(inference_data.output_tensor, &inference_data.out_tensor_info)
    );

    size_t dim_length;
    ORT_RETURN_ON_ERROR(env, inference_data, g_ort->GetDimensionsCount(inference_data.out_tensor_info, &dim_length));

    if (dim_length != 4) {
        throw_jvm_ort_exception(env, "Unexpected number of output dimensions");
        return NULL;
    }

    int64_t dim_values[dim_length];
    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->GetDimensions(inference_data.out_tensor_info, dim_values, dim_length));

    int output_width = (int) dim_values[dim_length - 1];
    int output_height = (int) dim_values[dim_length - 2];
    int output_size = output_height * output_width * 3;


    void *output_tensor_data = NULL;
    ORT_RETURN_ON_ERROR(env, inference_data,
                        g_ort->GetTensorMutableData(inference_data.output_tensor, (void **) &output_tensor_data));

    uint8_t *output_image_data = (uint8_t *) malloc(output_size);
    if (input_element_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT) {
        chw_to_hwc(output_tensor_data, output_height, output_width, 3, output_image_data);
    } else {
        chw_to_hwc_f16(output_tensor_data, output_height, output_width, 3, output_image_data);
    }

    VipsImage *inferred_image = vips_image_new_from_memory_copy(output_image_data, output_size,
                                                                output_width, output_height, 3,
                                                                VIPS_FORMAT_UCHAR);
    free(output_image_data);
    addToCache(inferred_image, cache_key);
    release_resources(inference_data);
    return inferred_image;
}

int enable_cuda(JNIEnv *env, OrtSessionOptions *options) {
    OrtCUDAProviderOptions cuda_options;
    memset(&cuda_options, 0, sizeof(cuda_options));
    cuda_options.device_id = 0;
    cuda_options.cudnn_conv_algo_search = OrtCudnnConvAlgoSearchHeuristic;
    cuda_options.gpu_mem_limit = SIZE_MAX;
    cuda_options.arena_extend_strategy = 0;
    cuda_options.do_copy_in_default_stream = 1;
    cuda_options.has_user_compute_stream = 0;
    cuda_options.user_compute_stream = 0;
    cuda_options.default_memory_arena_cfg = NULL;
    cuda_options.tunable_op_enable = 0;
    cuda_options.tunable_op_max_tuning_duration_ms = 0;

    OrtStatus *onnx_status = g_ort->SessionOptionsAppendExecutionProvider_CUDA(options, &cuda_options);
    if (onnx_status != NULL) {
        throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
        g_ort->ReleaseStatus(onnx_status);
        return -1;
    }
    return 0;
}

int enable_rocm(JNIEnv *env, OrtSessionOptions *options) {
    OrtROCMProviderOptions rocm_opts;
    memset(&rocm_opts, 0, sizeof(rocm_opts));
    rocm_opts.device_id = 0;
    rocm_opts.miopen_conv_exhaustive_search = 0;
    rocm_opts.gpu_mem_limit = SIZE_MAX;
    rocm_opts.arena_extend_strategy = 0;
    rocm_opts.do_copy_in_default_stream = 1;
    rocm_opts.has_user_compute_stream = 0;
    rocm_opts.user_compute_stream = 0;
    rocm_opts.enable_hip_graph = 0;
    rocm_opts.tunable_op_enable = 0;
    rocm_opts.tunable_op_tuning_enable = 0;
    rocm_opts.tunable_op_max_tuning_duration_ms = 0;

    OrtStatus *onnx_status = g_ort->SessionOptionsAppendExecutionProvider_ROCM(options, &rocm_opts);
    if (onnx_status != NULL) {
        throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
        g_ort->ReleaseStatus(onnx_status);
        return -1;
    }
    return 0;
}

#ifdef USE_DML
int enable_dml(JNIEnv *env, OrtSessionOptions* options) {
    OrtStatus *onnx_status = OrtSessionOptionsAppendExecutionProvider_DML(options, 0);
    if (onnx_status != NULL) {
        throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
        g_ort->ReleaseStatus(onnx_status);
        return -1;
    }
    return 0;
}
#endif

int init_onnx_session(JNIEnv *env,
                      jstring modelPath,
                      struct SessionData *session_data
) {
    if (modelPath == NULL) return 0;

    const char *model_path_chars = (*env)->GetStringUTFChars(env, modelPath, 0);
    if (session_data->session_model_path == NULL || strcmp(session_data->session_model_path, model_path_chars) != 0) {
        release_session(session_data);

        jsize model_path_char_length = (*env)->GetStringLength(env, modelPath);
        session_data->session_model_path = malloc(sizeof(char) * model_path_char_length);
        strcpy(session_data->session_model_path, model_path_chars);

        OrtStatus *onnx_status = g_ort->CreateSessionOptions(&session_data->session_options);
        if (onnx_status != NULL) {
            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
            g_ort->ReleaseStatus(onnx_status);
            return -1;
        }

        onnx_status = g_ort->SetSessionGraphOptimizationLevel(session_data->session_options, ORT_ENABLE_BASIC);
        if (onnx_status != NULL) {
            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
            g_ort->ReleaseStatus(onnx_status);
            return -1;
        }

        int provider_init_error = 0;
        switch (execution_provider) {
            case CUDA:
                provider_init_error = enable_cuda(env, session_data->session_options);
                break;
            case ROCm:
                provider_init_error = enable_rocm(env, session_data->session_options);
                break;
            case DML:
#ifdef USE_DML
                provider_init_error = enable_dml(env, session_data->session_options);
#endif
                break;
            case CPU:
                break;
        }
        if (provider_init_error) {
            return -1;
        }

#ifdef _WIN32
        size_t *wide_length = NULL;
        wchar_t *wide = fromUTF8(model_path_chars, model_path_char_length, wide_length);
        onnx_status = g_ort->CreateSession(ort_env, wide, session_data->session_options, &session_data->session);
        free(wide);
#else
        onnx_status = g_ort->CreateSession(ort_env, session_data->session_model_path, session_data->session_options,
                                           &session_data->session);
#endif
        (*env)->ReleaseStringUTFChars(env, modelPath, model_path_chars);
        if (onnx_status != NULL) {
            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
            g_ort->ReleaseStatus(onnx_status);
            return -1;
        }

        onnx_status = g_ort->CreateCpuMemoryInfo(OrtArenaAllocator, OrtMemTypeDefault, &session_data->memory_info);
        if (onnx_status != NULL) {
            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
            g_ort->ReleaseStatus(onnx_status);
            return -1;
        }

        onnx_status = g_ort->CreateRunOptions(&session_data->run_options);
        if (onnx_status != NULL) {
            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
            g_ort->ReleaseStatus(onnx_status);
            return -1;
        }

//        onnx_status = g_ort->AddRunConfigEntry(run_options, kOrtRunOptionsConfigEnableMemoryArenaShrinkage, "cpu:0");
//        if (onnx_status != NULL) {
//            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
//            g_ort->ReleaseStatus(onnx_status);
//            return -1;
//        }
    } else {
        (*env)->ReleaseStringUTFChars(env, modelPath, model_path_chars);
    }

    return 0;
}

JNIEXPORT jobject JNICALL Java_io_github_snd_1r_VipsOnnxRuntimeDecoder_decodeAndResize(
        JNIEnv *env,
        jobject this,
        jbyteArray encoded,
        jstring modelPath,
        jstring cacheKey,
        jint scaleWidth,
        jint scaleHeight
) {

    jsize inputLen = (*env)->GetArrayLength(env, encoded);
    jbyte *inputBytes = (*env)->GetByteArrayElements(env, encoded, JNI_FALSE);
    const char *cache_key_chars = NULL;
    if (cacheKey != NULL) {
        cache_key_chars = (*env)->GetStringUTFChars(env, cacheKey, 0);
    } else {
        cache_key_chars = "unknown";
    }

    VipsImage *input_image = vips_image_new_from_buffer((unsigned char *) inputBytes, inputLen, "", NULL);
    if (!input_image) {
        throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        return NULL;
    }

    int input_width = vips_image_get_width(input_image);
    int input_height = vips_image_get_height(input_image);

    if (input_width == scaleWidth && input_height == scaleHeight) {
        jobject jvm_image = komelia_image_to_jvm_image_data(env, input_image);
        g_object_unref(input_image);
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        return jvm_image;
    }

    if (input_width >= scaleWidth && input_height >= scaleHeight) {
        VipsImage *output_image = NULL;
        vips_thumbnail_image(input_image, &output_image, scaleWidth, "height", scaleHeight, NULL);

        if (!output_image) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            g_object_unref(input_image);
            (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
            return NULL;
        }

        jobject jvm_image = komelia_image_to_jvm_image_data(env, output_image);
        g_object_unref(input_image);
        g_object_unref(output_image);
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        return jvm_image;

    } else {
        VipsImage *inferred_image = NULL;
        pthread_mutex_lock(&session_mutex);
        int initError = init_onnx_session(env, modelPath, &current_session);
        if (initError) {
            g_object_unref(input_image);
            pthread_mutex_unlock(&session_mutex);
            (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
            return NULL;
        }

        inferred_image = run_inference(env, &current_session, input_image, cache_key_chars);

        g_object_unref(input_image);
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        pthread_mutex_unlock(&session_mutex);

        if (!inferred_image) {
            return NULL;
        }

        int inferred_width = vips_image_get_width(inferred_image);
        int inferred_height = vips_image_get_height(inferred_image);
        if (inferred_width == scaleWidth && inferred_height == scaleHeight) {
            jobject jvm_image = komelia_image_to_jvm_image_data(env, inferred_image);
            return jvm_image;
        }

        VipsImage *output_image = NULL;
        vips_thumbnail_image(inferred_image, &output_image, scaleWidth, "height", scaleHeight, NULL);


        if (!output_image) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            return NULL;
        }

        jobject jvm_image = komelia_image_to_jvm_image_data(env, output_image);
        g_object_unref(output_image);

        if (cacheKey != NULL) {
            (*env)->ReleaseStringUTFChars(env, modelPath, cache_key_chars);
        }
        fflush(stderr);
        return jvm_image;
    }
}
