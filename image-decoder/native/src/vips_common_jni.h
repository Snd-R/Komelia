#include <vips/vips.h>
#include <jni.h>

void throw_jvm_vips_exception(JNIEnv *env, const char *message);

jobject to_jvm_image_data(JNIEnv *env, VipsImage *decoded);

VipsImage *from_jvm_handle(JNIEnv *env, jobject jvm_image);

jobject to_jvm_handle(JNIEnv *env, VipsImage *image, const unsigned char *external_source_buffer);

jobject to_jvm_handle_from_file(JNIEnv *env, VipsImage *image);
