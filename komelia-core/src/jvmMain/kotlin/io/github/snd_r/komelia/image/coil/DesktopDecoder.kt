package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class DesktopDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        return try {
            VipsCoilImageDecoder(source, options).decode()
        } catch (e: Exception) {
            logger.catching(e)
            throw e
        }
    }

    class Factory() : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return DesktopDecoder(result.source, options)
        }
    }
}