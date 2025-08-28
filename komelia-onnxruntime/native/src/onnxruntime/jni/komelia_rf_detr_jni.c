#include "../komelia_onnxruntime.h"
#include "../komelia_ort_rf_detr.h"
#include "komelia_onnxruntime_common_jni.h"
#include "vips_common_jni.h"
#include <jni.h>

static KomeliaRfDetr *get_rf_detr_from_jvm_handle(
    JNIEnv *env,
    jobject jvm_rf_detr
) {
    jclass class = (*env)->GetObjectClass(env, jvm_rf_detr);
    jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
    return (KomeliaRfDetr *)(*env)->GetLongField(env, jvm_rf_detr, ptr_field);
}

JNIEXPORT jlong JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeRfDetr_create(
    JNIEnv *env,
    jobject this,
    jlong komelia_ort
) {
    KomeliaOrt *ort = (KomeliaOrt *)komelia_ort;
    KomeliaRfDetr *rf_detr = komelia_ort_rfdetr_create(ort);
    return (int64_t) rf_detr ;
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeRfDetr_destroy(
    JNIEnv *env,
    jobject this,
    jlong ptr
) {
    komelia_ort_rfdetr_destroy((KomeliaRfDetr *)ptr);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeRfDetr_setExecutionProvider(
    JNIEnv *env,
    jobject this,
    jint provider_ordinal,
    jint device_id
) {
    KomeliaRfDetr *rf_detr = get_rf_detr_from_jvm_handle(env, this);
    komelia_ort_rfdetr_set_execution_provider(rf_detr, provider_ordinal, device_id);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeRfDetr_setModelPath(
    JNIEnv *env,
    jobject this,
    jstring model_path
) {
    KomeliaRfDetr *rf_detr = get_rf_detr_from_jvm_handle(env, this);
    const char *model_path_chars = (*env)->GetStringUTFChars(env, model_path, nullptr);
    komelia_ort_rfdetr_set_model_path(rf_detr, model_path_chars);
    (*env)->ReleaseStringUTFChars(env, model_path, model_path_chars);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeRfDetr_closeCurrentSession(
    JNIEnv *env,
    jobject this
) {
    KomeliaRfDetr *rf_detr = get_rf_detr_from_jvm_handle(env, this);
    komelia_ort_rfdetr_close_session(rf_detr);
}

static jobject create_jvm_image_rect(
    JNIEnv *env,
    int left,
    int top,
    int right,
    int bottom
) {
    jclass class = (*env)->FindClass(env, "snd/komelia/image/ImageRect");
    jmethodID constructor = (*env)->GetMethodID(env, class, "<init>", "(IIII)V");
    return (*env)->NewObject(env, class, constructor, left, top, right, bottom);
}
JNIEXPORT jobject JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeRfDetr_detect(
    JNIEnv *env,
    jobject this,
    jobject jvmVipsImage
) {
    VipsImage *image = komelia_from_jvm_handle(env, jvmVipsImage);
    if (image == nullptr) {
        return nullptr;
    }

    GError *detect_error = nullptr;
    KomeliaRfDetr *rf_detr = get_rf_detr_from_jvm_handle(env, this);
    KomeliaRfDetrResults *results = komelia_ort_rfdetr(rf_detr, image, &detect_error);
    if (detect_error != nullptr) {
        throw_jvm_ort_exception(env, detect_error->message);
        g_error_free(detect_error);
        return nullptr;
    }

    jclass jvm_result_class =
        (*env)->FindClass(env, "snd/komelia/onnxruntime/OnnxRuntimeRfDetr$DetectResult");
    jmethodID jvm_result_constructor =
        (*env)->GetMethodID(env, jvm_result_class, "<init>", "(IFLsnd/komelia/image/ImageRect;)V");
    jobject jvm_list = create_jvm_list(env);
    for (int i = 0; i < results->results_size; ++i) {
        KomeliaRfDetrResult *result = results->data[i];
        jobject jvm_result = (*env)->NewObject(
            env,
            jvm_result_class,
            jvm_result_constructor,
            result->class_id,
            result->confidence,
            create_jvm_image_rect(
                env,
                result->box->x,
                result->box->y,
                result->box->width,
                result->box->height
            )
        );
        add_to_jvm_list(env, jvm_list, jvm_result);
    }

    komelia_ort_rfdetr_release_result(rf_detr, results);
    return jvm_list;
}
