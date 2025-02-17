#include "../vips/vips_common_jni.h"
#include "onnxruntime_conversions.h"
#include <math.h>
#include <onnxruntime_c_api.h>
#include <pthread.h>

#ifdef _WIN32
#include "win32_strings.h"

#endif

#ifdef USE_DML
#include "dml_provider_factory.h"
#endif

typedef enum { TENSOR_RT, CUDA, ROCm, DML, CPU } ExecutionProvider;

struct SessionData {
  OrtSessionOptions *session_options;
  OrtSession *session;
  OrtMemoryInfo *memory_info;
  OrtRunOptions *run_options;
  bool initialized;
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
};

static const OrtApi *g_ort = nullptr;
static OrtEnv *ort_env = nullptr;
static OrtAllocator *ort_default_allocator = nullptr;
static ExecutionProvider execution_provider;

static int current_tile_size = 0;
static int current_tile_threshold = 512 * 512;
static int current_device_id = 0;
static char *current_model_path = nullptr;

static struct SessionData current_session = {0};
static pthread_mutex_t session_mutex = PTHREAD_MUTEX_INITIALIZER;

void throw_jvm_ort_exception(JNIEnv *env, const char *message) {
  (*env)->ThrowNew(
      env, (*env)->FindClass(env, "snd/komelia/image/OnnxRuntimeUpscaler$OrtException"), message);
}

void release_resources(struct InferenceData resources) {
  if (resources.model_input_data) {
    free(resources.model_input_data);
  }
  if (resources.preprocessed_image) {
    g_object_unref(resources.preprocessed_image);
  }

  if (resources.input_name) {
    ort_default_allocator->Free(ort_default_allocator, resources.input_name);
  }
  if (resources.output_name) {
    ort_default_allocator->Free(ort_default_allocator, resources.output_name);
  }
  if (resources.out_tensor_info) {
    g_ort->ReleaseTensorTypeAndShapeInfo(resources.out_tensor_info);
  }
  if (resources.input_tensor) {
    g_ort->ReleaseValue(resources.input_tensor);
  }
  if (resources.output_tensor) {
    g_ort->ReleaseValue(resources.output_tensor);
  }
}

void release_session(struct SessionData *session_data) {
  if (session_data->session_options) {
    g_ort->ReleaseSessionOptions(session_data->session_options);
    session_data->session_options = nullptr;
  }
  if (session_data->run_options) {
    g_ort->ReleaseRunOptions(session_data->run_options);
    session_data->run_options = nullptr;
  }
  if (session_data->session) {
    g_ort->ReleaseSession(session_data->session);
    session_data->session = nullptr;
  }
  if (session_data->memory_info) {
    g_ort->ReleaseMemoryInfo(session_data->memory_info);
    session_data->memory_info = nullptr;
  }
  session_data->initialized = false;
}

#define ORT_RELEASE_ON_ERROR(jni_env, resources, expr)                                             \
  do {                                                                                             \
    OrtStatus *ort_status = (expr);                                                                \
    if (ort_status != nullptr) {                                                                      \
      const char *msg = g_ort->GetErrorMessage(ort_status);                                        \
      throw_jvm_ort_exception(jni_env, g_ort->GetErrorMessage(ort_status));                        \
      g_ort->ReleaseStatus(ort_status);                                                            \
                                                                                                   \
      release_resources(resources);                                                                \
      return nullptr;                                                                                 \
    }                                                                                              \
  } while (0)

#define ORT_INT_STATUS_THROW(jni_env, expr)                                                        \
  do {                                                                                             \
    OrtStatus *ort_status = (expr);                                                                \
    if (ort_status != nullptr) {                                                                      \
      throw_jvm_ort_exception(jni_env, g_ort->GetErrorMessage(ort_status));                        \
      g_ort->ReleaseStatus(ort_status);                                                            \
      return -1;                                                                                   \
    }                                                                                              \
  } while (0)

