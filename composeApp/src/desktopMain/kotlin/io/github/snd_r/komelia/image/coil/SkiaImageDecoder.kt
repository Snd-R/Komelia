package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Precision.INEXACT
import coil3.size.Scale
import coil3.size.Size
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import okio.use
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.impl.use
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

// based on https://github.com/coil-kt/coil/blob/004b116e2748de014ac5162429febe65ad4a9ce5/coil-core/src/nonAndroidMain/kotlin/coil3/decode/SkiaImageDecoder.kt
@OptIn(ExperimentalCoilApi::class)
class SkiaImageDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val samplingMode: SamplingMode = SamplingMode.MITCHELL
) : Decoder {

    override suspend fun decode(): DecodeResult {
        // https://github.com/JetBrains/skiko/issues/741
        val bytes = source.source().use { it.readByteArray() }
        val result = measureTimedValue {
            val image = Image.makeFromEncoded(bytes)

            val isSampled: Boolean
            val bitmap: Bitmap
            try {
                bitmap = Bitmap.makeFromImage(image, options, samplingMode)
                bitmap.setImmutable()
                isSampled = bitmap.width < image.width || bitmap.height < image.height
            } finally {
                image.close()
            }

            DecodeResult(
                image = bitmap.asImage(),
                isSampled = isSampled,
            )
        }
        logger.info { "Time spent ${result.duration}" }
        return result.value
    }

    private fun Bitmap.Companion.makeFromImage(
        image: Image,
        options: Options,
        samplingMode: SamplingMode,
    ): Bitmap {
        val srcWidth = image.width
        val srcHeight = image.height
        var multiplier = DecodeUtils.computeSizeMultiplier(
            srcWidth = srcWidth,
            srcHeight = srcHeight,
            dstWidth = options.size.widthPx(options.scale) { srcWidth },
            dstHeight = options.size.heightPx(options.scale) { srcHeight },
            scale = options.scale,
        )

        // Only upscale the image if the options require an exact size.
        if (options.precision == INEXACT) {
            multiplier = multiplier.coerceAtMost(1.0)
        }

        val dstWidth = (multiplier * srcWidth).toInt()
        val dstHeight = (multiplier * srcHeight).toInt()

        val bitmap = Bitmap()
        bitmap.allocN32Pixels(dstWidth, dstHeight)
        Canvas(bitmap).use { canvas ->
            canvas.drawImageRect(
                image = image,
                src = Rect.makeWH(srcWidth.toFloat(), srcHeight.toFloat()),
                dst = Rect.makeWH(dstWidth.toFloat(), dstHeight.toFloat()),
                samplingMode = samplingMode,
                paint = null,
                strict = true
            )
        }
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
            return SkiaImageDecoder(result.source, options)
        }
    }
}