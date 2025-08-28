#ifndef KOMELIA_KOMELIA_ONNXRUNTIME_COMMON_JNI_H
#define KOMELIA_KOMELIA_ONNXRUNTIME_COMMON_JNI_H
#include <jni.h>

static void throw_jvm_ort_exception(
    JNIEnv *env,
    const char *message
) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "snd/komelia/onnxruntime/OnnxRuntimeException"), message);
}

static jobject create_jvm_list(JNIEnv *env) {
    jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID array_list_constructor = (*env)->GetMethodID(env, array_list_class, "<init>", "()V");
    return (*env)->NewObject(env, array_list_class, array_list_constructor);
}

static void add_to_jvm_list(
    JNIEnv *env,
    jobject list,
    jobject list_entry
) {
    jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID array_list_add =
        (*env)->GetMethodID(env, array_list_class, "add", "(Ljava/lang/Object;)Z");
  (*env)->CallBooleanMethod(env, list, array_list_add, list_entry);
}

#endif // KOMELIA_KOMELIA_ONNXRUNTIME_COMMON_JNI_H