int enable_cuda(JNIEnv *env, int device_id, OrtSessionOptions *options) {
  OrtCUDAProviderOptions cuda_options;
  memset(&cuda_options, 0, sizeof(cuda_options));
  cuda_options.device_id = device_id;
  cuda_options.cudnn_conv_algo_search = OrtCudnnConvAlgoSearchHeuristic;
  cuda_options.gpu_mem_limit = SIZE_MAX;
  cuda_options.arena_extend_strategy = 0;
  cuda_options.do_copy_in_default_stream = 1;
  cuda_options.has_user_compute_stream = 0;
  cuda_options.user_compute_stream = 0;
  cuda_options.default_memory_arena_cfg = nullptr;
  cuda_options.tunable_op_enable = 0;
  cuda_options.tunable_op_max_tuning_duration_ms = 0;

  OrtStatus *onnx_status =
      g_ort->SessionOptionsAppendExecutionProvider_CUDA(options, &cuda_options);
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }
  return 0;
}

int enable_tensorrt(JNIEnv *env, int device_id, OrtSessionOptions *options) {
  OrtTensorRTProviderOptions tensorrt_options = {0};
  tensorrt_options.device_id = device_id;
  tensorrt_options.trt_fp16_enable = 1;
  tensorrt_options.trt_engine_cache_enable = 1;
  tensorrt_options.trt_engine_cache_path = g_get_tmp_dir();
  ORT_INT_STATUS_THROW(
      env, g_ort->SessionOptionsAppendExecutionProvider_TensorRT(options, &tensorrt_options));

  return enable_cuda(env, device_id, options);
}

int enable_rocm(JNIEnv *env, int device_id, OrtSessionOptions *options) {
  OrtROCMProviderOptions rocm_opts;
  memset(&rocm_opts, 0, sizeof(rocm_opts));
  rocm_opts.device_id = device_id;
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
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }
  return 0;
}

#ifdef USE_DML
int enable_dml(JNIEnv *env, int device_id, OrtSessionOptions *options) {
  const OrtDmlApi *ortDmlApi = nullptr;
  ORT_INT_STATUS_THROW(
      env, g_ort->GetExecutionProviderApi("DML", ORT_API_VERSION, (const void **)&ortDmlApi));

  ORT_INT_STATUS_THROW(env, g_ort->SetSessionExecutionMode(options, ORT_SEQUENTIAL));
  ORT_INT_STATUS_THROW(env, g_ort->DisableMemPattern(options));
  ORT_INT_STATUS_THROW(env,
                       ortDmlApi->SessionOptionsAppendExecutionProvider_DML(options, device_id));
  return 0;
}
#endif

int init_onnxruntime_session(JNIEnv *env) {
  release_session(&current_session);
  OrtStatus *onnx_status = g_ort->CreateSessionOptions(&current_session.session_options);
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }

  onnx_status =
      g_ort->SetSessionGraphOptimizationLevel(current_session.session_options, ORT_ENABLE_BASIC);
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }

  int provider_init_error = 0;
  switch (execution_provider) {
  case TENSOR_RT:
    provider_init_error = enable_tensorrt(env, current_device_id, current_session.session_options);
    break;
  case CUDA:
    provider_init_error = enable_cuda(env, current_device_id, current_session.session_options);
    break;
  case ROCm:
    provider_init_error = enable_rocm(env, current_device_id, current_session.session_options);
    break;
#ifdef USE_DML
  case DML:
    provider_init_error = enable_dml(env, current_device_id, current_session.session_options);
    break;
#endif
  default:
    break;
  }
  if (provider_init_error) {
    return -1;
  }

#ifdef _WIN32
  size_t *wide_length = nullptr;
  wchar_t *wide_model_path = fromUTF8(current_model_path, 0, wide_length);
  onnx_status = g_ort->CreateSession(ort_env, wide_model_path, current_session.session_options,
                                     &current_session.session);
  free(wide_model_path);
#else
  onnx_status = g_ort->CreateSession(ort_env, current_model_path, current_session.session_options,
                                     &current_session.session);
#endif
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }

  onnx_status = g_ort->CreateCpuMemoryInfo(OrtArenaAllocator, OrtMemTypeDefault,
                                           &current_session.memory_info);
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }

  onnx_status = g_ort->CreateRunOptions(&current_session.run_options);
  if (onnx_status != nullptr) {
    throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
    g_ort->ReleaseStatus(onnx_status);
    return -1;
  }

  //        onnx_status = g_ort->AddRunConfigEntry(run_options,
  //        kOrtRunOptionsConfigEnableMemoryArenaShrinkage, "cpu:0"); if (onnx_status != nullptr) {
  //            throw_jvm_ort_exception(env, g_ort->GetErrorMessage(onnx_status));
  //            g_ort->ReleaseStatus(onnx_status);
  //            return -1;
  //        }
  //    } else {
  //        (*env)->ReleaseStringUTFChars(env, modelPath, model_path_chars);
  //    }

  current_session.initialized = true;
  return 0;
}

