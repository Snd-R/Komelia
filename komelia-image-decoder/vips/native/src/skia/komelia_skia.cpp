#include <include/core/SkBitmap.h>
#include <include/core/SkImage.h>
#include <include/core/SkColorSpace.h>
#include "../vips/vips_common_jni.h"

static sk_sp<SkColorSpace> srgbColorspace = SkColorSpace::MakeSRGB();

void freeData(void *data, void *) {
    free(data);
}

extern "C" JNIEXPORT jobject JNICALL Java_snd_komelia_image_SkiaBitmap_directCopyToSkiaBitmap(
        JNIEnv *env,
        jobject thisObject,
        jobject jvm_image
) {
    VipsImage *image = komelia_from_jvm_handle(env, jvm_image);
    if (image == nullptr) return nullptr;

    int width = vips_image_get_width(image);
    int height = vips_image_get_height(image);
    int bands = vips_image_get_bands(image);
    size_t rowBytes = width * bands;
    size_t size = height * rowBytes;
    void *imageData = (void *) vips_image_get_data(image);
    if (imageData == nullptr) {
        komelia_throw_jvm_vips_exception(env);
        return nullptr;
    }

    void *dataCopy = malloc(height * rowBytes);
    memcpy(dataCopy, imageData, size);

    auto *bitmap = new SkBitmap();
    SkColorType colorType;
    if (bands == 1) colorType = kGray_8_SkColorType;
    else colorType = kRGBA_8888_SkColorType;

    SkImageInfo imageInfo = SkImageInfo::Make(width,
                                              height,
                                              colorType,
                                              kUnpremul_SkAlphaType,
                                              srgbColorspace);

    bool success = bitmap->tryAllocPixels(imageInfo, rowBytes);
    if (!success) {
        komelia_throw_jvm_vips_exception_message(env, "failed to allocate bitmap pixels");
        delete bitmap;
        free(dataCopy);
        return nullptr;
    }

    success = bitmap->installPixels(imageInfo, dataCopy, rowBytes, freeData, nullptr);
    if (!success) {
        komelia_throw_jvm_vips_exception_message(env, "failed to install bitmap pixels");
        delete bitmap;
        free(dataCopy);
        return nullptr;
    }
    bitmap->setImmutable();

    jclass jvm_bitmap_class = env->FindClass("org/jetbrains/skia/Bitmap");
    jmethodID constructor = env->GetMethodID(jvm_bitmap_class, "<init>", "(J)V");
    return env->NewObject(jvm_bitmap_class, constructor, reinterpret_cast<jlong>(bitmap));
}
