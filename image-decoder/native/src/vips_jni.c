#include "vips_jni.h"

void komelia_vips_init() {
    VIPS_INIT("komelia");
    vips_concurrency_set(1);
    vips_cache_set_max(0);
}

jobject get_jvm_enum_type(JNIEnv *env, VipsImage *transformed) {
    VipsInterpretation interpretation = vips_image_get_interpretation(transformed);
    char *enum_field_name;
    switch (interpretation) {
        case VIPS_INTERPRETATION_B_W:
            enum_field_name = "VIPS_INTERPRETATION_B_W";
            break;
        case VIPS_INTERPRETATION_sRGB:
            enum_field_name = "VIPS_INTERPRETATION_sRGB";
            break;
        default:
            enum_field_name = "VIPS_INTERPRETATION_ERROR";
    }

    jclass enum_class = (*env)->FindClass(env, "io/github/snd_r/VipsInterpretation");
    jfieldID enum_field =
            (*env)->GetStaticFieldID(env, enum_class, enum_field_name, "Lio/github/snd_r/VipsInterpretation;");

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
    return (interpretation == VIPS_INTERPRETATION_B_W && bands != 1) ||
           interpretation != VIPS_INTERPRETATION_sRGB && interpretation != VIPS_INTERPRETATION_B_W;
}

int shouldPadWithAlpha(VipsImage *image) {
    VipsInterpretation interpretation = vips_image_get_interpretation(image);
    int bands = vips_image_get_bands(image);
    return interpretation == VIPS_INTERPRETATION_sRGB && bands == 3;
}

void transform_to_supported_format(VipsImage **in) {
    // convert to sRGB if not grayscale or if grayscale with alpha
    if (shouldTransformToSRGB(*in)) {
        VipsImage *transformed;
        if (vips_colourspace(*in, &transformed, VIPS_INTERPRETATION_sRGB, NULL) == 0) {
            VipsImage *old = *in;
            *in = transformed;
            g_object_unref(old);
        }
    }

    // pad with alpha to use 32 bits per pixel
    if (shouldPadWithAlpha(*in)) {
        VipsImage *transformed;
        if (vips_addalpha(*in, &transformed, NULL) == 0) {
            VipsImage *old = *in;
            *in = transformed;
            g_object_unref(old);
        }
    }
}

jobject komelia_vips_image_to_jvm(JNIEnv *env, VipsImage *decoded) {
    transform_to_supported_format(&decoded);
    jobject jvm_enum_interpretation = get_jvm_enum_type(env, decoded);
    jbyteArray jvm_bytes = get_jvm_byte_array(env, decoded);
    int height = vips_image_get_height(decoded);
    int width = vips_image_get_width(decoded);
    int bands = vips_image_get_bands(decoded);

    jclass jvm_vips_class = (*env)->FindClass(env, "io/github/snd_r/VipsImage");
    jmethodID constructor = (*env)->GetMethodID(env, jvm_vips_class,
                                                "<init>", "([BIIILio/github/snd_r/VipsInterpretation;)V");

    return (*env)->NewObject(env, jvm_vips_class, constructor,
                             jvm_bytes, width, height, bands, jvm_enum_interpretation);
}

void throw_jvm_vips_exception(JNIEnv *env, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "io/github/snd_r/VipsException"), message);
}

