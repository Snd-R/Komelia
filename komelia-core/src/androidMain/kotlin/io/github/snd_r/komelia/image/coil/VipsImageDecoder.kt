package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.size.Scale
import coil3.size.isOriginal
import coil3.size.pxOrElse
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsImage
import io.github.snd_r.VipsImage.Companion.DIMENSION_MAX_SIZE


@OptIn(ExperimentalCoilApi::class)
class VipsImageDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult {
        return vipsDecode().use { vipsImage ->
            DecodeResult(
                image = VipsBitmapFactory.createHardwareBitmap(vipsImage).asImage(),
                isSampled = !options.size.isOriginal
            )
        }
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

    class Factory : Decoder.Factory {
        override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader) =
            VipsImageDecoder(result.source, options)
    }
}