int preprocess_for_inference(JNIEnv *env, VipsImage *input_image, VipsImage **output_image) {

  VipsInterpretation interpretation = vips_image_get_interpretation(input_image);
  int input_bands = vips_image_get_bands(input_image);
  int width = vips_image_get_width(input_image);
  int height = vips_image_get_height(input_image);
  VipsImage *transformed = nullptr;

  if (interpretation != VIPS_INTERPRETATION_sRGB) {
    int vips_error = vips_colourspace(input_image, &transformed, VIPS_INTERPRETATION_sRGB, nullptr);
    if (vips_error) {
      komelia_throw_jvm_vips_exception(env, vips_error_buffer());
      vips_error_clear();
      return -1;
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
      komelia_throw_jvm_vips_exception(env, vips_error_buffer());
      vips_error_clear();
      return -1;
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
      vips_error =
          vips_gravity(transformed, &extended, VIPS_COMPASS_DIRECTION_WEST, width + pad_width,
                       height + pad_height, "extend", VIPS_EXTEND_BLACK, nullptr);
      g_object_unref(transformed);
    } else {
      vips_error =
          vips_gravity(input_image, &extended, VIPS_COMPASS_DIRECTION_WEST, width + pad_width,
                       height + pad_height, "extend", VIPS_EXTEND_BLACK, nullptr);
    }
    if (vips_error) {
      komelia_throw_jvm_vips_exception(env, vips_error_buffer());
      vips_error_clear();
      return -1;
    }
    transformed = extended;
  }

  *output_image = transformed;

  return 0;
}

OrtStatus *create_tensor_f32(VipsImage *input_image, OrtMemoryInfo *memory_info,
                             float **tensor_data, OrtValue **tensor) {
  int input_height = vips_image_get_height(input_image);
  int input_width = vips_image_get_width(input_image);

  const int64_t tensor_shape[] = {1, 3, input_height, input_width};
  const size_t tensor_input_ele_count = input_height * input_width * 3;
  const size_t tensor_shape_len = sizeof(tensor_shape) / sizeof(tensor_shape[0]);

  const size_t tensor_data_len = tensor_input_ele_count * sizeof(float);
  *tensor_data = (float *)malloc(tensor_data_len);

  unsigned char *image_input_data = (unsigned char *)vips_image_get_data(input_image);
  hwc_to_chw(image_input_data, input_height, input_width, 3, *tensor_data);

  return g_ort->CreateTensorWithDataAsOrtValue(memory_info, *tensor_data, tensor_data_len,
                                               tensor_shape, tensor_shape_len,
                                               ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT, tensor);
}

OrtStatus *create_tensor_f16(VipsImage *input_image, OrtMemoryInfo *memory_info,
                             _Float16 **tensor_data, OrtValue **tensor) {
  int input_height = vips_image_get_height(input_image);
  int input_width = vips_image_get_width(input_image);

  const int64_t tensor_shape[] = {1, 3, input_height, input_width};
  const size_t tensor_input_ele_count = input_height * input_width * 3;
  const size_t tensor_shape_len = sizeof(tensor_shape) / sizeof(tensor_shape[0]);

  const size_t tensor_data_len = tensor_input_ele_count * sizeof(_Float16);
  *tensor_data = (_Float16 *)malloc(tensor_data_len);
  unsigned char *image_input_data = (unsigned char *)vips_image_get_data(input_image);
  hwc_to_chw_f16(image_input_data, input_height, input_width, 3, *tensor_data);

  return g_ort->CreateTensorWithDataAsOrtValue(memory_info, *tensor_data, tensor_data_len,
                                               tensor_shape, tensor_shape_len,
                                               ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16, tensor);
}

