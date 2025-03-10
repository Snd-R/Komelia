#include "vips_common_jni.h"
#include <stdint.h>

void komelia_throw_jvm_vips_exception_message(JNIEnv *env, const char *message) {
  (*env)->ThrowNew(env, (*env)->FindClass(env, "snd/komelia/image/VipsException"), message);
}
void komelia_throw_jvm_vips_exception(JNIEnv *env) {
  komelia_throw_jvm_vips_exception_message(env, vips_error_buffer());
  vips_error_clear();
}

jobject get_jvm_enum_type(JNIEnv *env, VipsImage *image) {
  VipsInterpretation interpretation = vips_image_get_interpretation(image);
  char *enum_field_name;
  switch (interpretation) {
  case VIPS_INTERPRETATION_B_W:
    enum_field_name = "GRAYSCALE_8";
    break;
  case VIPS_INTERPRETATION_sRGB:
    enum_field_name = "RGBA_8888";
    break;
  case VIPS_INTERPRETATION_HISTOGRAM:
    enum_field_name = "HISTOGRAM";
    break;
  default:
    komelia_throw_jvm_vips_exception_message(env, "unsupported vips interpretation");
    return nullptr;
  }

  jclass enum_class = (*env)->FindClass(env, "snd/komelia/image/ImageFormat");
  jfieldID enum_field =
      (*env)->GetStaticFieldID(env, enum_class, enum_field_name, "Lsnd/komelia/image/ImageFormat;");
  return (*env)->GetStaticObjectField(env, enum_class, enum_field);
}

int transform_to_supported_format(JNIEnv *env, VipsImage *in, VipsImage **transformed) {
  // convert to sRGB if not grayscale or if grayscale with alpha
  VipsInterpretation interpretation = vips_image_get_interpretation(in);
  if (interpretation == VIPS_INTERPRETATION_HISTOGRAM)
    return 0;

  int bands = vips_image_get_bands(in);
  bool is_grayscale_with_alpha = interpretation == VIPS_INTERPRETATION_B_W && bands != 1;
  bool is_not_srgb_or_grayscale =
      interpretation != VIPS_INTERPRETATION_sRGB && interpretation != VIPS_INTERPRETATION_B_W;

  if (is_grayscale_with_alpha || is_not_srgb_or_grayscale) {
    if (vips_colourspace(in, transformed, VIPS_INTERPRETATION_sRGB, nullptr) == 0) {
    } else {
      return -1;
    }
  }

  // add alpha channel to use 32 bits per pixel
  if (interpretation == VIPS_INTERPRETATION_sRGB && bands == 3) {
    VipsImage *with_alpha = nullptr;
    int vips_error;

    if (*transformed != nullptr) {
      vips_error = vips_addalpha(*transformed, &with_alpha, nullptr);
      g_object_unref(*transformed);
    } else {
      vips_error = vips_addalpha(in, &with_alpha, nullptr);
    }

    if (vips_error) {
      komelia_throw_jvm_vips_exception(env);
      return -1;
    }

    *transformed = with_alpha;
  }

  return 0;
}

VipsImage *komelia_from_jvm_handle(JNIEnv *env, jobject jvm_image) {
  jclass class = (*env)->GetObjectClass(env, jvm_image);
  jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
  VipsImage *image = (VipsImage *)(*env)->GetLongField(env, jvm_image, ptr_field);

  if (image == nullptr) {
    komelia_throw_jvm_vips_exception_message(env, "image was already closed\n");
    return nullptr;
  }

  return image;
}

jobject
komelia_to_jvm_handle(JNIEnv *env, VipsImage *image, const unsigned char *external_source_buffer) {
  VipsImage *transformed = nullptr;
  int transform_error = transform_to_supported_format(env, image, &transformed);
  if (transform_error) {
    return nullptr;
  }

  if (transformed == nullptr) {
    transformed = image;
  } else {
    g_object_unref(image);
  }

  jclass jvm_vips_class = (*env)->FindClass(env, "snd/komelia/image/VipsImage");
  jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class, "<init>",
                                              "(IIIIII[ILsnd/komelia/image/ImageFormat;JJ)V");

  jobject jvm_type_enum = get_jvm_enum_type(env, transformed);

  int height = vips_image_get_height(transformed);
  int width = vips_image_get_width(transformed);
  int bands = vips_image_get_bands(transformed);
  int pages_total = vips_image_get_n_pages(transformed);
  int page_height = vips_image_get_page_height(transformed);
  int pages_loaded = height / page_height;
  GType type = vips_image_get_typeof(transformed, "delay");
  jintArray jvm_delay_array = nullptr;
  if (type == VIPS_TYPE_ARRAY_INT) {
    int *delay = nullptr;
    int size = 0;
    vips_image_get_array_int(transformed, "delay", &delay, &size);
    jvm_delay_array = (*env)->NewIntArray(env, size);
    (*env)->SetIntArrayRegion(env, jvm_delay_array, 0, size, delay);
  }

  return (*env)->NewObject(env, jvm_vips_class, constructor, width, height, bands, pages_total,
                           page_height, pages_loaded, jvm_delay_array, jvm_type_enum,
                           (int64_t)external_source_buffer, (int64_t)transformed);
}
