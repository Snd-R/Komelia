package io.github.snd_r.komelia.ui.settings.decoder

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.ImageLoader
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.FilesystemSettingsRepository
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import io.github.snd_r.komelia.updates.UpdateProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class DecoderSettingsViewModel(
    private val settingsRepository: FilesystemSettingsRepository,
    private val imageLoader: ImageLoader,
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller,
    private val appNotifications: AppNotifications,
    val availableDecoders: Flow<List<PlatformDecoderDescriptor>>
) : ScreenModel {
    val currentDecoderDescriptor = MutableStateFlow<PlatformDecoderDescriptor?>(null)
    val decoderType = MutableStateFlow<PlatformDecoderType?>(null)
    val upscaleOption = MutableStateFlow<UpscaleOption?>(null)
    val downscaleOption = MutableStateFlow<DownscaleOption?>(null)
    val onnxModelsPath = MutableStateFlow<String?>(null)
    val ortUpdateProgress = MutableStateFlow<UpdateProgress?>(null)
    val ortInstallError = MutableStateFlow<String?>(null)

    private val ortInstallScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun initialize() {

        val decoder = settingsRepository.getDecoderType().first()
        decoderType.value = decoder.platformType
        upscaleOption.value = decoder.upscaleOption
        downscaleOption.value = decoder.downscaleOption

        onnxModelsPath.value = settingsRepository.getOnnxModelsPath().first()

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
        this.onnxModelsPath.value = path
        screenModelScope.launch { settingsRepository.putOnnxModelsPath(path) }
    }

    suspend fun onOrtInstallRequest(provider: OnnxRuntimeExecutionProvider) {
        appNotifications.runCatchingToNotifications {
            ortUpdateProgress.value = UpdateProgress(0, 0)
            onnxRuntimeInstaller.install(provider)
                .conflate()
                .onCompletion { ortUpdateProgress.value = null }
                .collect { ortUpdateProgress.value = it }
        }.onFailure {
            ortInstallError.value = "${it.javaClass.simpleName} ${it.message}" ?: "Unknown error"
            ortUpdateProgress.value = null
        }
    }

    fun onOrtInstallCancel() {
        ortInstallScope.coroutineContext.cancelChildren()
    }

    fun onOrtInstallErrorDismiss() {
        ortInstallError.value = null
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