#include "komelia_onnxruntime.h"
#include "komelia_error.h"
#include "komelia_matrix_ops.h"

#include <onnxruntime_c_api.h>
#include <vips/vips.h>
#define APPNAME "Komelia"

#ifdef _WIN32
#include "win32_strings.h"
#endif
#ifdef USE_DML
#include "dml_provider_factory.h"
#endif
#define KOMELIA_ORT_API_VERSION 21

typedef struct {
    char *input_name;
    char **output_names;
    size_t output_len;
    OrtTypeInfo *input_info;
    OrtValue *input_tensor;
} InferenceData;

void release_session(
    const OrtApi *ort_api,
    SessionData *session_data
) {
    if (session_data == nullptr)
        return;
    if (session_data->session_options) {
        ort_api->ReleaseSessionOptions(session_data->session_options);
        session_data->session_options = nullptr;
    }
    if (session_data->run_options) {
        ort_api->ReleaseRunOptions(session_data->run_options);
        session_data->run_options = nullptr;
    }
    if (session_data->session) {
        ort_api->ReleaseSession(session_data->session);
        session_data->session = nullptr;
    }
    if (session_data->memory_info) {
        ort_api->ReleaseMemoryInfo(session_data->memory_info);
        session_data->memory_info = nullptr;
    }
    free(session_data);
}

void enable_cuda(
    const OrtApi *ort_api,
    const int device_id,
    OrtSessionOptions *options,
    GError **error
) {
    OrtCUDAProviderOptions cuda_options = {0};
    cuda_options.device_id = device_id;
    cuda_options.cudnn_conv_algo_search = OrtCudnnConvAlgoSearchHeuristic;
    cuda_options.gpu_mem_limit = SIZE_MAX;
    cuda_options.arena_extend_strategy = 0;
    cuda_options.do_copy_in_default_stream = 1;
    cuda_options.has_user_compute_stream = 0;
    cuda_options.user_compute_stream = nullptr;
    cuda_options.default_memory_arena_cfg = nullptr;
    cuda_options.tunable_op_enable = 0;
    cuda_options.tunable_op_max_tuning_duration_ms = 0;

    OrtStatus *ort_status =
        ort_api->SessionOptionsAppendExecutionProvider_CUDA(options, &cuda_options);

    if (ort_status != nullptr) {
        g_set_error_literal(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_EXECUTION_PROVIDER_INIT,
            ort_api->GetErrorMessage(ort_status)
        );
        ort_api->ReleaseStatus(ort_status);
    }
}

void enable_tensorrt(
    const OrtApi *ort_api,
    const int device_id,
    const char *data_dir,
    OrtSessionOptions *options,
    GError **error
) {
    OrtTensorRTProviderOptions tensorrt_options = {0};
    tensorrt_options.device_id = device_id;
    tensorrt_options.trt_fp16_enable = 1;
    tensorrt_options.trt_engine_cache_enable = 1;
    tensorrt_options.trt_engine_cache_path = data_dir;
    OrtStatus *ort_status =
        ort_api->SessionOptionsAppendExecutionProvider_TensorRT(options, &tensorrt_options);

    if (ort_status != nullptr) {
        wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_EXECUTION_PROVIDER_INIT, error);
        return;
    }

    GError *cuda_error = nullptr;
    enable_cuda(ort_api, device_id, options, &cuda_error);
    if (cuda_error != nullptr) {
        g_propagate_error(error, cuda_error);
    }
}

void enable_rocm(
    const OrtApi *ort_api,
    const int device_id,
    OrtSessionOptions *options,
    GError **error
) {
    OrtROCMProviderOptions rocm_opts = {0};
    rocm_opts.device_id = device_id;
    rocm_opts.miopen_conv_exhaustive_search = 0;
    rocm_opts.gpu_mem_limit = SIZE_MAX;
    rocm_opts.arena_extend_strategy = 0;
    rocm_opts.do_copy_in_default_stream = 1;
    rocm_opts.has_user_compute_stream = 0;
    rocm_opts.user_compute_stream = nullptr;
    rocm_opts.enable_hip_graph = 0;
    rocm_opts.tunable_op_enable = 0;
    rocm_opts.tunable_op_tuning_enable = 0;
    rocm_opts.tunable_op_max_tuning_duration_ms = 0;

    OrtStatus *ort_status =
        ort_api->SessionOptionsAppendExecutionProvider_ROCM(options, &rocm_opts);
    if (ort_status != nullptr) {
        wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_EXECUTION_PROVIDER_INIT, error);
    }
}

