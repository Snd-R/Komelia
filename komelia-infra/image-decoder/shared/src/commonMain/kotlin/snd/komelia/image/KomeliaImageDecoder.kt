package snd.komelia.image

interface KomeliaImageDecoder {
    suspend fun decode(encoded: ByteArray, nPages: Int? = null): KomeliaImage
    suspend fun decodeFromFile(path: String, nPages: Int? = null): KomeliaImage
    suspend fun decodeAndResize(
        encoded: ByteArray,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int? = null
    ): KomeliaImage

    suspend fun decodeAndResize(
        path: String,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int? = null
    ): KomeliaImage
}
