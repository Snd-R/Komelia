#include "devices_common_jni.h"

void throw_jvm_exception(JNIEnv *env, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "snd/komelia/image/OnnxRuntimeUpscaler$OrtException"), message);
}

jobject create_jvm_list(JNIEnv *env) {
    jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID array_list_constructor = (*env)->GetMethodID(env, array_list_class, "<init>", "()V");
    return (*env)->NewObject(env, array_list_class, array_list_constructor);

}

void add_to_jvm_list(JNIEnv *env, jobject list, struct DeviceInfo device_info) {
    jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
    jmethodID array_list_add = (*env)->GetMethodID(env, array_list_class, "add", "(Ljava/lang/Object;)Z");

    jclass device_info_class = (*env)->FindClass(env, "snd/komelia/image/OnnxRuntimeUpscaler$DeviceInfo");
    jmethodID device_info_constructor = (*env)->GetMethodID(env, device_info_class, "<init>",
                                                            "(Ljava/lang/String;IJ)V");

    jstring name = (*env)->NewStringUTF(env, device_info.name);
    jint id = (jint) device_info.id;
    jlong memory = (jlong) device_info.memory;
    jobject jvm_device_info = (*env)->NewObject(env, device_info_class, device_info_constructor, name, id, memory);
    (*env)->CallBooleanMethod(env, list, array_list_add, jvm_device_info);
}
