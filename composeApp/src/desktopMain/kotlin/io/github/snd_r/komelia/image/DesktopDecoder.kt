package io.github.snd_r.komelia.image

import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType.IMAGE_IO
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS_ONNX
import kotlinx.coroutines.flow.StateFlow

class DesktopDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val decoderOptions: PlatformDecoderSettings
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val decoder = when (decoderOptions.platformType) {
            VIPS -> VipsImageDecoder(source, options)
            VIPS_ONNX -> VipsImageDecoder(source, options)
            IMAGE_IO -> ImageIODecoder(source, options)
        }

        return decoder.decode()
    }

    class Factory(
        private val decoderOption: StateFlow<PlatformDecoderSettings>
    ) : Decoder.Factory {

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder {
            return DesktopDecoder(result.source, options, decoderOption.value)
        }
    }
}