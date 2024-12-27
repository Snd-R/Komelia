package io.github.snd_r.komelia.image.coil

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
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.platform.formatDecimal
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.Path
import okio.use
import snd.komelia.image.ImageDecoder
import snd.komelia.image.KomeliaImage
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger { }

class CoilDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val imageDecoder: ImageDecoder,
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val start = TimeSource.Monotonic.markNow()
        val file = source.fileOrNull()
        val decoded =
            if (file != null) decodeFromFile(file)
            else decodeBytes()

        decoded.use {
            val coilImage = decoded.toCoilImage()
            val result = DecodeResult(
                image = coilImage,
                isSampled = !options.size.isOriginal
            )

            val duration = TimeSource.Monotonic.markNow() - start

            logger.info {
                val timeSpent = duration.toString(DurationUnit.MILLISECONDS, 2)
                val sizeInMiB = (coilImage.size.toFloat() / 1024 / 1024).formatDecimal(2)
                "time spent: ${timeSpent}; " +
                        "size: ${coilImage.width}x${coilImage.height}; " +
                        "memory usage ${sizeInMiB}MiB"
            }

            return result
        }

    }

    private suspend fun decodeFromFile(path: Path): KomeliaImage {
        if (options.size.isOriginal) {
            return imageDecoder.decodeFromFile(path.toString())
        }

        val dstSize = calculateDstSize()
        return imageDecoder.decodeAndResize(
            path = path.toString(),
            scaleWidth = dstSize.width,
            scaleHeight = dstSize.height,
            crop = options.scale == Scale.FILL
        )
    }

    private suspend fun decodeBytes(): KomeliaImage {
        val bytes = try {
            source.source().use { it.readByteArray() }
            // catch fetch api error thrown by ktor on WasmJs and rethrow as expected runtime exception
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            throw RuntimeException(e)
        }

        if (options.size.isOriginal) {
            return imageDecoder.decode(bytes)
        }

        val dstSize = calculateDstSize()
        return imageDecoder.decodeAndResize(
            encoded = bytes,
            scaleWidth = dstSize.width,
            scaleHeight = dstSize.height,
            crop = options.scale == Scale.FILL
        )
    }

    private fun calculateDstSize(): IntSize {
        val dstWidth = options.size.width.pxOrElse { Int.MAX_VALUE }
        val dstHeight = options.size.height.pxOrElse { Int.MAX_VALUE }
        require(dstWidth != Int.MAX_VALUE && dstHeight != Int.MAX_VALUE)
        return IntSize(dstWidth, dstHeight)
    }

    class Factory(private val imageDecoder: ImageDecoder) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder {
            return CoilDecoder(result.source, options, imageDecoder)
        }
    }
}

expect suspend fun KomeliaImage.toCoilImage(): Image