#include <vips/vips.h>
#include <jni.h>

void komelia_vips_init();

jobject komelia_vips_image_to_jvm(JNIEnv *env, VipsImage *decoded);

void throw_jvm_vips_exception(JNIEnv *env, const char *message);
