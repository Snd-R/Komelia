package io.github.snd_r.komelia.image

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.VipsDecoder
import io.github.snd_r.VipsImage
import io.github.snd_r.VipsInterpretation.VIPS_INTERPRETATION_B_W
import io.github.snd_r.VipsInterpretation.VIPS_INTERPRETATION_ERROR
import io.github.snd_r.VipsInterpretation.VIPS_INTERPRETATION_sRGB
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

private const val vipsMaxSize = 10000000

@OptIn(ExperimentalCoilApi::class)
class VipsImageDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val bytes = source.source().use { it.readByteArray() }

        val result = measureTimedValue {
            val decoded = if (options.size.isOriginal) VipsDecoder.vipsDecode(bytes)
            else {
                val dstWidth = options.size.width.pxOrElse { vipsMaxSize }
                val dstHeight = options.size.height.pxOrElse { vipsMaxSize }
                val crop = options.scale == Scale.FILL
                VipsDecoder.vipsDecodeAndResize(bytes, dstWidth, dstHeight, crop)
            }
            if (decoded == null) throw IllegalStateException("Could not decode image")

            val bitmap = decoded.toBitmap()
            bitmap.setImmutable()

            DecodeResult(
                image = bitmap.asCoilImage(),
                isSampled = !options.size.isOriginal
            )
        }

        val sizeInMb = "%.2f".format(result.value.image.size.toFloat() / 1024 / 1024)
        logger.info {
            "time spent: ${
                result.duration.toString(DurationUnit.MILLISECONDS, 2)
            }; size: ${result.value.image.width}x${result.value.image.height}; memory usage ${sizeInMb}MB"
        }

        return result.value
    }

    private fun VipsImage.toBitmap(): Bitmap {
        return when (type) {
            VIPS_INTERPRETATION_B_W -> grayscaleToBitmap(this)
            VIPS_INTERPRETATION_sRGB -> sRGBImageToBitmap(this)
            VIPS_INTERPRETATION_ERROR -> throw IllegalStateException("Decode error")
        }
    }

    private fun grayscaleToBitmap(image: VipsImage): Bitmap {
        require(image.bands == 1) { "Unexpected number of bands  for grayscale image \"${image.bands}\"" }

        val colorInfo = ColorInfo(
            ColorType.GRAY_8,
            ColorAlphaType.UNPREMUL,
            ColorSpace.sRGB
        )

        val imageInfo = ImageInfo(colorInfo, image.width, image.height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(image.data)
        return bitmap
    }

    private fun sRGBImageToBitmap(image: VipsImage): Bitmap {
        require(image.bands == 4) { "Unexpected number of bands  for sRGB image  \"${image.bands}\"" }
        val colorInfo = ColorInfo(
            ColorType.RGBA_8888,
            ColorAlphaType.UNPREMUL,
            ColorSpace.sRGB
        )

        val imageInfo = ImageInfo(colorInfo, image.width, image.height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(image.data)
        return bitmap
    }

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return VipsImageDecoder(result.source, options)
        }
    }

}

