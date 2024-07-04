#include "vips_jni.h"
#include <stdint.h>

void komelia_vips_init() {
    VIPS_INIT("komelia");
    vips_concurrency_set(1);
    vips_cache_set_max(0);
}

void komelia_vips_shutdown() {
    vips_shutdown();
}

void throw_jvm_vips_exception(JNIEnv *env, const char *message) {
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
            throw_jvm_vips_exception(env, "unsupported vips interpretation");
            return NULL;
    }

    jclass enum_class = (*env)->FindClass(env, "io/github/snd_r/ImageFormat");
    jfieldID enum_field = (*env)->GetStaticFieldID(env, enum_class, enum_field_name, "Lio/github/snd_r/ImageFormat;");
    return (*env)->GetStaticObjectField(env, enum_class, enum_field);
}


jbyteArray get_jvm_byte_array(JNIEnv *env, VipsImage *transformed) {
    unsigned char *data = (unsigned char *) vips_image_get_data(transformed);

    int size = transformed->Bands * transformed->Xsize * transformed->Ysize;

    jbyteArray java_bytes = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, java_bytes, 0, size, (signed char *) data);

    return java_bytes;
}

int shouldTransformToSRGB(VipsImage *image) {
    VipsInterpretation interpretation = vips_image_get_interpretation(image);
    int bands = vips_image_get_bands(image);
    return ((interpretation == VIPS_INTERPRETATION_B_W || interpretation == VIPS_INTERPRETATION_MULTIBAND) &&
            bands != 1) ||
           interpretation != VIPS_INTERPRETATION_sRGB &&
           interpretation != VIPS_INTERPRETATION_B_W &&
           interpretation != VIPS_INTERPRETATION_MULTIBAND;
}

int shouldPadWithAlpha(VipsImage *image) {
    VipsInterpretation interpretation = vips_image_get_interpretation(image);
    int bands = vips_image_get_bands(image);
    return interpretation == VIPS_INTERPRETATION_sRGB && bands == 3;
}

void transform_to_supported_format(VipsImage *in, VipsImage **transformed) {
    // convert to sRGB if not grayscale or if grayscale with alpha
    if (transformed == NULL) return;

    if (shouldTransformToSRGB(in)) {
        if (vips_colourspace(in, transformed, VIPS_INTERPRETATION_sRGB, NULL) == 0) {
        } else {
            fprintf(stderr, "failed to change to srgb\n");
        }
    }

    // pad with alpha to use 32 bits per pixel
    if (shouldPadWithAlpha(in)) {
        VipsImage *with_alpha = NULL;
        int vips_error;
        if (*transformed != NULL) {
            vips_error = vips_addalpha(*transformed, &with_alpha, NULL);
            g_object_unref(*transformed);
        } else {
            vips_error = vips_addalpha(in, &with_alpha, NULL);
        }

        *transformed = with_alpha;
    }

}

jobject komelia_image_to_jvm_image_data(JNIEnv *env, VipsImage *decoded) {
    VipsImage *transformed = NULL;
    transform_to_supported_format(decoded, &transformed);
    if (transformed == NULL) {
        transformed = decoded;
        g_object_ref(transformed);
    }

    jobject jvm_enum_interpretation = get_jvm_enum_type(env, transformed);
    if (jvm_enum_interpretation == NULL) {
        g_object_unref(transformed);
        return NULL;
    }

    jbyteArray jvm_bytes = get_jvm_byte_array(env, transformed);
    int height = vips_image_get_height(transformed);
    int width = vips_image_get_width(transformed);
    g_object_unref(transformed);

    jclass jvm_vips_class = (*env)->FindClass(env, "io/github/snd_r/VipsImageData");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class,
                                                "<init>", "([BIILio/github/snd_r/ImageFormat;)V");

    return (*env)->NewObject(env, jvm_vips_class, constructor,
                             jvm_bytes, width, height, jvm_enum_interpretation);
}

VipsImage *from_jvm_handle(JNIEnv *env, jobject jvm_image) {
    jclass class = (*env)->GetObjectClass(env, jvm_image);
    jfieldID ptr_field = (*env)->GetFieldID(env, class, "_ptr", "J");
    VipsImage *image = (VipsImage *) (*env)->GetLongField(env, jvm_image, ptr_field);

    if (image == NULL) {
        throw_jvm_vips_exception(env, "image was already closed\n");
        return NULL;
    }

    return image;
}

jobject to_jvm_handle_from_file(JNIEnv *env, VipsImage *image) {
    return to_jvm_image_handle(env, image, NULL);
}

jobject to_jvm_image_handle(JNIEnv *env, VipsImage *image, unsigned char *jvm_bytes) {
    const char *jvm_vips_class_name = "io/github/snd_r/VipsImage";

    VipsImage *transformed = NULL;
    transform_to_supported_format(image, &transformed);

    if (transformed == NULL) {
        transformed = image;
    } else {
        g_object_unref(image);
    }

    jclass jvm_vips_class = (*env)->FindClass(env, jvm_vips_class_name);
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class,
                                                "<init>", "(IIILio/github/snd_r/ImageFormat;JJ)V");

    jobject jvm_type_enum = get_jvm_enum_type(env, transformed);

    int height = vips_image_get_height(transformed);
    int width = vips_image_get_width(transformed);
    int bands = vips_image_get_bands(transformed);
    return (*env)->NewObject(env, jvm_vips_class, constructor,
                             width, height, bands, jvm_type_enum,
                             (int64_t) jvm_bytes, (int64_t) transformed);
}
