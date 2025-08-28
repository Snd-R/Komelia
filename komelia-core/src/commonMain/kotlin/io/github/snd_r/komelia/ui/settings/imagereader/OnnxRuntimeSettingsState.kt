package io.github.snd_r.komelia.ui.settings.imagereader

import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.KomeliaUpscaler
import io.github.snd_r.komelia.image.UpscaleMode
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import io.github.snd_r.komelia.updates.UpdateProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider

class OnnxRuntimeSettingsState(
    private val upscaler: KomeliaUpscaler?,
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller?,
    private val mangaJaNaiDownloader: MangaJaNaiDownloader?,
    private val appNotifications: AppNotifications,
    private val settingsRepository: ImageReaderSettingsRepository,
    private val coroutineScope: CoroutineScope,
) {
    val deviceId = MutableStateFlow(0)
    val availableDevices = upscaler?.availableDevices
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
        ?: MutableStateFlow(emptyList())

    val upscaleModelPath = upscaler?.userModelPath
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, null)
        ?: MutableStateFlow<String?>(null)
    val upscaleMode = MutableStateFlow(UpscaleMode.NONE)
    val upscalerTileSize = MutableStateFlow(0)
    val currentExecutionProvider = upscaler?.provider ?: OnnxRuntimeExecutionProvider.CPU


    val installProgress = MutableStateFlow<UpdateProgress?>(null)
    val installError = MutableStateFlow<String?>(null)

    val mangaJaNaiIsInstalled = upscaler?.mangaJaNaiIsAvailable
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, false)
        ?: MutableStateFlow(false)

    private val ortInstallScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun initialize() {
        upscalerTileSize.value = settingsRepository.getOnnxRuntimeTileSize().first()
        deviceId.value = settingsRepository.getOnnxRuntimeDeviceId().first()
        upscaleMode.value = settingsRepository.getOnnxRuntimeMode().first()
    }


    suspend fun onInstallRequest(provider: OnnxRuntimeExecutionProvider) {
        checkNotNull(onnxRuntimeInstaller)
        appNotifications.runCatchingToNotifications {
            installProgress.value = UpdateProgress(0, 0)
            onnxRuntimeInstaller.install(provider)
                .conflate()
                .onCompletion { installProgress.value = null }
                .collect {
                    installProgress.value = it
                    delay(100)
                }
        }.onFailure {
            installError.value = "${it::class.simpleName} ${it.message}"
            installProgress.value = null
        }
    }

    fun onMangaJaNaiDownloadRequest(): Flow<UpdateProgress> {
        checkNotNull(mangaJaNaiDownloader)
        return mangaJaNaiDownloader.download()
    }

    fun onTileSizeChange(tileSize: Int) {
        this.upscalerTileSize.value = tileSize
        coroutineScope.launch { settingsRepository.putOnnxRuntimeTileSize(tileSize) }
    }

    fun onDeviceIdChange(deviceId: Int) {
        this.deviceId.value = deviceId
        coroutineScope.launch { settingsRepository.putOnnxRuntimeDeviceId(deviceId) }
    }

    fun onOnnxRuntimeUpscaleModeChange(mode: UpscaleMode) {
        this.upscaleMode.value = mode
        coroutineScope.launch { settingsRepository.putOnnxRuntimeMode(mode) }
    }

    fun onOnnxModelSelect(path: String?) {
        this.upscaler?.setOnnxModelPath(path)
    }

    fun onInstallationCancel() {
        ortInstallScope.coroutineContext.cancelChildren()
    }

    fun onInstallErrorDismiss() {
        installError.value = null
    }

    fun onDispose() {
        ortInstallScope.cancel()
    }
}

expect fun isOnnxRuntimeSupported(): Boolean
expect fun isOnnxRuntimeInstalled(): Boolean
expect fun onnxRuntimeLoadError(): String?
expect fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider>
