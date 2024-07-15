package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsImage
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

            val bitmap = VipsBitmapFactory.createHardwareBitmap(decoded)
            decoded.close()

            DecodeResult(
                image = bitmap.asImage(),
                isSampled = !options.size.isOriginal
            )
        }

        return result.value
    }

    class Factory : Decoder.Factory {
        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader) =
            VipsImageDecoder(result.source, options)
    }
}
