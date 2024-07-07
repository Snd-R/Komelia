package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS
import io.github.snd_r.komelia.platform.PlatformDecoderType.VIPS_ONNX
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.skiaSamplerCatmullRom
import io.github.snd_r.komelia.platform.skiaSamplerMitchell
import io.github.snd_r.komelia.platform.skiaSamplerNearest
import io.github.snd_r.komelia.platform.upsamplingFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.skia.SamplingMode
import java.nio.file.Path
import kotlin.io.path.readBytes

class DesktopImageDecoder(
    private val decoderSettings: StateFlow<PlatformDecoderSettings>,
    private val onnxModelsPath: StateFlow<String?>,
    private val onnxRuntimeCacheDir: Path,
) : ImageDecoder {
    private val stateFlowScope = CoroutineScope(Dispatchers.Default)
    private val samplingMode: StateFlow<SamplingMode> = decoderSettings
        .map { getSamplingMode(it.upscaleOption) }
        .stateIn(stateFlowScope, SharingStarted.Eagerly, getSamplingMode(decoderSettings.value.upscaleOption))

    override fun decode(bytes: ByteArray): ReaderImage {
        val settings = decoderSettings.value

        return when (settings.platformType) {
            VIPS -> DesktopTilingReaderImage(
                encoded = bytes,
                upsamplingMode = samplingMode,
                onnxModelPath = MutableStateFlow(null),
                onnxRuntimeCacheDir = onnxRuntimeCacheDir,
            )

            VIPS_ONNX -> {

                DesktopTilingReaderImage(
                    encoded = bytes,
                    upsamplingMode = samplingMode,
                    onnxModelPath = getOnnxRuntimeModelPathFlow(),
                    onnxRuntimeCacheDir = onnxRuntimeCacheDir,
                )
            }
        }
    }

    override fun decode(cacheFile: okio.Path): ReaderImage {
        val bytes = cacheFile.toNioPath().readBytes()
        val settings = decoderSettings.value

        return when (settings.platformType) {
            VIPS -> DesktopTilingReaderImage(
                encoded = bytes,
                upsamplingMode = samplingMode,
                onnxModelPath = MutableStateFlow(null),
                onnxRuntimeCacheDir = onnxRuntimeCacheDir,
            )

            VIPS_ONNX -> {
                DesktopTilingReaderImage(
                    encoded = bytes,
                    upsamplingMode = samplingMode,
                    onnxModelPath = getOnnxRuntimeModelPathFlow(),
                    onnxRuntimeCacheDir = onnxRuntimeCacheDir,
                )
            }
        }
    }

    private fun getOnnxRuntimeModelPathFlow(): StateFlow<Path?> {
        return onnxModelsPath.combine(decoderSettings) { maybePath, settings ->
            maybePath?.let { path ->
                val option = settings.upscaleOption
                if (option in upsamplingFilters) null
                else Path.of(path).resolve(option.value)
            }
        }.stateIn(stateFlowScope, SharingStarted.Eagerly, getCurrentOnnxModelPath())
    }

    private fun getCurrentOnnxModelPath(): Path? {
        return onnxModelsPath.value?.let { path ->
            val option = decoderSettings.value.upscaleOption
            if (option in upsamplingFilters) null
            else Path.of(path).resolve(option.value)
        }
    }

    private fun getSamplingMode(option: UpscaleOption): SamplingMode {
        return when (option) {
            skiaSamplerMitchell -> SamplingMode.MITCHELL
            skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
            skiaSamplerNearest -> SamplingMode.DEFAULT
            else -> SamplingMode.MITCHELL
        }
    }
}
