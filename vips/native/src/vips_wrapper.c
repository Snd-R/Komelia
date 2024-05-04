#include <stdio.h>
#include <vips/vips.h>
#include <jni.h>

jobject getJavaImageType(JNIEnv *env, VipsImage *transformed);

jobject getJavaByteArray(JNIEnv *env, VipsImage *transformed);

void transformIfNecessary(VipsImage **in);

jobject toJavaRepresentation(JNIEnv *env, VipsImage *decoded);

JNIEXPORT void JNICALL Java_io_github_snd_1r_VipsDecoder_init() {
    VIPS_INIT("komelia");
    vips_concurrency_set(1);
    vips_cache_set_max(0);
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsDecoder_vipsDecode(
        JNIEnv *env,
        jobject this,
        jbyteArray encoded) {
    jsize inputLen = (*env)->GetArrayLength(env, encoded);
    jbyte *inputBytes = (*env)->GetByteArrayElements(env, encoded, JNI_FALSE);

    VipsImage *decoded = vips_image_new_from_buffer((unsigned char *) inputBytes, inputLen, "", NULL);

    if (!decoded) {
        return NULL;
    }

    transformIfNecessary(&decoded);

    jobject javaImage = toJavaRepresentation(env, decoded);
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
        return NULL;
    }

    transformIfNecessary(&decoded);

    jobject javaImage = toJavaRepresentation(env, decoded);
    g_object_unref(decoded);
    (*env)->ReleaseByteArrayElements(env, encoded, inputBytes, JNI_ABORT);
    return javaImage;
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

void transformIfNecessary(VipsImage **in) {
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

jobject toJavaRepresentation(JNIEnv *env, VipsImage *decoded) {
    jobject javaEnumInterpretation = getJavaImageType(env, decoded);
    jbyteArray javaBytes = getJavaByteArray(env, decoded);
    int height = vips_image_get_height(decoded);
    int width = vips_image_get_width(decoded);
    int bands = vips_image_get_bands(decoded);

    jclass javaVipsClass = (*env)->FindClass(env, "io/github/snd_r/VipsImage");
    jmethodID constructor = (*env)->GetMethodID(env, javaVipsClass,
                                                "<init>", "([BIIILio/github/snd_r/VipsInterpretation;)V");

    return (*env)->NewObject(env, javaVipsClass, constructor,
                             javaBytes, width, height, bands, javaEnumInterpretation);
}

jobject getJavaImageType(JNIEnv *env, VipsImage *transformed) {

    VipsInterpretation interpretation = vips_image_get_interpretation(transformed);
    char *interpEnumFieldName;
    switch (interpretation) {
        case VIPS_INTERPRETATION_B_W:
            interpEnumFieldName = "VIPS_INTERPRETATION_B_W";
            break;
        case VIPS_INTERPRETATION_sRGB:
            interpEnumFieldName = "VIPS_INTERPRETATION_sRGB";
            break;
        default:
            interpEnumFieldName = "VIPS_INTERPRETATION_ERROR";
    }

    jclass interpClass = (*env)->FindClass(env, "io/github/snd_r/VipsInterpretation");
    jfieldID interpEnumField =
            (*env)->GetStaticFieldID(env, interpClass, interpEnumFieldName, "Lio/github/snd_r/VipsInterpretation;");
    jobject interpEnum = (*env)->GetStaticObjectField(env, interpClass, interpEnumField);

    return interpEnum;
}

jbyteArray getJavaByteArray(JNIEnv *env, VipsImage *transformed) {
    unsigned char *data = (unsigned char *) vips_image_get_data(transformed);

    int size = transformed->Bands * transformed->Xsize * transformed->Ysize;

    jbyteArray java_bytes = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, java_bytes, 0, size, (signed char *) data);

    return java_bytes;
}

