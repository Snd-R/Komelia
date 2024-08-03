package io.github.snd_r.komelia.ui.settings.decoder

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.ImageLoader
import io.github.snd_r.OnnxRuntimeSharedLibraries
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CPU
import io.github.snd_r.OnnxRuntimeUpscaler
import io.github.snd_r.OnnxRuntimeUpscaler.DeviceInfo
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.mangaJaNai
import io.github.snd_r.komelia.settings.DesktopSettingsRepository
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import io.github.snd_r.komelia.updates.UpdateProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DecoderSettingsViewModel(
    private val settingsRepository: DesktopSettingsRepository,
    private val imageLoader: ImageLoader,
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller,
    private val mangaJaNaiDownloader: MangaJaNaiDownloader,
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
    val gpuInfo = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val tileSize = MutableStateFlow(0)
    val deviceId = MutableStateFlow(0)

    val mangaJaNaiIsAvailable = MutableStateFlow(false)

    private val ortInstallScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun initialize() {
        onnxModelsPath.value = settingsRepository.getOnnxModelsPath().first()
        tileSize.value = settingsRepository.getOnnxRuntimeTileSize().first()
        deviceId.value = settingsRepository.getOnnxRuntimeDeviceId().first()

        settingsRepository.getDecoderSettings().onEach { decoder ->
            decoderType.value = decoder.platformType
            upscaleOption.value = decoder.upscaleOption
            downscaleOption.value = decoder.downscaleOption
        }.launchIn(screenModelScope)

        availableDecoders.onEach { decoders ->
            val newDescriptor = decoders.firstOrNull { it.platformType == decoderType.value }
            currentDecoderDescriptor.value = newDescriptor
            mangaJaNaiIsAvailable.value = newDescriptor?.upscaleOptions?.contains(mangaJaNai) ?: false
        }.launchIn(screenModelScope)

        try {
            if (OnnxRuntimeSharedLibraries.executionProvider != CPU)
                gpuInfo.value = OnnxRuntimeUpscaler.enumerateDevices()
        } catch (e: Throwable) {
            appNotifications.add(AppNotification.Error(e.message ?: "Failed to get device list"))
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
                .collect {
                    ortUpdateProgress.value = it
                    delay(100)
                }
        }.onFailure {
            ortInstallError.value = "${it.javaClass.simpleName} ${it.message}"
            ortUpdateProgress.value = null
        }
    }

    fun onMangaJaNaiDownloadRequest(): Flow<UpdateProgress> {
        return mangaJaNaiDownloader.download()
    }

    fun onTileSizeChange(tileSize: Int) {
        this.tileSize.value = tileSize
        ortInstallScope.launch { settingsRepository.putOnnxRuntimeTileSize(tileSize) }
    }

    fun onDeviceIdChange(deviceId: Int) {
        this.deviceId.value = deviceId
        ortInstallScope.launch { settingsRepository.putOnnxRuntimeDeviceId(deviceId) }
    }


    fun onOrtInstallCancel() {
        ortInstallScope.coroutineContext.cancelChildren()
    }

    fun onOrtInstallErrorDismiss() {
        ortInstallError.value = null
    }

    fun onClearImageCache() {
        clearImageCache()
        appNotifications.add(AppNotification.Success("Cleared image cache"))
    }

    private fun clearImageCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }

    private fun updateDecoderSettings() {
        clearImageCache()

        val currentDecoder = requireNotNull(currentDecoderDescriptor.value)
        val upscaleOption = requireNotNull(upscaleOption.value)
        val downscaleOption = requireNotNull(downscaleOption.value)
        screenModelScope.launch {
            settingsRepository.putDecoderSettings(
                PlatformDecoderSettings(
                    platformType = currentDecoder.platformType,
                    upscaleOption = upscaleOption,
                    downscaleOption = downscaleOption
                )
            )
        }

    }
}