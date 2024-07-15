package io.github.snd_r.komelia.image.coil

import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.ImageFormat.GRAYSCALE_8
import io.github.snd_r.ImageFormat.RGBA_8888
import io.github.snd_r.VipsImage
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
        // assuming that disk cache is enabled, file should always be already written to disk
        // let vips handle file read
        val file = source.file()
        val result = measureTimedValue {
            val decoded = if (options.size.isOriginal) VipsImage.decodeFromFile(file.toString())
            else {
                val dstWidth = options.size.width.pxOrElse { vipsMaxSize }
                val dstHeight = options.size.height.pxOrElse { vipsMaxSize }
                val crop = options.scale == Scale.FILL
                        && options.size.width != Dimension.Undefined && options.size.height != Dimension.Undefined

                VipsImage.thumbnail(file.toString(), dstWidth, dstHeight, crop)
            }

//            val bitmap = VipsBitmapFactory.createSkiaBitmap(decoded)
            val bitmap = decoded.toBitmap()
            decoded.close()
            bitmap.setImmutable()

            DecodeResult(
                image = bitmap.asImage(),
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
        val colorInfo = when (type) {
            GRAYSCALE_8 -> ColorInfo(
                ColorType.GRAY_8,
                ColorAlphaType.UNPREMUL,
                ColorSpace.sRGB
            )

            RGBA_8888 -> ColorInfo(
                ColorType.RGBA_8888,
                ColorAlphaType.UNPREMUL,
                ColorSpace.sRGB
            )
        }

        val imageInfo = ImageInfo(colorInfo, width, height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        bitmap.installPixels(getBytes())
        return bitmap
    }
}