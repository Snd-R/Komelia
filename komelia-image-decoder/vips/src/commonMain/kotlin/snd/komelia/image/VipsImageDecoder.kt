package snd.komelia.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VipsImageDecoder : ImageDecoder {
    override suspend fun decode(encoded: ByteArray): KomeliaImage {
        return withContext(Dispatchers.Default) { VipsBackedImage(VipsImage.decode(encoded)) }
    }

    override suspend fun decodeFromFile(path: String): KomeliaImage {
        return withContext(Dispatchers.Default) { VipsBackedImage(VipsImage.decodeFromFile(path)) }
    }

    override suspend fun decodeAndResize(
        path: String,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
    ): KomeliaImage {
        return withContext(Dispatchers.Default) {
            VipsBackedImage(
                VipsImage.thumbnail(
                    path = path,
                    scaleWidth = scaleWidth.coerceAtMost(VipsImage.DIMENSION_MAX_SIZE),
                    scaleHeight = scaleHeight.coerceAtMost(VipsImage.DIMENSION_MAX_SIZE),
                    crop = crop
                )
            )
        }
    }

    override suspend fun decodeAndResize(
        encoded: ByteArray,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean
    ): KomeliaImage {
        return withContext(Dispatchers.Default) {
            VipsBackedImage(
                VipsImage.thumbnailBuffer(
                    encoded = encoded,
                    scaleWidth = scaleWidth.coerceAtMost(VipsImage.DIMENSION_MAX_SIZE),
                    scaleHeight = scaleHeight.coerceAtMost(VipsImage.DIMENSION_MAX_SIZE),
                    crop = crop
                )
            )
        }
    }
}

fun KomeliaImage.toVipsImage(): VipsImage = when (this) {
    is VipsBackedImage -> vipsImage
    else -> throw UnsupportedOperationException("Unable to obtain snd.komelia.Image")
}

class VipsBackedImage(val vipsImage: VipsImage) : KomeliaImage {
    override val width: Int = vipsImage.width
    override val height: Int = vipsImage.height
    override val bands: Int = vipsImage.bands
    override val type: ImageFormat = vipsImage.type

    override suspend fun extractArea(rect: ImageRect): KomeliaImage {
        return withContext(Dispatchers.Default) {
            VipsBackedImage(vipsImage.extractArea(rect))
        }
    }

    override suspend fun resize(
        scaleWidth: Int,
        scaleHeight: Int,
        linear: Boolean,
        kernel: ReduceKernel,
    ): KomeliaImage {
        return withContext(Dispatchers.Default) {
            val vipsKernel = when (kernel) {
                ReduceKernel.DEFAULT -> VipsKernel.LANCZOS3
                ReduceKernel.NEAREST -> VipsKernel.NEAREST
                ReduceKernel.LINEAR -> VipsKernel.LINEAR
                ReduceKernel.CUBIC -> VipsKernel.CUBIC
                ReduceKernel.MITCHELL -> VipsKernel.MITCHELL
                ReduceKernel.LANCZOS2 -> VipsKernel.LANCZOS2
                ReduceKernel.LANCZOS3 -> VipsKernel.LANCZOS3
                ReduceKernel.MKS2013 -> VipsKernel.MKS2013
                ReduceKernel.MKS2021 -> VipsKernel.MKS2021
            }

            VipsBackedImage(
                vipsImage.resize(
                    targetWidth = scaleWidth.coerceAtMost(VipsImage.DIMENSION_MAX_SIZE),
                    targetHeight = scaleHeight.coerceAtMost(VipsImage.DIMENSION_MAX_SIZE),
                    kernel = vipsKernel.name,
                    linear = linear,
                )
            )
        }
    }

    override suspend fun getBytes(): ByteArray {
        return vipsImage.getBytes()
    }

    override suspend fun shrink(factor: Double): KomeliaImage {
        return withContext(Dispatchers.Default) {
            VipsBackedImage(vipsImage.shrink(factor))
        }
    }

    override suspend fun findTrim(): ImageRect {
        return withContext(Dispatchers.Default) { vipsImage.findTrim() }
    }

    override suspend fun makeHistogram(): KomeliaImage {
        return withContext(Dispatchers.Default) {
            VipsBackedImage(vipsImage.makeHistogram())
        }
    }

    override suspend fun mapLookupTable(table: ByteArray): KomeliaImage {
        return withContext(Dispatchers.Default) {
            VipsBackedImage(vipsImage.mapLookupTable(table))
        }
    }

    override fun close() {
        vipsImage.close()
    }
}