void enable_webGpu(
    const OrtApi *ort_api,
    const int device_id,
    OrtSessionOptions *options,
    GError **error
) {
    OrtStatus *ort_status =
        ort_api->SessionOptionsAppendExecutionProvider(options, "WebGPU", nullptr, nullptr, 0);
    if (ort_status != nullptr) {
        wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_EXECUTION_PROVIDER_INIT, error);
    }
}

#ifdef USE_DML
void enable_dml(
    const OrtApi *ort_api,
    int device_id,
    OrtSessionOptions *options,
    GError **error
) {
    const OrtDmlApi *ortDmlApi = nullptr;
    ort_api->GetExecutionProviderApi("DML", ORT_API_VERSION, (const void **)&ortDmlApi);
    ort_api->SetSessionExecutionMode(options, ORT_SEQUENTIAL);
    ort_api->DisableMemPattern(options);
    OrtStatus *ort_status =
        ortDmlApi->SessionOptionsAppendExecutionProvider_DML(options, device_id);

    if (ort_status != nullptr) {
        g_set_error(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_EXECUTION_PROVIDER_INIT,
            ort_api->GetErrorMessage(ort_status)
        );
        ort_api->ReleaseStatus(ort_status);
    }
}
#endif

SessionData *komelia_ort_create_session(
    KomeliaOrt *komelia_ort,
    KomeliaOrtExecutionProvider execution_provider,
    int device_id,
    char *model_path,
    GError **error
) {
    const OrtApi *ort_api = komelia_ort->ort_api;
    const OrtEnv *ort_env = komelia_ort->ort_env;

    SessionData *session = malloc(sizeof(SessionData));
    session->session_options = nullptr;
    session->session = nullptr;
    session->memory_info = nullptr;
    session->run_options = nullptr;
    session->input_info = nullptr;
    session->input_tensor_info = nullptr;
    session->execution_provider = execution_provider;
    session->device_id = device_id;
    session->model_path = strdup(model_path);

    OrtStatus *ort_status = ort_api->CreateSessionOptions(&session->session_options);
    if (ort_status != nullptr) {
        goto on_error;
    }

    ort_status =
        ort_api->SetSessionGraphOptimizationLevel(session->session_options, ORT_ENABLE_BASIC);

    if (ort_status != nullptr) {
        goto on_error;
    }

    GError *provider_init_error = nullptr;
    switch (execution_provider) {
    case TENSOR_RT:
        enable_tensorrt(
            ort_api,
            device_id,
            komelia_ort->data_dir,
            session->session_options,
            &provider_init_error
        );
        break;
    case CUDA:
        enable_cuda(ort_api, device_id, session->session_options, &provider_init_error);
        break;
    case ROCm:
        enable_rocm(ort_api, device_id, session->session_options, &provider_init_error);
        break;
#ifdef USE_DML
    case DML:
        enable_dml(ort_api, device_id, session->session_options, &provider_init_error);
        break;
#endif
    case WEBGPU:
        enable_webGpu(ort_api, device_id, session->session_options, &provider_init_error);
        break;
    default:
        break;
    }

    if (provider_init_error != nullptr) {
        g_propagate_error(error, provider_init_error);
        return nullptr;
    }

#ifdef _WIN32
    wchar_t *wide_model_path = fromUTF8(session->model_path, 0, nullptr);
    ort_status = ort_api->CreateSession(
        ort_env,
        wide_model_path,
        session->session_options,
        &session->session
    );
    free(wide_model_path);
#else
    ort_status = ort_api->CreateSession(
        ort_env,
        session->model_path,
        session->session_options,
        &session->session
    );
#endif
    if (ort_status != nullptr) {
        goto on_error;
    }

    ort_status =
        ort_api->CreateCpuMemoryInfo(OrtArenaAllocator, OrtMemTypeDefault, &session->memory_info);
    if (ort_status != nullptr) {
        goto on_error;
    }

    ort_status = ort_api->CreateRunOptions(&session->run_options);
    if (ort_status != nullptr) {
        goto on_error;
    }

    OrtTypeInfo *input_info = nullptr;
    ort_status = ort_api->SessionGetInputTypeInfo(session->session, 0, &input_info);
    if (ort_status != nullptr) {
        goto on_error;
    }
    const OrtTensorTypeAndShapeInfo *input_tensor_info;
    ort_status = ort_api->CastTypeInfoToTensorInfo(input_info, &input_tensor_info);
    if (ort_status != nullptr) {
        goto on_error;
    }

    ONNXTensorElementDataType input_element_type;
    ort_status = ort_api->GetTensorElementType(input_tensor_info, &input_element_type);
    if (ort_status != nullptr) {
        goto on_error;
    }
    size_t input_count;
    size_t output_count;
    ort_status = ort_api->SessionGetInputCount(session->session, &input_count);
    if (ort_status != nullptr) {
        goto on_error;
    }
    ort_status = ort_api->SessionGetOutputCount(session->session, &output_count);
    if (ort_status != nullptr) {
        goto on_error;
    }

    session->input_info = input_info;
    session->input_tensor_info = input_tensor_info;
    session->input_data_type = input_element_type;
    // session->input_count = input_count;
    session->output_count = output_count;
    return session;

on_error:
    wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_SESSION_INIT, error);
    release_session(ort_api, session);
    return nullptr;
}

