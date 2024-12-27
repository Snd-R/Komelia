package snd.komelia.image

interface ImageDecoder {
    suspend fun decode(encoded: ByteArray): KomeliaImage
    suspend fun decodeFromFile(path: String): KomeliaImage
    suspend fun decodeAndResize(
        encoded: ByteArray,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
    ): KomeliaImage

    suspend fun decodeAndResize(
        path: String,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean
    ): KomeliaImage
}
