#include "vips_common_jni.h"

JNIEXPORT void JNICALL Java_snd_komelia_image_VipsImage_vipsInit() {
  VIPS_INIT("komelia");
  vips_cache_set_max(0);
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_decode(JNIEnv *env,
                                                                  jobject this,
                                                                  jbyteArray encoded) {
  jsize input_len = (*env)->GetArrayLength(env, encoded);
  jbyte *input_bytes = (*env)->GetByteArrayElements(env, encoded, nullptr);

  unsigned char *internal_buffer = malloc(input_len * sizeof(unsigned char));
  memcpy(internal_buffer, input_bytes, input_len);
  (*env)->ReleaseByteArrayElements(env, encoded, input_bytes, JNI_ABORT);

  VipsImage *decoded = vips_image_new_from_buffer(internal_buffer, input_len, "", nullptr);

  if (!decoded) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return nullptr;
  }

  jobject jvm_image = komelia_to_jvm_handle(env, decoded, internal_buffer);
  if (jvm_image == nullptr) {
    g_object_unref(decoded);
  }

  vips_thread_shutdown();
  return jvm_image;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_decodeFromFile(JNIEnv *env,
                                                                          jobject this,
                                                                          jstring path) {
  const char *path_chars = (*env)->GetStringUTFChars(env, path, nullptr);
  VipsImage *decoded = vips_image_new_from_file(path_chars, nullptr);
  (*env)->ReleaseStringUTFChars(env, path, path_chars);

  if (!decoded) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return nullptr;
  }

  vips_thread_shutdown();
  jobject jvm_handle = komelia_to_jvm_handle(env, decoded, nullptr);
  if (jvm_handle == nullptr) {
    g_object_unref(decoded);
  }
  return jvm_handle;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_thumbnail(
    JNIEnv *env, jobject this, jstring path, jint scaleWidth, jint scaleHeight, jboolean crop) {
  const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
  VipsImage *thumbnail = nullptr;
  if (crop) {
    vips_thumbnail(path_chars, &thumbnail, scaleWidth, "height", scaleHeight, "crop",
                   VIPS_INTERESTING_ENTROPY, nullptr);
  } else {
    vips_thumbnail(path_chars, &thumbnail, scaleWidth, "height", scaleHeight, nullptr);
  }
  (*env)->ReleaseStringUTFChars(env, path, path_chars);

  if (!thumbnail) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return nullptr;
  }

  vips_thread_shutdown();
  jobject jvm_handle = komelia_to_jvm_handle(env, thumbnail, nullptr);
  if (jvm_handle == nullptr) {
    g_object_unref(thumbnail);
  }
  return jvm_handle;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_thumbnailBuffer(JNIEnv *env,
                                                                           jobject this,
                                                                           jbyteArray encoded,
                                                                           jint scaleWidth,
                                                                           jint scaleHeight,
                                                                           jboolean crop) {
  jsize input_len = (*env)->GetArrayLength(env, encoded);
  jbyte *input_bytes = (*env)->GetByteArrayElements(env, encoded, nullptr);

  unsigned char *internal_buffer = malloc(input_len * sizeof(unsigned char));
  memcpy(internal_buffer, input_bytes, input_len);
  (*env)->ReleaseByteArrayElements(env, encoded, input_bytes, JNI_ABORT);

  VipsImage *thumbnail = nullptr;
  if (crop) {
    vips_thumbnail_buffer(internal_buffer, input_len, &thumbnail, scaleWidth, "height", scaleHeight,
                          "crop", VIPS_INTERESTING_ENTROPY, nullptr);
  } else {
    vips_thumbnail_buffer(internal_buffer, input_len, &thumbnail, scaleWidth, "height", scaleHeight,
                          nullptr);
  }

  if (!thumbnail) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    free(internal_buffer);
    return nullptr;
  }

  vips_thread_shutdown();
  jobject jvm_handle = komelia_to_jvm_handle(env, thumbnail, internal_buffer);
  if (jvm_handle == nullptr) {
    g_object_unref(thumbnail);
    free(internal_buffer);
  }
  return jvm_handle;
}

