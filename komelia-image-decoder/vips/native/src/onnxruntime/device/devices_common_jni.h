#ifndef KOMELIA_DEVICES_COMMON_JNI_H
#define KOMELIA_DEVICES_COMMON_JNI_H

#include <jni.h>

struct DeviceInfo {
  char *name;
  int id;
  size_t memory;
};

void throw_jvm_exception(JNIEnv *env, const char *message);

jobject create_jvm_list(JNIEnv *env);

void add_to_jvm_list(JNIEnv *env, jobject list, struct DeviceInfo device_info);

#endif // KOMELIA_DEVICES_COMMON_JNI_H