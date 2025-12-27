package snd.komelia.image.coil

import androidx.compose.ui.unit.IntSize
import coil3.Image
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.Path
import okio.use
import snd.komelia.image.KomeliaImage
import snd.komelia.image.KomeliaImageDecoder

class CoilDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val decoder: CoilAwareDecoder
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val file = source.fileOrNull()
        val decoded =
            if (file != null) decoder.decodeFromFile(file, options)
            else decoder.decodeBytes(getBytes(), options)


        return decoded.use {
            DecodeResult(decoded.toCoilImage(), !options.size.isOriginal)
        }
    }

    private suspend fun getBytes(): ByteArray {
        return try {
            source.source().use { it.readByteArray() }
            // catch fetch api error thrown by ktor on WasmJs and rethrow as expected runtime exception
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            throw RuntimeException(e)
        }
    }

    class Factory(private val imageDecoder: CoilAwareDecoder) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder {
            return CoilDecoder(result.source, options, imageDecoder)
        }
    }
}

class CoilAwareDecoder(private val imageDecoder: KomeliaImageDecoder) {

    suspend fun decodeFromFile(path: Path, options: Options): KomeliaImage {
        if (options.size.isOriginal) {
            return imageDecoder.decodeFromFile(path.toString())
        }

        val dstSize = calculateDstSize(options)
        return imageDecoder.decodeAndResize(
            path = path.toString(),
            scaleWidth = dstSize.width,
            scaleHeight = dstSize.height,
            crop = options.scale == Scale.FILL
        )
    }

    suspend fun decodeBytes(bytes: ByteArray, options: Options): KomeliaImage {
        if (options.size.isOriginal) {
            return imageDecoder.decode(bytes)
        }

        val dstSize = calculateDstSize(options)
        return imageDecoder.decodeAndResize(
            encoded = bytes,
            scaleWidth = dstSize.width,
            scaleHeight = dstSize.height,
            crop = options.scale == Scale.FILL
        )
    }

    private fun calculateDstSize(options: Options): IntSize {
        val dstWidth = options.size.width.pxOrElse { Int.MAX_VALUE }
        val dstHeight = options.size.height.pxOrElse { Int.MAX_VALUE }
        require(dstWidth != Int.MAX_VALUE && dstHeight != Int.MAX_VALUE)
        return IntSize(dstWidth, dstHeight)
    }
}

expect suspend fun KomeliaImage.toCoilImage(): Image