#ifndef KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H
#define KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H

#include <jni.h>

JNIEXPORT jobject JNICALL Java_snd_komelia_onnxruntime_JvmOnnxRuntime_enumerateDevices(
    JNIEnv *env,
    jobject this
);

struct DeviceInfo {
    char *name;
    int id;
    size_t memory;
};

static void throw_jvm_exception(
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
    struct DeviceInfo device_info
) {
    jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID array_list_add =
        (*env)->GetMethodID(env, array_list_class, "add", "(Ljava/lang/Object;)Z");

    jclass device_info_class = (*env)->FindClass(env, "snd/komelia/onnxruntime/DeviceInfo");
    jmethodID device_info_constructor =
        (*env)->GetMethodID(env, device_info_class, "<init>", "(Ljava/lang/String;IJ)V");

    jstring name = (*env)->NewStringUTF(env, device_info.name);
    jint id = device_info.id;
    jlong memory = (jlong)device_info.memory;
    jobject jvm_device_info =
        (*env)->NewObject(env, device_info_class, device_info_constructor, name, id, memory);
    (*env)->CallBooleanMethod(env, list, array_list_add, jvm_device_info);
}

#endif // KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H