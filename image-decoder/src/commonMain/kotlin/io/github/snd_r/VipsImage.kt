package io.github.snd_r

typealias NativePointer = Long

class VipsImage private constructor(
    val width: Int,
    val height: Int,
    val bands: Int,
    val type: ImageFormat,
    internalBuffer: NativePointer,
    vipsPointer: NativePointer,
) : VipsPointer(internalBuffer, vipsPointer) {

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
    }

    external fun getRegion(rect: ImageRect): VipsImage
    external fun resize(scaleWidth: Int, scaleHeight: Int, crop: Boolean): VipsImage
    external fun getBytes(): ByteArray
    external fun encodeToFile(path: String)
    external fun shrink(factor: Double): VipsImage

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

abstract class VipsPointer(
    bytesPtr: NativePointer,
    vipsPtr: NativePointer,
) : AutoCloseable {
    private var _ptr = vipsPtr
    private var _bytes = bytesPtr
    var isClosed = false
        private set

    @Synchronized
    override fun close() {
//        cleanable.clean()

        if (_ptr != 0L) gObjectUnref(_ptr)
        if (_bytes != 0L) free(_bytes)
        _ptr = 0
        _bytes = 0
        isClosed = true
    }

//    companion object {
//        private val cleaner: Cleaner = Cleaner.create()
//    }

//    @Suppress("LeakingThis")
//    private val cleanable: Cleaner.Cleanable = cleaner.register(this) {
//        if (vipsPtr != 0L) gObjectUnref(vipsPtr)
//        if (bytesPtr != 0L) free(bytesPtr)
//
//    }

    private external fun gObjectUnref(pointer: NativePointer)
    private external fun free(pointer: NativePointer)
}