VipsImage *run_inference(JNIEnv *env, struct SessionData *session_info, VipsImage *input_image) {
  struct InferenceData inference_data = {0};

  ORT_RELEASE_ON_ERROR(
      env, inference_data,
      g_ort->SessionGetInputTypeInfo(session_info->session, 0, &inference_data.input_info));
  const OrtTensorTypeAndShapeInfo *input_tensor_info;
  ORT_RELEASE_ON_ERROR(
      env, inference_data,
      g_ort->CastTypeInfoToTensorInfo(inference_data.input_info, &input_tensor_info));

  ONNXTensorElementDataType input_element_type;
  ORT_RELEASE_ON_ERROR(env, inference_data,
                       g_ort->GetTensorElementType(input_tensor_info, &input_element_type));

  int processing_error =
      preprocess_for_inference(env, input_image, &inference_data.preprocessed_image);
  if (processing_error) {
    release_resources(inference_data);
    return nullptr;
  }
  if (inference_data.preprocessed_image == nullptr) {
    inference_data.preprocessed_image = input_image;
    g_object_ref(inference_data.preprocessed_image);
  }

  switch (input_element_type) {
  case ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT:
    ORT_RELEASE_ON_ERROR(env, inference_data,
                         create_tensor_f32(inference_data.preprocessed_image,
                                           session_info->memory_info,
                                           (float **)&inference_data.model_input_data,
                                           &inference_data.input_tensor));
    break;
  case ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT16:
    ORT_RELEASE_ON_ERROR(env, inference_data,
                         create_tensor_f16(inference_data.preprocessed_image,
                                           session_info->memory_info,
                                           (_Float16 **)&inference_data.model_input_data,
                                           &inference_data.input_tensor));
    break;
  default: {
    throw_jvm_ort_exception(
        env, "Unsupported model input data format. Only float32 and float16 are supported");
    release_resources(inference_data);
    return nullptr;
  }
  }
  const char *input_names[1];
  const char *output_names[1];

  ORT_RELEASE_ON_ERROR(env, inference_data,
                       g_ort->SessionGetInputName(session_info->session, 0, ort_default_allocator,
                                                  &inference_data.input_name));

  ORT_RELEASE_ON_ERROR(env, inference_data,
                       g_ort->SessionGetOutputName(session_info->session, 0, ort_default_allocator,
                                                   &inference_data.output_name));

  input_names[0] = inference_data.input_name;
  output_names[0] = inference_data.output_name;

  ORT_RELEASE_ON_ERROR(env, inference_data,
                       g_ort->Run(session_info->session, session_info->run_options, input_names,
                                  (const OrtValue *const *)&inference_data.input_tensor, 1,
                                  output_names, 1, &inference_data.output_tensor));

  ORT_RELEASE_ON_ERROR(
      env, inference_data,
      g_ort->GetTensorTypeAndShape(inference_data.output_tensor, &inference_data.out_tensor_info));

  size_t dim_length;
  ORT_RELEASE_ON_ERROR(env, inference_data,
                       g_ort->GetDimensionsCount(inference_data.out_tensor_info, &dim_length));

  if (dim_length != 4) {
    throw_jvm_ort_exception(env, "Unexpected number of output dimensions");
    release_resources(inference_data);
    return nullptr;
  }

  int64_t dim_values[dim_length];
  ORT_RELEASE_ON_ERROR(
      env, inference_data,
      g_ort->GetDimensions(inference_data.out_tensor_info, dim_values, dim_length));

  int output_width = (int)dim_values[dim_length - 1];
  int output_height = (int)dim_values[dim_length - 2];
  int output_size = output_height * output_width * 3;

  void *output_tensor_data = nullptr;
  ORT_RELEASE_ON_ERROR(
      env, inference_data,
      g_ort->GetTensorMutableData(inference_data.output_tensor, (void **)&output_tensor_data));

  uint8_t *output_image_data = (uint8_t *)malloc(output_size);
  if (input_element_type == ONNX_TENSOR_ELEMENT_DATA_TYPE_FLOAT) {
    chw_to_hwc(output_tensor_data, output_height, output_width, 3, output_image_data);
  } else {
    chw_to_hwc_f16(output_tensor_data, output_height, output_width, 3, output_image_data);
  }

  VipsImage *inferred_image = vips_image_new_from_memory_copy(
      output_image_data, output_size, output_width, output_height, 3, VIPS_FORMAT_UCHAR);
  free(output_image_data);
  release_resources(inference_data);
  return inferred_image;
}

