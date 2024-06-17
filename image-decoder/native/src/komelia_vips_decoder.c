#include "vips_jni.h"

JNIEXPORT void JNICALL Java_io_github_snd_1r_VipsDecoder_init() {
    komelia_vips_init();
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsDecoder_vipsDecode(
        JNIEnv *env,
        jobject this,
        jbyteArray encoded
) {
    jsize inputLen = (*env)->GetArrayLength(env, encoded);
    jbyte *inputBytes = (*env)->GetByteArrayElements(env, encoded, JNI_FALSE);

    VipsImage *decoded = vips_image_new_from_buffer((unsigned char *) inputBytes, inputLen, "", NULL);

    if (!decoded) {
        throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        return NULL;
    }


    jobject javaImage = komelia_vips_image_to_jvm(env, decoded);
    g_object_unref(decoded);
    (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
    return javaImage;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsDecoder_vipsDecodeAndResize(
        JNIEnv *env,
        jobject this,
        jbyteArray encoded,
        jint scaleWidth,
        jint scaleHeight,
        jboolean crop
) {
    jsize inputLen = (*env)->GetArrayLength(env, encoded);
    jbyte *inputBytes = (*env)->GetByteArrayElements(env, encoded, JNI_FALSE);

    VipsImage *decoded = NULL;

    if (crop) {
        vips_thumbnail_buffer((unsigned char *) inputBytes, inputLen, &decoded, scaleWidth,
                              "height", scaleHeight,
                              "crop", VIPS_INTERESTING_ENTROPY,
                              NULL
        );
    } else {
        vips_thumbnail_buffer((unsigned char *) inputBytes, inputLen, &decoded, scaleWidth,
                              "height", scaleHeight,
                              NULL
        );
    }
    if (!decoded) {
        throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        return NULL;
    }

    jobject javaImage = komelia_vips_image_to_jvm(env, decoded);
    g_object_unref(decoded);
    (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
    return javaImage;
}