KomeliaOrt *komelia_ort_create(
    const char *data_dir,
    GError **error
) {
    const OrtApi *ort_api = OrtGetApiBase()->GetApi(KOMELIA_ORT_API_VERSION);
    if (ort_api == nullptr) {
        g_set_error(
            error,
            KOMELIA_ORT_ERROR,
            KOMELIA_ORT_ERROR_UNSUPPORTED_API_VERSION,
            "The requested API version [%u] is not available. Update to newer version",
            KOMELIA_ORT_API_VERSION
        );
        return nullptr;
    }
    OrtEnv *ort_env = nullptr;
    OrtStatus *ort_status = ort_api->CreateEnv(ORT_LOGGING_LEVEL_WARNING, "komelia", &ort_env);
    if (ort_status != nullptr) {
        wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_UNKNOWN, error);
        return nullptr;
    }
    ort_status = ort_api->DisableTelemetryEvents(ort_env);
    if (ort_status != nullptr) {
        wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_UNKNOWN, error);
        return nullptr;
    }
    OrtAllocator *ort_default_allocator;
    ort_status = ort_api->GetAllocatorWithDefaultOptions(&ort_default_allocator);
    if (ort_status != nullptr) {
        wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_UNKNOWN, error);
        return nullptr;
    }

    KomeliaOrt *komelia_ort = malloc(sizeof(KomeliaOrt));
    komelia_ort->ort_api = ort_api;
    komelia_ort->ort_env = ort_env;
    komelia_ort->ort_allocator = ort_default_allocator;
    komelia_ort->data_dir = strdup(data_dir);

    return komelia_ort;
}

static void release_inference_data(
    const OrtApi *ort_api,
    OrtAllocator *allocator,
    InferenceData *inference_data
) {
    if (inference_data == nullptr) {
        return;
    }

    if (inference_data->input_name != nullptr) {
        allocator->Free(allocator, inference_data->input_name);
    }
    if (inference_data->output_names != nullptr) {
        for (int i = 0; i < inference_data->output_len; ++i) {
            if (inference_data->output_names[i] != nullptr)
                allocator->Free(allocator, inference_data->output_names[i]);
        }
        free(inference_data->output_names);
    }
    if (inference_data->input_tensor != nullptr) {
        ort_api->ReleaseValue(inference_data->input_tensor);
    }
    free(inference_data);
}

static OrtValue *create_tensor(
    const OrtApi *ort_api,
    const SessionData *session,
    const KomeliaOrtInputTensor *input,
    GError **error
) {
    OrtValue *ort_tensor = nullptr;
    OrtStatus *status = ort_api->CreateTensorWithDataAsOrtValue(
        session->memory_info,
        input->data,
        input->data_len,
        input->shape,
        input->shape_len,
        session->input_data_type,
        &ort_tensor
    );
    if (status != nullptr) {
        wrap_ort_error(ort_api, status, KOMELIA_ORT_ERROR_INFERENCE, error);
    }
    return ort_tensor;
}

static InferenceData *prepare_inference_data(
    KomeliaOrt *komelia_ort,
    SessionData *session,
    const KomeliaOrtInputTensor *input,
    GError **error
) {

    InferenceData *inference_data = malloc(sizeof(InferenceData));
    inference_data->output_names = malloc(sizeof(char *) * session->output_count);
    inference_data->output_len = session->output_count;
    inference_data->input_info = nullptr;
    inference_data->input_tensor = nullptr;

    const OrtApi *ort_api = komelia_ort->ort_api;
    OrtAllocator *ort_allocator = komelia_ort->ort_allocator;

    OrtStatus *ort_status =
        ort_api->SessionGetInputTypeInfo(session->session, 0, &inference_data->input_info);
    if (ort_status != nullptr)
        goto inference_error;

    const OrtTensorTypeAndShapeInfo *input_tensor_info;
    ort_status = ort_api->CastTypeInfoToTensorInfo(inference_data->input_info, &input_tensor_info);
    if (ort_status != nullptr)
        goto inference_error;

    ONNXTensorElementDataType input_element_type;
    ort_status = ort_api->GetTensorElementType(input_tensor_info, &input_element_type);
    if (ort_status != nullptr)
        goto inference_error;

    GError *tensor_create_error = nullptr;
    OrtValue *input_tensor = create_tensor(ort_api, session, input, &tensor_create_error);
    if (tensor_create_error != nullptr) {
        g_propagate_error(error, tensor_create_error);
        release_inference_data(ort_api, ort_allocator, inference_data);
        return nullptr;
    }
    inference_data->input_tensor = input_tensor;
    ort_status = ort_api->SessionGetInputName(
        session->session,
        0,
        komelia_ort->ort_allocator,
        &inference_data->input_name
    );

    if (ort_status != nullptr)
        goto inference_error;

    for (int i = 0; i < session->output_count; ++i) {
        char *out_name = nullptr;
        ort_status =
            ort_api
                ->SessionGetOutputName(session->session, i, komelia_ort->ort_allocator, &out_name);
        inference_data->output_names[i] = out_name;
        if (ort_status != nullptr)
            goto inference_error;
    }
    return inference_data;
inference_error:
    wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_INFERENCE, error);
    release_inference_data(ort_api, ort_allocator, inference_data);
    return nullptr;
}

