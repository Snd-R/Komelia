#include <vips/vips.h>
#include <jni.h>

void komelia_vips_init();
void komelia_vips_shutdown();

jobject komelia_image_to_jvm_image_data(JNIEnv *env, VipsImage *decoded);

void throw_jvm_vips_exception(JNIEnv *env, const char *message);

VipsImage *from_jvm_handle(JNIEnv *env, jobject jvm_image);

jobject to_jvm_handle_from_file(JNIEnv *env, VipsImage *image);

jobject to_jvm_image_handle(JNIEnv *env, VipsImage *image, unsigned char* jvm_bytes);
