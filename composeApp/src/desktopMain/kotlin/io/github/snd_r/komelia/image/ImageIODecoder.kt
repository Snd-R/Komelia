package io.github.snd_r.komelia.image

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.Size
import coil3.size.isOriginal
import coil3.size.pxOrElse
import com.twelvemonkeys.image.ResampleOp
import io.github.oshai.kotlinlogging.KotlinLogging
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalCoilApi::class)
class ImageIODecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val bytes = source.source().use { it.readByteArray() }
        return imageIoResample(bytes, options)
    }

    private fun imageIoResample(bytes: ByteArray, options: Options): DecodeResult {
        val result = measureTimedValue {
            val image = ImageIO.read(bytes.inputStream())

            val srcWidth = image.width
            val srcHeight = image.height
            var multiplier = DecodeUtils.computeSizeMultiplier(
                srcWidth = srcWidth,
                srcHeight = srcHeight,
                dstWidth = options.size.widthPx(options.scale) { srcWidth },
                dstHeight = options.size.heightPx(options.scale) { srcHeight },
                scale = options.scale,
            )
            if (options.allowInexactSize) {
                multiplier = multiplier.coerceAtMost(1.0)
            }

            if (multiplier == 1.0) return DecodeResult(image = image.toBitmap().asCoilImage(), isSampled = false)


            val dstWidth = (multiplier * srcWidth).toInt()
            val dstHeight = (multiplier * srcHeight).toInt()

            val resampled = ResampleOp(dstWidth, dstHeight, ResampleOp.FILTER_LANCZOS)
                .filter(image, null)

            val bitmap = resampled.toBitmap()
            bitmap.setImmutable()

            DecodeResult(
                image = bitmap.asCoilImage(),
                isSampled = true,
            )
        }
        logger.info { "Time spent ${result.duration}" }

        return result.value
    }

    fun BufferedImage.toBitmap(): Bitmap {
        return when (colorModel.colorSpace.type) {
            ColorSpace.TYPE_GRAY -> grayscaleImageToBitmap(this)
            else -> rgbImageToBitmap(this)

        }
    }

    private fun grayscaleImageToBitmap(image: BufferedImage): Bitmap {
        val pixels = ByteArray(image.width * image.height)
        var k = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixelSample = image.raster.getSample(x, y, 0)
                val pixelByte = pixelSample.toByte()
                pixels[k++] = pixelByte
            }
        }

        val colorInfo = ColorInfo(
            ColorType.GRAY_8,
            ColorAlphaType.UNPREMUL,
            org.jetbrains.skia.ColorSpace.sRGB
        )
        val imageInfo = ImageInfo(colorInfo, image.width, image.height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(pixels)
        return bitmap
    }

    private fun rgbImageToBitmap(image: BufferedImage): Bitmap {
        val bytesPerPixel = 4
        val pixels = ByteArray(image.width * image.height * bytesPerPixel)

        var k = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                val a = (argb shr 24) and 0xff
                val r = (argb shr 16) and 0xff
                val g = (argb shr 8) and 0xff
                val b = (argb shr 0) and 0xff
                pixels[k++] = b.toByte()
                pixels[k++] = g.toByte()
                pixels[k++] = r.toByte()
                pixels[k++] = a.toByte()
            }
        }

        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo.makeS32(image.width, image.height, ColorAlphaType.UNPREMUL))
        bitmap.installPixels(pixels)
        return bitmap

    }

    private inline fun Size.widthPx(scale: Scale, original: () -> Int): Int {
        return if (isOriginal) original() else width.toPx(scale)
    }

    private inline fun Size.heightPx(scale: Scale, original: () -> Int): Int {
        return if (isOriginal) original() else height.toPx(scale)
    }

    private fun Dimension.toPx(scale: Scale): Int = pxOrElse {
        when (scale) {
            Scale.FILL -> Int.MIN_VALUE
            Scale.FIT -> Int.MAX_VALUE
        }
    }

    class Factory : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return ImageIODecoder(result.source, options)
        }
    }
}


