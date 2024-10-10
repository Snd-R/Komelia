#include "vips_common_jni.h"

JNIEXPORT void JNICALL Java_io_github_snd_1r_VipsImage_vipsInit() {
    VIPS_INIT("komelia");
    vips_concurrency_set(2);
    vips_cache_set_max(0);
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_decode(
    JNIEnv *env,
    jobject this,
    jbyteArray encoded
) {
    jsize input_len = (*env)->GetArrayLength(env, encoded);
    jbyte *input_bytes = (*env)->GetByteArrayElements(env, encoded, NULL);

    unsigned char *internal_buffer = malloc(input_len * sizeof(unsigned char));
    memcpy(internal_buffer, input_bytes, input_len);
    (*env)->ReleaseByteArrayElements(env, encoded, input_bytes, JNI_ABORT);

    VipsImage *decoded = vips_image_new_from_buffer(internal_buffer, input_len, "", NULL);

    if (!decoded) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return NULL;
    }

    jobject jvm_image = komelia_to_jvm_handle(env, decoded, internal_buffer);
    if (jvm_image == NULL) { g_object_unref(decoded); }

    vips_thread_shutdown();
    return jvm_image;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_decodeFromFile(
    JNIEnv *env,
    jobject this,
    jstring path
) {
    const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
    VipsImage *decoded = vips_image_new_from_file(path_chars, NULL);
    (*env)->ReleaseStringUTFChars(env, path, path_chars);

    if (!decoded) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return NULL;
    }

    vips_thread_shutdown();
    jobject jvm_handle = komelia_to_jvm_handle(env, decoded, NULL);
    if (jvm_handle == NULL) { g_object_unref(decoded); }
    return jvm_handle;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_thumbnail(
    JNIEnv *env,
    jobject this,
    jstring path,
    jint scaleWidth,
    jint scaleHeight,
    jboolean crop
) {
    const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
    VipsImage *thumbnail = NULL;
    if (crop) {
        vips_thumbnail(path_chars, &thumbnail, scaleWidth,
                       "height", scaleHeight,
                       "crop", VIPS_INTERESTING_ENTROPY,
                       NULL
        );
    } else {
        vips_thumbnail(path_chars, &thumbnail, scaleWidth, "height", scaleHeight, NULL);
    }
    (*env)->ReleaseStringUTFChars(env, path, path_chars);

    if (!thumbnail) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return NULL;
    }

    vips_thread_shutdown();
    jobject jvm_handle = komelia_to_jvm_handle(env, thumbnail, NULL);
    if (jvm_handle == NULL) { g_object_unref(thumbnail); }
    return jvm_handle;
}

JNIEXPORT void JNICALL
Java_io_github_snd_1r_VipsImage_encodeToFile(
    JNIEnv *env,
    jobject this,
    jstring path
) {
    VipsImage *image = komelia_from_jvm_handle(env, this);
    if (image == NULL) return;

    const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
    int write_error = vips_image_write_to_file(image, path_chars, NULL);
    (*env)->ReleaseStringUTFChars(env, path, path_chars);

    if (write_error) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return;
    }

    vips_thread_shutdown();
}