JNIEXPORT void JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_init(JNIEnv *env, jclass this,
                                                                       jstring provider) {
  g_ort = OrtGetApiBase()->GetApi(ORT_API_VERSION);
  if (!g_ort) {
    char message[124];
    snprintf(message, 124,
             "The requested API version [%u] is not available. Update to newer version",
             ORT_API_VERSION);
    throw_jvm_ort_exception(env, message);
    return;
  }

  OrtStatus *ort_status = g_ort->CreateEnv(ORT_LOGGING_LEVEL_WARNING, "komelia", &ort_env);
  if (ort_status != nullptr) {
    const char *msg = g_ort->GetErrorMessage(ort_status);
    throw_jvm_ort_exception(env, msg);
    g_ort->ReleaseStatus(ort_status);
    return;
  }
  g_ort->DisableTelemetryEvents(ort_env);

  ort_status = g_ort->GetAllocatorWithDefaultOptions(&ort_default_allocator);
  if (ort_status != nullptr) {
    const char *msg = g_ort->GetErrorMessage(ort_status);
    throw_jvm_ort_exception(env, msg);
    g_ort->ReleaseStatus(ort_status);
    return;
  }

  const char *provider_chars = (*env)->GetStringUTFChars(env, provider, 0);
  if (strcmp(provider_chars, "TENSOR_RT") == 0)
    execution_provider = TENSOR_RT;
  else if (strcmp(provider_chars, "CUDA") == 0)
    execution_provider = CUDA;
  else if (strcmp(provider_chars, "ROCM") == 0)
    execution_provider = ROCm;
  else if (strcmp(provider_chars, "DML") == 0)
    execution_provider = DML;
  else if (strcmp(provider_chars, "CPU") == 0)
    execution_provider = CPU;
  (*env)->ReleaseStringUTFChars(env, provider, provider_chars);
}

JNIEXPORT void JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_setTileSize(JNIEnv *env,
                                                                              jclass this,
                                                                              jint tile_size) {
  current_tile_size = tile_size;
}

JNIEXPORT void JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_setModelPath(JNIEnv *env,
                                                                               jclass this,
                                                                               jstring model_path) {
  pthread_mutex_lock(&session_mutex);
  const char *model_path_chars = (*env)->GetStringUTFChars(env, model_path, 0);
  jsize model_path_char_length = (*env)->GetStringLength(env, model_path);
  if (current_model_path != nullptr && strcmp(current_model_path, model_path_chars) == 0) {
    pthread_mutex_unlock(&session_mutex);
    return;
  }

  release_session(&current_session);
  if (current_model_path != nullptr) {
    free(current_model_path);
  }
  current_model_path = malloc(sizeof(char) * model_path_char_length);
  strcpy(current_model_path, model_path_chars);
  (*env)->ReleaseStringUTFChars(env, model_path, model_path_chars);

  pthread_mutex_unlock(&session_mutex);
}

JNIEXPORT void JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_setDeviceId(JNIEnv *env,
                                                                              jclass this,
                                                                              jint device_id) {
  pthread_mutex_lock(&session_mutex);
  release_session(&current_session);
  current_device_id = device_id;
  pthread_mutex_unlock(&session_mutex);
}

JNIEXPORT void JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_closeCurrentSession(JNIEnv *env,
                                                                                      jclass this) {
  pthread_mutex_lock(&session_mutex);
  release_session(&current_session);
  pthread_mutex_unlock(&session_mutex);
}

