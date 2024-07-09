#ifndef KOMELIA_VIPS_COMMON_JNI_H
#define KOMELIA_VIPS_COMMON_JNI_H

#include <vips/vips.h>
#include <jni.h>

void komelia_throw_jvm_vips_exception(JNIEnv *env, const char *message);

jobject komelia_to_jvm_image_data(JNIEnv *env, VipsImage *decoded);

VipsImage *komelia_from_jvm_handle(JNIEnv *env, jobject jvm_image);

jobject komelia_to_jvm_handle(JNIEnv *env, VipsImage *image, const unsigned char *external_source_buffer);

#endif //KOMELIA_VIPS_COMMON_JNI_H
