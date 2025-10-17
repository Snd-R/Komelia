#ifndef KOMELIA_ONNXRUNTIME_H
#define KOMELIA_ONNXRUNTIME_H

#include "komelia_error.h"
#include <pthread.h>
#include <onnxruntime_c_api.h>
#include <vips/vips.h>

typedef enum {
    TENSOR_RT = 0,
    CUDA = 1,
    ROCm = 2,
    DML = 3,
    CPU = 4,
    WEBGPU = 5
} KomeliaOrtExecutionProvider;

typedef struct {
    int x;
    int y;
    int width;
    int height;
} KomeliaRect;

typedef struct {
    OrtSessionOptions *session_options;
    OrtSession *session;
    OrtMemoryInfo *memory_info;
    OrtRunOptions *run_options;
    const OrtTypeInfo *input_info;
    const OrtTensorTypeAndShapeInfo *input_tensor_info;
    ONNXTensorElementDataType input_data_type;

    KomeliaOrtExecutionProvider execution_provider;
    int device_id;
    char *model_path;
    // size_t input_count;
    size_t output_count;
} SessionData;

typedef struct {
    void *data;
    size_t data_len;
    int64_t *shape;
    size_t shape_len;
} KomeliaOrtInputTensor;

typedef struct {
    OrtTensorTypeAndShapeInfo **out_tensors_info;
    OrtValue **output_tensors;
    size_t output_len;
} InferenceResult;

typedef struct {
    const OrtApi *ort_api;
    OrtEnv *ort_env;
    OrtAllocator *ort_allocator;
    char *data_dir;
    pthread_mutex_t mutex;
} KomeliaOrt;

static void wrap_ort_error(
    const OrtApi *ort_api,
    OrtStatus *ort_status,
    const KomeliaOrtError komelia_error,
    GError **error
) {
    g_set_error_literal(
        error,
        KOMELIA_ORT_ERROR,
        komelia_error,
        ort_api->GetErrorMessage(ort_status)
    );
    ort_api->ReleaseStatus(ort_status);
}

static int
min(const int a,
    const int b) {
    return a < b ? a : b;
}

KomeliaOrt *komelia_ort_create(
    const char *data_dir,
    GError **error
);

void komelia_ort_destroy(KomeliaOrt *komelia_ort);

SessionData *komelia_ort_create_session(
    KomeliaOrt *komelia_ort,
    KomeliaOrtExecutionProvider execution_provider,
    int device_id,
    char *model_path,
    GError **error
);
void komelia_ort_close_session(
    KomeliaOrt *komelia_ort,
    SessionData *session_data
);

InferenceResult *komelia_ort_run_inference(
    KomeliaOrt *komelia_ort,
    SessionData *session,
    const KomeliaOrtInputTensor *input_tensor,
    GError **error
);

void komelia_ort_release_inference_result(
    KomeliaOrt *komelia_ort,
    InferenceResult *result
);

#endif // KOMELIA_ONNXRUNTIME_H
