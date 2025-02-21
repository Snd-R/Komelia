package snd.komelia.image

interface KomeliaImage : AutoCloseable {
    val width: Int
    val height: Int
    val bands: Int
    val type: ImageFormat


    suspend fun extractArea(rect: ImageRect): KomeliaImage
    suspend fun resize(
        scaleWidth: Int,
        scaleHeight: Int,
        linear: Boolean = false,
        kernel: ReduceKernel = ReduceKernel.DEFAULT
    ): KomeliaImage

    suspend fun shrink(factor: Double): KomeliaImage
    suspend fun findTrim(): ImageRect

    suspend fun makeHistogram(): KomeliaImage
    suspend fun mapLookupTable(table: ByteArray): KomeliaImage

    suspend fun getBytes(): ByteArray
}

data class ImageDimensions(
    val width: Int,
    val height: Int,
    val bands: Int,
)

enum class ImageFormat {
    GRAYSCALE_8,
    RGBA_8888,
    HISTOGRAM,
}

data class ImageRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

enum class ReduceKernel {
    NEAREST,
    LINEAR,
    CUBIC,
    MITCHELL,
    LANCZOS2,
    LANCZOS3,
    MKS2013,
    MKS2021,
    DEFAULT,
}
