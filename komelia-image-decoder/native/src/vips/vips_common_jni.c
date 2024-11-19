#include "vips_common_jni.h"
#include <stdint.h>
#include <stdbool.h>

void komelia_throw_jvm_vips_exception(JNIEnv *env, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "io/github/snd_r/VipsException"), message);
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
        default:
            komelia_throw_jvm_vips_exception(env, "unsupported vips interpretation");
            return NULL;
    }

    jclass enum_class = (*env)->FindClass(env, "io/github/snd_r/ImageFormat");
    jfieldID enum_field = (*env)->GetStaticFieldID(env, enum_class, enum_field_name, "Lio/github/snd_r/ImageFormat;");
    return (*env)->GetStaticObjectField(env, enum_class, enum_field);
}

int transform_to_supported_format(JNIEnv *env, VipsImage *in, VipsImage **transformed) {
    // convert to sRGB if not grayscale or if grayscale with alpha
    VipsInterpretation interpretation = vips_image_get_interpretation(in);
    int bands = vips_image_get_bands(in);
    bool is_grayscale_with_alpha = interpretation == VIPS_INTERPRETATION_B_W && bands != 1;
    bool is_not_srgb_or_grayscale =
            interpretation != VIPS_INTERPRETATION_sRGB && interpretation != VIPS_INTERPRETATION_B_W;

    if (is_grayscale_with_alpha || is_not_srgb_or_grayscale) {
        if (vips_colourspace(in, transformed, VIPS_INTERPRETATION_sRGB, NULL) == 0) {
        } else { return -1; }
    }

    // add alpha channel to use 32 bits per pixel
    if (interpretation == VIPS_INTERPRETATION_sRGB && bands == 3) {
        VipsImage *with_alpha = NULL;
        int vips_error;

        if (*transformed != NULL) {
            vips_error = vips_addalpha(*transformed, &with_alpha, NULL);
            g_object_unref(*transformed);
        } else {
            vips_error = vips_addalpha(in, &with_alpha, NULL);
        }

        if (vips_error) {
            komelia_throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            return -1;
        }

        *transformed = with_alpha;
    }

    return 0;
}

jobject komelia_to_jvm_image_data(JNIEnv *env, VipsImage *decoded) {
    VipsImage *transformed = NULL;
    int transform_error = transform_to_supported_format(env, decoded, &transformed);
    if (transform_error) { return NULL; }

    if (transformed == NULL) {
        transformed = decoded;
        g_object_ref(transformed);
    }

    jobject jvm_enum_interpretation = get_jvm_enum_type(env, transformed);
    if (jvm_enum_interpretation == NULL) {
        g_object_unref(transformed);
        return NULL;
    }

    int height = vips_image_get_height(transformed);
    int width = vips_image_get_width(transformed);
    int bands = vips_image_get_bands(transformed);
    int size = bands * width * height;

    unsigned char *data = (unsigned char *) vips_image_get_data(transformed);
    if (data == NULL) {
        komelia_throw_jvm_vips_exception(env, vips_error_buffer());
        vips_error_clear();
        g_object_unref(transformed);
        return NULL;
    }

    jbyteArray jvm_bytes = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, jvm_bytes, 0, size, (signed char *) data);

    g_object_unref(transformed);

    jclass jvm_vips_class = (*env)->FindClass(env, "io/github/snd_r/VipsImageData");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class,
                                                "<init>", "([BIILio/github/snd_r/ImageFormat;)V");

    return (*env)->NewObject(env, jvm_vips_class, constructor,
                             jvm_bytes, width, height, jvm_enum_interpretation);
}

VipsImage *komelia_from_jvm_handle(JNIEnv *env, jobject jvm_image) {
    jclass class = (*env)->GetObjectClass(env, jvm_image);
    jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
    VipsImage *image = (VipsImage *) (*env)->GetLongField(env, jvm_image, ptr_field);

    if (image == NULL) {
        komelia_throw_jvm_vips_exception(env, "image was already closed\n");
        return NULL;
    }

    return image;
}

jobject komelia_to_jvm_handle(JNIEnv *env, VipsImage *image, const unsigned char *external_source_buffer) {
    VipsImage *transformed = NULL;
    int transform_error = transform_to_supported_format(env, image, &transformed);
    if (transform_error) { return NULL; }

    if (transformed == NULL) {
        transformed = image;
    } else {
        g_object_unref(image);
    }

    jclass jvm_vips_class = (*env)->FindClass(env, "io/github/snd_r/VipsImage");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class,
                                                "<init>", "(IIILio/github/snd_r/ImageFormat;JJ)V");

    jobject jvm_type_enum = get_jvm_enum_type(env, transformed);

    int height = vips_image_get_height(transformed);
    int width = vips_image_get_width(transformed);
    int bands = vips_image_get_bands(transformed);
    return (*env)->NewObject(env, jvm_vips_class, constructor,
                             width, height, bands, jvm_type_enum,
                             (int64_t) external_source_buffer, (int64_t) transformed);
}