static InferenceResult *run_inference(
    KomeliaOrt *komelia_ort,
    SessionData *session,
    InferenceData *inference_data,
    GError **error
) {
    const OrtApi *ort_api = komelia_ort->ort_api;
    OrtAllocator *ort_allocator = komelia_ort->ort_allocator;

    size_t out_len = inference_data->output_len;
    // OrtValue *output_tensors = nullptr;
    OrtValue **output_tensors = malloc(sizeof(OrtValue *) * out_len);
    for (int i = 0; i < out_len; ++i) {
        output_tensors[i] = nullptr;
    }
    OrtTensorTypeAndShapeInfo **out_tensors_info =
        calloc(out_len, sizeof(OrtTensorTypeAndShapeInfo *));

    const char *input_names[1];
    const char *output_names[2];
    input_names[0] = inference_data->input_name;
    output_names[0] = inference_data->output_names[0];
    output_names[1] = inference_data->output_names[1];
    const OrtValue *inputs[1];
    inputs[0] = inference_data->input_tensor;
    OrtStatus *ort_status = ort_api->Run(
        session->session,
        session->run_options,
        input_names,
        inputs,
        1,
        output_names,
        out_len,
        output_tensors
    );
    if (ort_status != nullptr) {
        goto inference_error;
    }

    for (int i = 0; i < out_len; ++i) {
        ort_status = ort_api->GetTensorTypeAndShape(output_tensors[i], &out_tensors_info[i]);
        if (ort_status != nullptr)
            goto inference_error;
    }

    release_inference_data(ort_api, ort_allocator, inference_data);
    InferenceResult *result = malloc(sizeof(InferenceResult));
    result->out_tensors_info = out_tensors_info;
    result->output_tensors = output_tensors;
    result->output_len = out_len;
    return result;
inference_error:
    wrap_ort_error(ort_api, ort_status, KOMELIA_ORT_ERROR_INFERENCE, error);
    release_inference_data(ort_api, ort_allocator, inference_data);
    free(output_tensors);
    free(out_tensors_info);
    return nullptr;
}

InferenceResult *komelia_ort_run_inference(
    KomeliaOrt *komelia_ort,
    SessionData *session,
    const KomeliaOrtInputTensor *input,
    GError **error
) {

    GError *prepare_error = nullptr;
    InferenceData *inference_data =
        prepare_inference_data(komelia_ort, session, input, &prepare_error);
    if (prepare_error != nullptr) {
        g_propagate_error(error, prepare_error);
        return nullptr;
    }

    GError *inference_error = nullptr;
    InferenceResult *result = run_inference(komelia_ort, session, inference_data, &inference_error);
    if (inference_error != nullptr) {
        g_propagate_error(error, inference_error);
        // release_inference_data(komelia_ort->ort_api, komelia_ort->ort_allocator, inference_data);
        return nullptr;
    }
    return result;
}

void komelia_ort_destroy(KomeliaOrt *komelia_ort) {
    komelia_ort->ort_api->ReleaseEnv(komelia_ort->ort_env);
    free(komelia_ort->data_dir);
    free(komelia_ort);
}

void komelia_ort_close_session(
    KomeliaOrt *komelia_ort,
    SessionData *session_data
) {
    release_session(komelia_ort->ort_api, session_data);
}
void komelia_ort_release_inference_result(
    KomeliaOrt *komelia_ort,
    InferenceResult *result
) {
    for (int i = 0; i < result->output_len; ++i) {
        komelia_ort->ort_api->ReleaseTensorTypeAndShapeInfo(result->out_tensors_info[i]);
        komelia_ort->ort_api->ReleaseValue(result->output_tensors[i]);
    }
    free(result->out_tensors_info);
    free(result->output_tensors);
    free(result);
}
