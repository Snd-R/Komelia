#include "vips_common_jni.h"
#include "android/bitmap.h"
#include <android/hardware_buffer_jni.h>
#include <android/hardware_buffer.h>
#include <android/log.h>


int convert_to_rgba(JNIEnv *env, VipsImage *input, VipsImage **output) {
    VipsImage *transformed = input;
    g_object_ref(transformed);

    VipsInterpretation interpretation = vips_image_get_interpretation(input);
    if (interpretation != VIPS_INTERPRETATION_sRGB) {
        VipsImage *srgb = NULL;
        if (vips_colourspace(input, &srgb, VIPS_INTERPRETATION_sRGB, NULL)) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            g_object_unref(transformed);
            vips_error_clear();
            return -1;
        }

        g_object_unref(transformed);
        transformed = srgb;
    }

    if (vips_image_get_bands(transformed) != 4) {
        VipsImage *with_alpha = NULL;
        if (vips_addalpha(transformed, &with_alpha, NULL)) {
            throw_jvm_vips_exception(env, vips_error_buffer());
            vips_error_clear();
            g_object_unref(transformed);
            return -1;
        }

        g_object_unref(transformed);
        transformed = with_alpha;

    }

    *output = transformed;
    return 0;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsBitmapFactory_createHardwareBitmap(JNIEnv *env, jobject this, jobject jvm_image) {
    VipsImage *image = from_jvm_handle(env, jvm_image);
    if (image == NULL) return NULL;

    VipsImage *processed_input = NULL;
    int conversion_error = convert_to_rgba(env, image, &processed_input);
    if (conversion_error) { return NULL; }
    int image_width = vips_image_get_width(processed_input);
    int image_height = vips_image_get_height(processed_input);

    AHardwareBuffer *hardware_buffer = NULL;
    AHardwareBuffer_Desc desc;
    desc.width = image_width;
    desc.height = image_height;
    desc.layers = 1;
    desc.format = AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM;

    desc.usage = AHARDWAREBUFFER_USAGE_CPU_READ_RARELY |
                 AHARDWAREBUFFER_USAGE_CPU_WRITE_RARELY |
                 AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE;
    desc.stride = 0;
    desc.rfu0 = 0;
    desc.rfu1 = 0;

    int allocation_error = AHardwareBuffer_allocate(&desc, &hardware_buffer);
    if (allocation_error) {
        throw_jvm_vips_exception(env, "Could not allocate bitmap hardware buffer");
        return NULL;
    }

    void *write_buffer = NULL;
    int lock_error = AHardwareBuffer_lock(hardware_buffer,
                                          AHARDWAREBUFFER_USAGE_CPU_READ_RARELY |
                                          AHARDWAREBUFFER_USAGE_CPU_WRITE_RARELY,
                                          -1, NULL,
                                          &write_buffer
    );

    AHardwareBuffer_Desc created_desc;
    AHardwareBuffer_describe(hardware_buffer, &created_desc);
    if (lock_error) {
        throw_jvm_vips_exception(env, "Could not acquire created hardware hardware_buffer");
        AHardwareBuffer_release(hardware_buffer);
        return NULL;
    }

    // assume little endian, should be able to interpret as 32bit packed int
    unsigned char *image_data = (unsigned char *) vips_image_get_data(processed_input);

    if (created_desc.stride == image_width) {
        memcpy(write_buffer, image_data, image_width * image_height * 4);
    } else {
        memset(write_buffer, 255, created_desc.stride * created_desc.height);
//        memcpy(write_buffer, image_data, image_width * image_height * 4);
        for (int y = 0; y < image_height; ++y) {
            memcpy(write_buffer + (created_desc.stride * y * 4),
                   image_data + (image_width * y * 4),
                   image_width * 4
            );

        }
    }
    int unlock_error = AHardwareBuffer_unlock(hardware_buffer, NULL);
    g_object_unref(processed_input);

    if (unlock_error) {
        throw_jvm_vips_exception(env, "Failed to unlock hardware buffer");
        AHardwareBuffer_release(hardware_buffer);
        return NULL;
    }

    jobject jvm_buffer = AHardwareBuffer_toHardwareBuffer(env, hardware_buffer);
    AHardwareBuffer_release(hardware_buffer);
    jclass bitmap_class = (*env)->FindClass(env, "android/graphics/Bitmap");

    jmethodID create_method = (*env)->GetStaticMethodID(env, bitmap_class,
                                                        "wrapHardwareBuffer",
                                                        "(Landroid/hardware/HardwareBuffer;Landroid/graphics/ColorSpace;)Landroid/graphics/Bitmap;");

    jobject jvm_bitmap = (*env)->CallStaticObjectMethod(env, bitmap_class, create_method, jvm_buffer, NULL);
    return jvm_bitmap;
}

JNIEXPORT jobject JNICALL
Java_io_github_snd_1r_VipsBitmapFactory_createSoftwareBitmap(JNIEnv *env, jobject this, jobject jvm_image) {
    VipsImage *image = from_jvm_handle(env, jvm_image);
    VipsImage *processed_image = NULL;
    int conversion_error = convert_to_rgba(env, image, &processed_image);
    if (conversion_error) { return NULL; }

    int image_width = vips_image_get_width(processed_image);
    int image_height = vips_image_get_height(processed_image);
    unsigned char *image_data = (unsigned char *) vips_image_get_data(processed_image);

    jclass bitmap_class = (*env)->FindClass(env, "android/graphics/Bitmap");
    jclass config_enum_class = (*env)->FindClass(env, "android/graphics/Bitmap$Config");
    jfieldID config_enum_field = (*env)->GetStaticFieldID(env, config_enum_class, "ARGB_8888",
                                                          "Landroid/graphics/Bitmap$Config;");
    jobject config_enum = (*env)->GetStaticObjectField(env, config_enum_class, config_enum_field);

    jmethodID create_method = (*env)->GetStaticMethodID(env, bitmap_class,
                                                        "createBitmap",
                                                        "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");


    jobject jvm_bitmap = (*env)->CallStaticObjectMethod(env, bitmap_class, create_method,
                                                        image_width, image_height, config_enum);


    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, jvm_bitmap, &info);
    unsigned char *bitmap_data = NULL;
    int lock_error = AndroidBitmap_lockPixels(env, jvm_bitmap, (void *) &bitmap_data);
    if (lock_error) {
        throw_jvm_vips_exception(env, "Failed to lock Bitmap");
        g_object_unref(processed_image);
        return NULL;
    }
    memcpy(bitmap_data, image_data, info.height * info.stride);

    int unlock_error = AndroidBitmap_unlockPixels(env, jvm_bitmap);
    if (unlock_error) {
        throw_jvm_vips_exception(env, "Failed to unlock Bitmap");
        return NULL;
    }

    g_object_unref(processed_image);
    return jvm_bitmap;
}
