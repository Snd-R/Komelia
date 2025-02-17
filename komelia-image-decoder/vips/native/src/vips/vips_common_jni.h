#ifndef KOMELIA_VIPS_COMMON_JNI_H
#define KOMELIA_VIPS_COMMON_JNI_H

#include <jni.h>
#include <vips/vips.h>

#if defined(__cplusplus)
#define EXTERN_C extern "C"
#else
#define EXTERN_C extern
#endif

EXTERN_C void komelia_throw_jvm_vips_exception_message(JNIEnv *env, const char *message);

EXTERN_C void komelia_throw_jvm_vips_exception(JNIEnv *env);

EXTERN_C jobject komelia_to_jvm_image_data(JNIEnv *env, VipsImage *decoded);

EXTERN_C VipsImage *komelia_from_jvm_handle(JNIEnv *env, jobject jvm_image);

EXTERN_C jobject komelia_to_jvm_handle(JNIEnv *env,
                                       VipsImage *image,
                                       const unsigned char *external_source_buffer);

#endif // KOMELIA_VIPS_COMMON_JNI_H