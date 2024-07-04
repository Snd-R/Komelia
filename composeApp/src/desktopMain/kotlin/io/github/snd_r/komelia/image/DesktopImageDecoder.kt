package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType.IMAGE_IO
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS_ONNX
import io.github.snd_r.komelia.platform.vipsUpscaleBicubic
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.skia.SamplingMode
import java.nio.file.Path
import kotlin.io.path.readBytes

class DesktopImageDecoder(
    private val decoderSettings: StateFlow<PlatformDecoderSettings>,
    private val onnxModelsPath: StateFlow<String?>,
    private val samplingMode: StateFlow<SamplingMode>,
    private val tempDir: Path,
) : ImageDecoder {

    override fun decode(bytes: ByteArray): ReaderImage {
        val settings = decoderSettings.value

        return when (settings.platformType) {
            VIPS -> VipsTilingReaderImage(
                encoded = bytes,
                onnxModelPath = null,
            )

            VIPS_ONNX -> {
                val modelPath = when (settings.upscaleOption) {
                    vipsUpscaleBicubic -> null
                    else -> "$onnxModelsPath/${settings.upscaleOption.value}"
                }
                VipsTilingReaderImage(
                    encoded = bytes,
                    onnxModelPath = modelPath,
                )
            }

            IMAGE_IO -> TODO()
        }
    }

    override fun decode(cacheFile: okio.Path): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        val settings = decoderSettings.value

        return when (settings.platformType) {
            VIPS -> VipsTilingReaderImage(
                encoded = bytes,
                onnxModelPath = null,
            )

            VIPS_ONNX -> {
                val modelPath = when (settings.upscaleOption) {
                    vipsUpscaleBicubic -> null
                    else -> "$onnxModelsPath/${settings.upscaleOption.value}"
                }
                VipsTilingReaderImage(
                    encoded = bytes,
                    onnxModelPath = modelPath,
                )
            }

            IMAGE_IO -> TODO()
        }
    }
}