JNIEXPORT void JNICALL Java_snd_komelia_image_VipsImage_encodeToFile(JNIEnv *env,
                                                                     jobject this,
                                                                     jstring path) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return;

  const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
  int write_error = vips_image_write_to_file(image, path_chars, nullptr);
  (*env)->ReleaseStringUTFChars(env, path, path_chars);

  if (write_error) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return;
  }

  vips_thread_shutdown();
}

JNIEXPORT void JNICALL Java_snd_komelia_image_VipsImage_encodeToFilePng(JNIEnv *env,
                                                                        jobject this,
                                                                        jstring path) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return;

  const char *path_chars = (*env)->GetStringUTFChars(env, path, 0);
  int write_error = vips_pngsave(image, path_chars, nullptr);
  (*env)->ReleaseStringUTFChars(env, path, path_chars);

  if (write_error) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return;
  }

  vips_thread_shutdown();
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_getDimensions(JNIEnv *env,
                                                                         jobject this,
                                                                         jbyteArray encoded) {
  jsize input_len = (*env)->GetArrayLength(env, encoded);
  jbyte *input_bytes = (*env)->GetByteArrayElements(env, encoded, nullptr);

  VipsImage *decoded = vips_image_new_from_buffer(input_bytes, input_len, "", nullptr);

  if (!decoded) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return nullptr;
  }

  jclass jvm_vips_class = (*env)->FindClass(env, "snd/komelia/image/ImageDimensions");
  jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class, "<init>", "(III)V");
  jobject jvm_dimensions =
      (*env)->NewObject(env, jvm_vips_class, constructor, vips_image_get_width(decoded),
                        vips_image_get_height(decoded), vips_image_get_bands(decoded));
  g_object_unref(decoded);
  (*env)->ReleaseByteArrayElements(env, encoded, input_bytes, JNI_ABORT);

  vips_thread_shutdown();
  return jvm_dimensions;
}

JNIEXPORT jbyteArray JNICALL Java_snd_komelia_image_VipsImage_getBytes(JNIEnv *env, jobject this) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return nullptr;

  unsigned char *data = (unsigned char *)vips_image_get_data(image);
  if (data == nullptr) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    return nullptr;
  }

  int bands = vips_image_get_bands(image);
  int width = vips_image_get_width(image);
  int height = vips_image_get_height(image);
  int size = bands * width * height;

  jbyteArray java_bytes = (*env)->NewByteArray(env, size);
  (*env)->SetByteArrayRegion(env, java_bytes, 0, size, (signed char *)data);

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

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_extractArea(JNIEnv *env,
                                                                       jobject this,
                                                                       jobject rect) {
  VipsImage *input_image = komelia_from_jvm_handle(env, this);
  if (input_image == nullptr) {
    return nullptr;
  }

  VipsRect vips_rect = to_vips_rect(env, rect);
  VipsImage *extracted_image = nullptr;
  int extract_error = vips_extract_area(input_image, &extracted_image, vips_rect.left,
                                        vips_rect.top, vips_rect.width, vips_rect.height, nullptr);
  if (extract_error) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    return nullptr;
  }

  jobject jvm_image = komelia_to_jvm_handle(env, extracted_image, nullptr);
  return jvm_image;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_resize(
    JNIEnv *env, jobject this, jint scaleWidth, jint scaleHeight, jboolean crop) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return nullptr;

  VipsImage *resized = nullptr;

  if (crop) {
    vips_thumbnail_image(image, &resized, scaleWidth, "height", scaleHeight, "crop",
                         VIPS_INTERESTING_ENTROPY, "linear", 1, nullptr);
  } else {
    vips_thumbnail_image(image, &resized, scaleWidth, "height", scaleHeight, "linear", 1, nullptr);
  }

  if (resized == nullptr) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return nullptr;
  }

  jobject jvm_image = komelia_to_jvm_handle(env, resized, nullptr);
  if (jvm_image == nullptr) {
    g_object_unref(resized);
  }
  vips_thread_shutdown();
  return jvm_image;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_shrink(JNIEnv *env,
                                                                  jobject this,
                                                                  jdouble factor) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return nullptr;

  VipsImage *resized = nullptr;
  if (vips_shrink(image, &resized, factor, factor, nullptr) != 0) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    vips_thread_shutdown();
    return nullptr;
  }

  jobject jvm_image = komelia_to_jvm_handle(env, resized, nullptr);
  if (jvm_image == nullptr) {
    g_object_unref(resized);
  }
  vips_thread_shutdown();
  return jvm_image;
}

