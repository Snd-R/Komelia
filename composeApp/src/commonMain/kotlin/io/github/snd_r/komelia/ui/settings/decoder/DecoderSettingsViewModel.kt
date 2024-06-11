package io.github.snd_r.komelia.ui.settings.decoder

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.ImageLoader
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DecoderSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val imageLoader: ImageLoader,
    val availableDecoders: Flow<List<PlatformDecoderDescriptor>>
) : ScreenModel {
    val currentDecoderDescriptor = MutableStateFlow<PlatformDecoderDescriptor?>(null)
    val decoderType = MutableStateFlow<PlatformDecoderType?>(null)
    val upscaleOption = MutableStateFlow<UpscaleOption?>(null)
    val downscaleOption = MutableStateFlow<DownscaleOption?>(null)
    val onnxPath = MutableStateFlow<String?>(null)

    suspend fun initialize() {
        val decoder = settingsRepository.getDecoderType().first()
        decoderType.value = decoder.platformType
        upscaleOption.value = decoder.upscaleOption
        downscaleOption.value = decoder.downscaleOption

        onnxPath.value = settingsRepository.getOnnxModelsPath().first()

        availableDecoders.collect { decoders ->
            currentDecoderDescriptor.value = decoders.first { it.platformType == decoderType.value }
        }
    }

    fun onDecoderChange(type: PlatformDecoderType) {
        val descriptor = requireNotNull(currentDecoderDescriptor.value)
        val newUpscaleOption = descriptor.upscaleOptions.first()
        val newDownscaleOption = descriptor.downscaleOptions.first()

        screenModelScope.launch {
            decoderType.value = type
            currentDecoderDescriptor.value = availableDecoders.first().first { it.platformType == type }
            upscaleOption.value = newUpscaleOption
            downscaleOption.value = newDownscaleOption
            updateDecoderSettings()
        }
    }

    fun onUpscaleOptionChange(option: UpscaleOption) {
        val currentDecoder = requireNotNull(currentDecoderDescriptor.value)
        require(currentDecoder.upscaleOptions.contains(option))
        this.upscaleOption.value = option

        updateDecoderSettings()
    }

    fun onDownscaleOptionChange(option: DownscaleOption) {
        val currentDecoder = requireNotNull(currentDecoderDescriptor.value)
        require(currentDecoder.downscaleOptions.contains(option))
        this.downscaleOption.value = option

        updateDecoderSettings()
    }

    fun onOnnxPathChange(path: String) {
        this.onnxPath.value = path
        screenModelScope.launch { settingsRepository.putOnnxModelsPath(path) }
    }

    private fun updateDecoderSettings() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()

        val currentDecoder = requireNotNull(currentDecoderDescriptor.value)
        val upscaleOption = requireNotNull(upscaleOption.value)
        val downscaleOption = requireNotNull(downscaleOption.value)
        screenModelScope.launch {
            settingsRepository.putDecoderType(
                PlatformDecoderSettings(
                    platformType = currentDecoder.platformType,
                    upscaleOption = upscaleOption,
                    downscaleOption = downscaleOption
                )
            )
        }

    }
}