JNIEXPORT void JNICALL
Java_io_github_snd_1r_VipsImage_encodeToFilePng(
    JNIEnv *env,
    jobject this,
    jstring path
) {
    VipsImage *image = komelia_from_jvm_handle(env, this);
    if (image == NULL) return;

    const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
    int write_error = vips_pngsave(image, path_chars, NULL);
    (*env)->ReleaseStringUTFChars(env, path, path_chars);

    if (write_error) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return;
    }

    vips_thread_shutdown();
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_getDimensions(
    JNIEnv *env,
    jobject this,
    jbyteArray encoded
) {
    jsize input_len = (*env)->GetArrayLength(env, encoded);
    jbyte *input_bytes = (*env)->GetByteArrayElements(env, encoded, NULL);

    VipsImage *decoded = vips_image_new_from_buffer(input_bytes, input_len, "", NULL);

    if (!decoded) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return NULL;
    }

    jclass jvm_vips_class = (*env)->FindClass(env, "io/github/snd_r/VipsImageDimensions");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class,
                                                "<init>", "(III)V");
    jobject jvm_dimensions = (*env)->NewObject(env, jvm_vips_class, constructor,
                                               vips_image_get_width(decoded),
                                               vips_image_get_height(decoded),
                                               vips_image_get_bands(decoded)
    );
    g_object_unref(decoded);
    (*env)->ReleaseByteArrayElements(env, encoded, input_bytes, JNI_ABORT);

    vips_thread_shutdown();
    return jvm_dimensions;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_decodeAndGet(
    JNIEnv *env,
    jobject this,
    jbyteArray encoded
) {
    jsize inputLen = (*env)->GetArrayLength(env, encoded);
    jbyte *inputBytes = (*env)->GetByteArrayElements(env, encoded, JNI_FALSE);

    VipsImage *decoded = vips_image_new_from_buffer((unsigned char *) inputBytes, inputLen, "", NULL);

    if (!decoded) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        vips_thread_shutdown();
        return NULL;
    }


    jobject javaImage = komelia_to_jvm_image_data(env, decoded);
    g_object_unref(decoded);
    (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
    vips_thread_shutdown();
    return javaImage;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_decodeResizeAndGet(
    JNIEnv *env,
    jobject this,
    jbyteArray encoded,
    jint scaleWidth,
    jint scaleHeight,
    jboolean crop
) {
    jsize inputLen = (*env)->GetArrayLength(env, encoded);
    jbyte *inputBytes = (*env)->GetByteArrayElements(env, encoded, 0);

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
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
        vips_thread_shutdown();
        return NULL;
    }

    jobject javaImage = komelia_to_jvm_image_data(env, decoded);
    g_object_unref(decoded);
    (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
    vips_thread_shutdown();
    return javaImage;
}

JNIEXPORT jbyteArray JNICALL
Java_io_github_snd_1r_VipsImage_getBytes(JNIEnv *env, jobject this) {
    VipsImage *image = komelia_from_jvm_handle(env, this);
    if (image == NULL) return NULL;

    unsigned char *data = (unsigned char *) vips_image_get_data(image);

    int bands = vips_image_get_bands(image);
    int width = vips_image_get_width(image);
    int height = vips_image_get_height(image);
    int size = bands * width * height;

    jbyteArray java_bytes = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, java_bytes, 0, size, (signed char *) data);

    vips_thread_shutdown();
    return java_bytes;
}

