#include "../komelia_onnxruntime.h"
#include "komelia_onnxruntime_common_jni.h"
#include "vips_common_jni.h"
#include <jni.h>

JNIEXPORT jobject JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntime_create(
    JNIEnv *env,
    jobject this
) {
    GError *error = nullptr;
    KomeliaOrt *ort = komelia_ort_create(&error);
    if (error != nullptr) {
        throw_jvm_ort_exception(env, error->message);
        g_error_free(error);
        return nullptr;
    }

    jclass jvm_ort_class = (*env)->FindClass(env, "snd/komelia/onnxruntime/JvmOnnxRuntime");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_ort_class, "<init>", "(J)V");
    return (*env)->NewObject(env, jvm_ort_class, constructor, (int64_t)ort);
}

JNIEXPORT void JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntime_destroy(
    JNIEnv *env,
    jobject this,
    jlong ptr
) {
    komelia_ort_destroy((KomeliaOrt *)ptr);
}