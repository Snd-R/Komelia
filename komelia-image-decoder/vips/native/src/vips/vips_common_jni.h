#ifndef KOMELIA_VIPS_COMMON_JNI_H
#define KOMELIA_VIPS_COMMON_JNI_H

#include <jni.h>
#include <vips/vips.h>

JNIEXPORT void komelia_throw_jvm_vips_exception_message(JNIEnv *env, const char *message);

JNIEXPORT void komelia_throw_jvm_vips_exception(JNIEnv *env);

JNIEXPORT jobject komelia_to_jvm_image_data(JNIEnv *env, VipsImage *decoded);

JNIEXPORT VipsImage *komelia_from_jvm_handle(JNIEnv *env, jobject jvm_image);

JNIEXPORT jobject komelia_to_jvm_handle(JNIEnv *env,
                                       VipsImage *image,
                                       const unsigned char *external_source_buffer);

#endif // KOMELIA_VIPS_COMMON_JNI_H