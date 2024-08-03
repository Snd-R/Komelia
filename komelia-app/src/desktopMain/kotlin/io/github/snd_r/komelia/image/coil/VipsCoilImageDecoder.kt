package io.github.snd_r.komelia.image.coil

import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.request.Options
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsImage
import io.github.snd_r.VipsImage.Companion.DIMENSION_MAX_SIZE
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalCoilApi::class)
class VipsCoilImageDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val result = measureTimedValue {
            vipsDecode().use { vipsImage ->
                DecodeResult(
                    image = VipsBitmapFactory.toSkiaBitmap(vipsImage).asImage(),
                    isSampled = !options.size.isOriginal
                )
            }
        }

        logger.info {
            val timeSpent = result.duration.toString(DurationUnit.MILLISECONDS, 2)
            val sizeInMb = "%.2f".format(result.value.image.size.toFloat() / 1024 / 1024)
            "time spent: ${timeSpent}; " +
                    "size: ${result.value.image.width}x${result.value.image.height}; " +
                    "memory usage ${sizeInMb}MB"
        }

        return result.value
    }

    private fun vipsDecode(): VipsImage {
        // assuming that disk cache is enabled, file should always be already written to disk
        // let vips handle file read
        val filePath = source.file()
        if (options.size.isOriginal) return VipsImage.decodeFromFile(filePath.toString())

        val crop = options.scale == Scale.FILL
        val dstWidth = options.size.width.pxOrElse { DIMENSION_MAX_SIZE }
        val dstHeight = options.size.height.pxOrElse { DIMENSION_MAX_SIZE }
        require(dstWidth != DIMENSION_MAX_SIZE && dstHeight != DIMENSION_MAX_SIZE)

        return VipsImage.thumbnail(filePath.toString(), dstWidth, dstHeight, crop)
    }
}