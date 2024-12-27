package snd.komelia.image

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
        external fun getDimensions(encoded: ByteArray): ImageDimensions

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

    external fun makeHistogram(): VipsImage
    external fun mapLookupTable(table: ByteArray): VipsImage
}

class VipsException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
}

enum class VipsKernel {
    NEAREST,
    LINEAR,
    CUBIC,
    MITCHELL,
    LANCZOS2,
    LANCZOS3
}
