package io.github.snd_r.komelia.image.coil

import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType
import kotlinx.coroutines.flow.StateFlow

private val logger = KotlinLogging.logger {}

class DesktopDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val decoderOptions: PlatformDecoderSettings,
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val decoder = when (decoderOptions.platformType) {
            PlatformDecoderType.VIPS, PlatformDecoderType.VIPS_ONNX -> VipsImageDecoder(source, options)
        }
        return try {
            decoder.decode()
        } catch (e: Exception) {
            logger.catching(e)
            throw e
        }
    }

    class Factory(private val decoderOption: StateFlow<PlatformDecoderSettings>) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return DesktopDecoder(result.source, options, decoderOption.value)
        }
    }
}