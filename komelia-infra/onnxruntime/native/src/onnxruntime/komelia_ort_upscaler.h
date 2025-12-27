#ifndef KOMELIA_ORT_UPSCALER
#define KOMELIA_ORT_UPSCALER

#include <pthread.h>
#include "komelia_onnxruntime.h"

typedef struct {
    KomeliaOrt *komelia_ort;
    KomeliaOrtExecutionProvider execution_provider;
    int device_id;
    char *model_path;
    SessionData *session;
    int tile_size;
    pthread_mutex_t mutex;
} KomeliaOrtUpscaler;

KomeliaOrtUpscaler *komelia_ort_upscaler_create(KomeliaOrt *ort);

void komelia_ort_upscaler_destroy(KomeliaOrtUpscaler *upscaler);

void komelia_ort_upscaler_set_tile_size(
    KomeliaOrtUpscaler *upscaler,
    int size
);

void komelia_ort_upscaler_set_model_path(
    KomeliaOrtUpscaler *upscaler,
    const char *path
);

void komelia_ort_upscaler_set_execution_provider(
    KomeliaOrtUpscaler *upscaler,
    KomeliaOrtExecutionProvider execution_provider,
    int device_id
);

void komelia_ort_upscaler_close_session(KomeliaOrtUpscaler *upscaler);

VipsImage *komelia_ort_upscale(
    KomeliaOrtUpscaler *upscaler,
    VipsImage *image,
    GError **error
);

#endif // KOMELIA_ORT_UPSCALER
