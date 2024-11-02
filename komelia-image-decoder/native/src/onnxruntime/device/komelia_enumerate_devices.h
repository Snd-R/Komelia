#ifndef KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H
#define KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H

#include <jni.h>

JNIEXPORT jobject JNICALL Java_io_github_snd_1r_OnnxRuntimeUpscaler_enumerateDevices(JNIEnv *env, jobject this);

#endif //KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H
