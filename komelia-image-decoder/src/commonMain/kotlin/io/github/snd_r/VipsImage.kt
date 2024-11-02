package io.github.snd_r

import snd.jni.Managed
import snd.jni.NativePointer


class VipsImage private constructor(
    val width: Int,
    val height: Int,
    val bands: Int,
    val type: ImageFormat,
    internalBuffer: NativePointer,
    vipsPointer: NativePointer,
) : Managed(vipsPointer, VipsFinalizer(internalBuffer, vipsPointer)) {

    private class VipsFinalizer(private var bytesPtr: Long, private var vipsPtr: Long) : Runnable {
        override fun run() {
            if (vipsPtr != 0L) gObjectUnref(vipsPtr)
            if (bytesPtr != 0L) free(bytesPtr)
        }
    }

    companion object {
        const val DIMENSION_MAX_SIZE = 10_000_000

        @JvmStatic
        external fun getDimensions(encoded: ByteArray): VipsImageDimensions

        @JvmStatic
        external fun decode(encoded: ByteArray): VipsImage


        @JvmStatic
        external fun decodeFromFile(path: String): VipsImage

        @JvmStatic
        external fun thumbnail(
            path: String,
            scaleWidth: Int,
            scaleHeight: Int,
            crop: Boolean
        ): VipsImage

        @JvmStatic
        external fun decodeAndGet(encoded: ByteArray): VipsImageData

        @JvmStatic
        external fun decodeResizeAndGet(
            encoded: ByteArray,
            scaleWidth: Int,
            scaleHeight: Int,
            crop: Boolean
        ): VipsImageData

        @JvmStatic
        external fun vipsInit()

        @JvmStatic
        private external fun gObjectUnref(pointer: NativePointer)

        @JvmStatic
        private external fun free(pointer: NativePointer)
    }

    external fun extractArea(rect: ImageRect): VipsImage
    external fun resize(scaleWidth: Int, scaleHeight: Int, crop: Boolean): VipsImage
    external fun getBytes(): ByteArray
    external fun encodeToFile(path: String)
    external fun encodeToFilePng(path: String)
    external fun shrink(factor: Double): VipsImage
    external fun findTrim(): ImageRect
}

data class VipsImageDimensions(
    val width: Int,
    val height: Int,
    val bands: Int,
)

class VipsImageData(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val type: ImageFormat,
)

data class ImageRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

enum class ImageFormat {
    GRAYSCALE_8,
    RGBA_8888,
}
