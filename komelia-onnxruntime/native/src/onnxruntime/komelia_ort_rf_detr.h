#ifndef KOMELIA_ORT_RF_DETR
#define KOMELIA_ORT_RF_DETR

#include "komelia_onnxruntime.h"
#include <vips/vips.h>

typedef struct {
    KomeliaOrt *komelia_ort;
    KomeliaOrtExecutionProvider execution_provider;
    int device_id;
    char *model_path;
    SessionData *session;
    pthread_mutex_t mutex;
} KomeliaRfDetr;

typedef struct {
    int class_id;
    float confidence;
    KomeliaRect *box;
} KomeliaRfDetrResult;

typedef struct {
    KomeliaRfDetrResult **data;
    int results_size;
} KomeliaRfDetrResults;

KomeliaRfDetr *komelia_ort_rfdetr_create(KomeliaOrt *ort);

void komelia_ort_rfdetr_destroy(KomeliaRfDetr *rf_detr);

void komelia_ort_rfdetr_set_model_path(
    KomeliaRfDetr *rf_detr,
    const char *path
);

void komelia_ort_rfdetr_set_execution_provider(
    KomeliaRfDetr *rf_detr,
    KomeliaOrtExecutionProvider execution_provider,
    int device_id
);

KomeliaRfDetrResults *komelia_ort_rfdetr(
    KomeliaRfDetr *rf_detr,
    VipsImage *image,
    GError **error
);

void komelia_ort_rfdetr_close_session(KomeliaRfDetr *rf_detr);

void komelia_ort_rfdetr_release_result(
    KomeliaRfDetr *rf_detr,
    KomeliaRfDetrResults *result
);

#endif // KOMELIA_ORT_RF_DETR