VipsRect to_vips_rect(JNIEnv *env, jobject jvm_rect) {
    jclass class = (*env)->GetObjectClass(env, jvm_rect);
    jfieldID left = (*env)->GetFieldID(env, class, "left", "I");
    jfieldID top = (*env)->GetFieldID(env, class, "top", "I");
    jfieldID right = (*env)->GetFieldID(env, class, "right", "I");
    jfieldID bottom = (*env)->GetFieldID(env, class, "bottom", "I");

    VipsRect vipsRect;
    vipsRect.left = (*env)->GetIntField(env, jvm_rect, left);
    vipsRect.top = (*env)->GetIntField(env, jvm_rect, top);
    vipsRect.width = (*env)->GetIntField(env, jvm_rect, right) - vipsRect.left;
    vipsRect.height = (*env)->GetIntField(env, jvm_rect, bottom) - vipsRect.top;

    return vipsRect;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_getRegion(JNIEnv *env, jobject this, jobject rect) {
    VipsImage *input_image = komelia_from_jvm_handle(env, this);
    if (input_image == NULL) { return NULL; }

    VipsRegion *region = vips_region_new(input_image);
    VipsRect vips_rect = to_vips_rect(env, rect);

    int bands = vips_image_get_bands(input_image);

    int prepare_error = vips_region_prepare(region, &vips_rect);
    if (prepare_error) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        g_object_unref(region);
        return NULL;
    }

    size_t region_size = 0;
    // using region directly sometimes causes image corruption or only loads part of the region
    // copy entire region instead
    VipsPel *region_data = vips_region_fetch(region,
                                             vips_rect.left, vips_rect.top,
                                             vips_rect.width, vips_rect.height,
                                             &region_size
    );
    VipsImage *memory_image = vips_image_new_from_memory(
        //                VIPS_REGION_ADDR_TOPLEFT(region),
        region_data,
        region_size,
        vips_rect.width,
        vips_rect.height,
        bands,
        VIPS_FORMAT_UCHAR
    );
    if (!memory_image) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        g_object_unref(region);
        g_free(region_data);
        vips_thread_shutdown();
        return NULL;
    }
    VipsImage *region_image = NULL;
    vips_copy(memory_image, &region_image,
              "bands", vips_image_get_bands(input_image),
              "format", vips_image_get_format(input_image),
              "coding", vips_image_get_coding(input_image),
              "interpretation", vips_image_get_interpretation(input_image),
              NULL
    );
    if (!region_image) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        g_object_unref(memory_image);
        g_object_unref(region);
        g_free(region_data);
        vips_thread_shutdown();
        return NULL;
    }

    jobject jvm_image = komelia_to_jvm_handle(env, region_image, region_data);
    g_object_unref(memory_image);
    g_object_unref(region);
    vips_thread_shutdown();
    return jvm_image;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_resize(
    JNIEnv *env,
    jobject this,
    jint scaleWidth,
    jint scaleHeight,
    jboolean crop
) {
    VipsImage *image = komelia_from_jvm_handle(env, this);
    if (image == NULL) return NULL;

    VipsImage *resized = NULL;

    if (crop) {
        vips_thumbnail_image(image, &resized, scaleWidth,
                             "height", scaleHeight,
                             "crop", VIPS_INTERESTING_ENTROPY,
                             NULL
        );
    } else {
        vips_thumbnail_image(image, &resized, scaleWidth,
                             "height", scaleHeight,
                             NULL
        );
    }

    if (resized == NULL) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return NULL;
    }


    jobject jvm_image = komelia_to_jvm_handle(env, resized, NULL);
    if (jvm_image == NULL) { g_object_unref(resized); }
    vips_thread_shutdown();
    return jvm_image;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_shrink(
    JNIEnv *env,
    jobject this,
    jdouble factor
) {
    VipsImage *image = komelia_from_jvm_handle(env, this);
    if (image == NULL) return NULL;

    VipsImage *resized = NULL;
    if (vips_shrink(image, &resized, factor, factor, NULL) != 0) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        vips_thread_shutdown();
        return NULL;
    }

    jobject jvm_image = komelia_to_jvm_handle(env, resized, NULL);
    if (jvm_image == NULL) { g_object_unref(resized); }
    vips_thread_shutdown();
    return jvm_image;
}

jobject jvm_rect(JNIEnv *env, int left, int top, int width, int height) {
    jclass jvm_vips_class = (*env)->FindClass(env, "io/github/snd_r/ImageRect");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class, "<init>", "(IIII)V");
    return (*env)->NewObject(env, jvm_vips_class, constructor, left, top, width + left, height + top);
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsImage_findTrim(JNIEnv *env, jobject this) {
    VipsImage *image = komelia_from_jvm_handle(env, this);
    if (image == NULL) return NULL;

    int left, top, width, height;
    vips_find_trim(image, &left, &top, &width, &height, "threshold", 50.0, "line_art", 1, NULL);
    return jvm_rect(env, left, top, width, height);
}

JNIEXPORT void JNICALL
Java_io_github_snd_1r_VipsPointer_gObjectUnref(JNIEnv *env, jobject this, jlong ptr) {
    g_object_unref((VipsImage *) ptr);
}

JNIEXPORT void JNICALL
Java_io_github_snd_1r_VipsPointer_free(JNIEnv *env, jobject this, jlong bytes) {
    free((void *) bytes);
}
