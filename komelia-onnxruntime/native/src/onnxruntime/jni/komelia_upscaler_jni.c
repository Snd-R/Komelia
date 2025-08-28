#include "../komelia_onnxruntime.h"
#include "../komelia_ort_upscaler.h"
#include "komelia_onnxruntime_common_jni.h"
#include "vips_common_jni.h"
#include <jni.h>

static KomeliaOrtUpscaler *get_upscaler_from_jvm_handle(
    JNIEnv *env,
    jobject jvm_upscaler
) {
    jclass class = (*env)->GetObjectClass(env, jvm_upscaler);
    jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
    return (KomeliaOrtUpscaler *)(*env)->GetLongField(env, jvm_upscaler, ptr_field);
}

JNIEXPORT jlong JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_create(
    JNIEnv *env,
    jobject this,
    jlong komelia_ort
) {
    KomeliaOrt *ort = (KomeliaOrt *)komelia_ort;
    KomeliaOrtUpscaler *upscaler = komelia_ort_upscaler_create(ort);

    return (int64_t)upscaler;
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_destroy(
    JNIEnv *env,
    jobject this,
    jlong ptr
) {
    komelia_ort_upscaler_destroy((KomeliaOrtUpscaler *)ptr);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_setExecutionProvider(
    JNIEnv *env,
    jobject this,
    jint provider_ordinal,
    jint device_id
) {
    KomeliaOrtUpscaler *upscaler = get_upscaler_from_jvm_handle(env, this);
    komelia_ort_upscaler_set_execution_provider(upscaler, provider_ordinal, device_id);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_setModelPath(
    JNIEnv *env,
    jobject this,
    jstring model_path
) {
    KomeliaOrtUpscaler *upscaler = get_upscaler_from_jvm_handle(env, this);
    const char *model_path_chars = (*env)->GetStringUTFChars(env, model_path, nullptr);
    komelia_ort_upscaler_set_model_path(upscaler, model_path_chars);
    (*env)->ReleaseStringUTFChars(env, model_path, model_path_chars);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_setTileSize(
    JNIEnv *env,
    jobject this,
    jint tile_size
) {

    KomeliaOrtUpscaler *upscaler = get_upscaler_from_jvm_handle(env, this);
    komelia_ort_upscaler_set_tile_size(upscaler, tile_size);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_closeCurrentSession(
    JNIEnv *env,
    jobject this
) {
    KomeliaOrtUpscaler *upscaler = get_upscaler_from_jvm_handle(env, this);
    komelia_ort_upscaler_close_session(upscaler);
}
JNIEXPORT jobject JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntimeUpscaler_upscale(
    JNIEnv *env,
    jobject this,
    jobject jvmVipsImage
) {

    VipsImage *image = komelia_from_jvm_handle(env, jvmVipsImage);
    if (image == nullptr) {
        return nullptr;
    }

    GError *upscale_error = nullptr;
    KomeliaOrtUpscaler *upscaler = get_upscaler_from_jvm_handle(env, this);
    VipsImage *result = komelia_ort_upscale(upscaler, image, &upscale_error);
    if (upscale_error != nullptr) {
        throw_jvm_ort_exception(env, upscale_error->message);
        g_error_free(upscale_error);
        return nullptr;
    }

    return komelia_to_jvm_handle(env, result, nullptr);
}
