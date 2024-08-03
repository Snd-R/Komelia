package io.github.snd_r.komelia.image

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.isOriginal
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.ImageWorker.Interpretation.BW
import io.github.snd_r.komelia.image.ImageWorker.Interpretation.SRGB
import io.github.snd_r.komelia.image.ImageWorker.WorkerDecodeResult
import io.ktor.util.*
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class VipsImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val worker: ImageWorker
) : Decoder {

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun decode(): DecodeResult {
        val dataResult = measureTimedValue {
            // FIXME? wasm bytearray to js array copy overhead
            source.source().use { it.readByteArray() }.toJsArray()
        }.also { logger.info { "retrieved image bytes in ${it.duration}" } }

        val crop = options.scale == Scale.FILL
                && options.size.width != Dimension.Undefined
                && options.size.height != Dimension.Undefined

        val decoded = worker.decode(
            bytes = dataResult.value,
            dstWidth = options.size.width.pxOrNull(),
            dstHeight = options.size.height.pxOrNull(),
            crop = crop
        )

        // FIXME? js array to wasm array copy overhead
        val decodeResult = measureTimedValue {
            val bitmap = toBitmap(decoded)
            bitmap.setImmutable()

            DecodeResult(
                image = bitmap.asCoilImage(),
                isSampled = !options.size.isOriginal
            )
        }.also { logger.info { "installed pixels in ${it.duration}" } }

        return decodeResult.value
    }

    private fun toBitmap(decoded: WorkerDecodeResult): Bitmap {
        val colorInfo = when (decoded.interpretation) {
            BW -> {
                require(decoded.bands == 1) { "Unexpected number of bands  for grayscale image \"${decoded.bands}\"" }
                ColorInfo(
                    ColorType.GRAY_8,
                    ColorAlphaType.UNPREMUL,
                    ColorSpace.sRGB
                )
            }

            SRGB -> {
                require(decoded.bands == 4) { "Unexpected number of bands  for sRGB image  \"${decoded.bands}\"" }
                ColorInfo(
                    ColorType.RGBA_8888,
                    ColorAlphaType.UNPREMUL,
                    ColorSpace.sRGB
                )
            }
        }

        val imageInfo = ImageInfo(colorInfo, decoded.width, decoded.height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(decoded.buffer.toByteArray())
        return bitmap
    }

    class Factory(
        private
        val worker: ImageWorker
    ) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return VipsImageDecoder(result.source, options, worker)
        }
    }
}

fun Dimension.pxOrNull(): Int? = if (this is Dimension.Pixels) px else null

private fun Uint8Array.toByteArray(): ByteArray = ByteArray(this.length) { this[it] }