jobject jvm_rect(JNIEnv *env, int left, int top, int width, int height) {
  jclass jvm_vips_class = (*env)->FindClass(env, "snd/komelia/image/ImageRect");
  jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class, "<init>", "(IIII)V");
  return (*env)->NewObject(env, jvm_vips_class, constructor, left, top, width + left, height + top);
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_findTrim(JNIEnv *env, jobject this) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return nullptr;

  int left, top, width, height;
  vips_find_trim(image, &left, &top, &width, &height, "threshold", 50.0, "line_art", 0, nullptr);
  return jvm_rect(env, left, top, width, height);
}

JNIEXPORT void JNICALL Java_snd_komelia_image_VipsImage_gObjectUnref(JNIEnv *env,
                                                                     jobject this,
                                                                     jlong ptr) {
  g_object_unref((VipsImage *)ptr);
}

JNIEXPORT void JNICALL Java_snd_komelia_image_VipsImage_free(JNIEnv *env,
                                                             jobject this,
                                                             jlong bytes) {
  free((void *)bytes);
}

jobject create_jvm_list(JNIEnv *env) {
  jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
  jmethodID array_list_constructor = (*env)->GetMethodID(env, array_list_class, "<init>", "()V");
  return (*env)->NewObject(env, array_list_class, array_list_constructor);
}

void add_to_jvm_list(JNIEnv *env, jobject list, jobject new_item) {
  jclass array_list_class = (*env)->FindClass(env, "java/util/ArrayList");
  jmethodID array_list_add =
      (*env)->GetMethodID(env, array_list_class, "add", "(Ljava/lang/Object;)Z");
  (*env)->CallBooleanMethod(env, list, array_list_add, new_item);
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_makeHistogram(JNIEnv *env,
                                                                         jobject this) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  if (image == nullptr)
    return nullptr;
  VipsImage *histogram = nullptr;
  int bands = vips_image_get_bands(image);
  if (bands != 1 && bands != 4) {
    komelia_throw_jvm_vips_exception(env, "unsupported number of image bands");
    return nullptr;
  }

  vips_hist_find(image, &histogram, "band", -1, nullptr);

  if (histogram == nullptr) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    return nullptr;
  }

  VipsImage *histogram_normalized = nullptr;
  vips_hist_norm(histogram, &histogram_normalized, nullptr);
  g_object_unref(histogram);

  if (histogram_normalized == nullptr) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    return nullptr;
  }

  jobject jvm_image = komelia_to_jvm_handle(env, histogram_normalized, nullptr);
  if (jvm_image == nullptr) {
    g_object_unref(histogram_normalized);
  }

  return jvm_image;
}

JNIEXPORT jobject JNICALL Java_snd_komelia_image_VipsImage_mapLookupTable(JNIEnv *env,
                                                                          jobject this,
                                                                          jarray jvm_lut) {
  VipsImage *image = komelia_from_jvm_handle(env, this);
  VipsImage *lut = nullptr;
  VipsImage *transformed = nullptr;
  if (image == nullptr)
    return nullptr;

  jsize input_len = (*env)->GetArrayLength(env, jvm_lut);
  jbyte *input_bytes = (*env)->GetByteArrayElements(env, jvm_lut, nullptr);

  int bands = vips_image_get_bands(image);
  lut = vips_image_new_from_memory(input_bytes, input_len, 256, 1, bands, VIPS_FORMAT_UCHAR);
  vips_maplut(image, &transformed, lut, nullptr);
  g_object_unref(lut);
  (*env)->ReleaseByteArrayElements(env, jvm_lut, input_bytes, JNI_ABORT);

  if (transformed == nullptr) {
    komelia_throw_jvm_vips_exception(env, vips_error_buffer());
    vips_error_clear();
    return nullptr;
  }

  jobject jvm_image = komelia_to_jvm_handle(env, transformed, nullptr);
  if (jvm_image == nullptr) {
    g_object_unref(transformed);
  }

  return jvm_image;
}