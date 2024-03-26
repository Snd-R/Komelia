package io.github.snd_r.komelia.image

import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import io.github.snd_r.komelia.image.SamplerType.IMAGEIO_LANCZOS
import io.github.snd_r.komelia.image.SamplerType.SKIA_CATMULL_ROM
import io.github.snd_r.komelia.image.SamplerType.SKIA_MITCHELL
import io.github.snd_r.komelia.image.SamplerType.SKIA_NEAREST
import io.github.snd_r.komelia.image.SamplerType.VIPS_LANCZOS_DOWN_BICUBIC_UP
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.skia.SamplingMode

class DesktopDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val samplerType: SamplerType
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val decoder = when (samplerType) {
            VIPS_LANCZOS_DOWN_BICUBIC_UP -> VipsImageDecoder(source, options)
            IMAGEIO_LANCZOS -> ImageIODecoder(source, options)
            SKIA_MITCHELL -> SkiaImageDecoder(source, options, SamplingMode.MITCHELL)
            SKIA_CATMULL_ROM -> SkiaImageDecoder(source, options, SamplingMode.CATMULL_ROM)
            SKIA_NEAREST -> SkiaImageDecoder(source, options, SamplingMode.DEFAULT)
        }

        return decoder.decode()
    }

    class Factory(
        private val decoderOption: StateFlow<SamplerType>
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