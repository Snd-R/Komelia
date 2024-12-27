#ifndef KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H
#define KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H

#include <jni.h>

JNIEXPORT jobject JNICALL Java_snd_komelia_image_OnnxRuntimeUpscaler_enumerateDevices(JNIEnv *env, jobject this);

#endif //KOMELIA_KOMELIA_ENUMERATE_GPU_DEVICES_H