VipsImage *tiled_inference(JNIEnv *env, VipsImage *input_image) {
  pthread_mutex_lock(&session_mutex);
  int tile_size = current_tile_size;
  int image_width = vips_image_get_width(input_image);
  int image_height = vips_image_get_height(input_image);
  int image_bands = vips_image_get_bands(input_image);

  int row_tiles = ceil((double)image_width / tile_size);
  int column_tiles = ceil((double)image_height / tile_size);

  int tile_count = 0;
  VipsImage *upscaled_tiles[row_tiles * column_tiles];

  int y_taken = 0;
  while (y_taken != image_height) {
    int x_taken = 0;
    int tile_height = y_taken + tile_size < image_height ? tile_size : image_height - y_taken;
    while (x_taken != image_width) {
      VipsRect region_rect;
      region_rect.top = y_taken;
      region_rect.left = x_taken;
      region_rect.height = tile_height;
      region_rect.width = x_taken + tile_size < image_width ? tile_size : image_width - x_taken;

      VipsRegion *region = vips_region_new(input_image);
      int prepare_error = vips_region_prepare(region, &region_rect);
      if (prepare_error) {
        for (int i = 0; i < tile_count; ++i) {
          g_object_unref(upscaled_tiles[i]);
        }
        g_object_unref(region);
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        pthread_mutex_unlock(&session_mutex);
        return nullptr;
      }

      size_t region_size = 0;
      VipsPel *region_data = vips_region_fetch(region, region_rect.left, region_rect.top,
                                               region_rect.width, region_rect.height, &region_size);
      VipsImage *unformatted_image = nullptr;
      unformatted_image =
          vips_image_new_from_memory(region_data, region_size, region_rect.width,
                                     region_rect.height, image_bands, VIPS_FORMAT_UCHAR);
      if (!unformatted_image) {
        for (int i = 0; i < tile_count; ++i) {
          g_object_unref(upscaled_tiles[i]);
        }
        g_object_unref(region);
        g_free(region_data);
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        pthread_mutex_unlock(&session_mutex);
        return nullptr;
      }

      VipsImage *formatted_region_image = nullptr;
      vips_copy(unformatted_image, &formatted_region_image, "bands",
                vips_image_get_bands(input_image), "format", vips_image_get_format(input_image),
                "coding", vips_image_get_coding(input_image), "interpretation",
                vips_image_get_interpretation(input_image), nullptr);

      if (!formatted_region_image) {
        for (int i = 0; i < tile_count; ++i) {
          g_object_unref(upscaled_tiles[i]);
        }
        g_object_unref(region);
        g_object_unref(unformatted_image);
        g_free(region_data);
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        pthread_mutex_unlock(&session_mutex);
        return nullptr;
      }

      VipsImage *upscaled_image = nullptr;
      upscaled_image = run_inference(env, &current_session, formatted_region_image);
      if (!upscaled_image) {
        for (int i = 0; i < tile_count; ++i) {
          g_object_unref(upscaled_tiles[i]);
        }
        g_object_unref(region);
        g_object_unref(unformatted_image);
        g_object_unref(formatted_region_image);
        g_free(region_data);
        pthread_mutex_unlock(&session_mutex);
        return nullptr;
      }

      g_object_unref(region);
      g_object_unref(unformatted_image);
      g_object_unref(formatted_region_image);
      g_free(region_data);
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
  pthread_mutex_unlock(&session_mutex);

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
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
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
      komelia_throw_jvm_vips_exception(env, vips_error_buffer());
      vips_error_clear();
      return nullptr;
    }
    return cropped;
  }

  return joined;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_upscale(JNIEnv *env,
                                                                             jclass this,
                                                                             jobject vips_image) {
  pthread_mutex_lock(&session_mutex);
  if (!current_session.initialized) {
    int initError = init_onnxruntime_session(env);
    if (initError) {
      pthread_mutex_unlock(&session_mutex);
      return nullptr;
    }
  }
  pthread_mutex_unlock(&session_mutex);

  VipsImage *image = komelia_from_jvm_handle(env, vips_image);
  if (image == nullptr)
    return nullptr;
  int image_width = vips_image_get_width(image);
  int image_height = vips_image_get_height(image);

  VipsImage *upscaled_image = nullptr;
  if (current_tile_size != 0 && image_width * image_height > current_tile_threshold) {
    upscaled_image = tiled_inference(env, image);
  } else {
    pthread_mutex_lock(&session_mutex);
    upscaled_image = run_inference(env, &current_session, image);
    pthread_mutex_unlock(&session_mutex);
  }

  if (!upscaled_image) {
    return nullptr;
  }
  jobject jvm_image = komelia_to_jvm_handle(env, upscaled_image, nullptr);

  vips_thread_shutdown();
  return jvm_image;
}