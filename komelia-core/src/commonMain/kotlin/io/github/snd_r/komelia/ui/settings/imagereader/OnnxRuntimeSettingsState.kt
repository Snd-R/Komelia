package io.github.snd_r.komelia.ui.settings.imagereader

import io.github.snd_r.komelia.AppNotifications
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
import snd.komelia.image.OnnxRuntime
import snd.komelia.image.OnnxRuntime.DeviceInfo
import snd.komelia.image.OnnxRuntimeExecutionProvider
import snd.komelia.image.OnnxRuntimeUpscaleMode

class OnnxRuntimeSettingsState(
    private val onnxRuntime: OnnxRuntime?,
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller?,
    private val mangaJaNaiDownloader: MangaJaNaiDownloader?,
    private val appNotifications: AppNotifications,
    private val settingsRepository: ImageReaderSettingsRepository,
    private val coroutineScope: CoroutineScope,
) {
    val onnxModelPath = onnxRuntime?.selectedModelPath
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, null)
        ?: MutableStateFlow<String?>(null)

    val onnxRuntimeMode = MutableStateFlow(OnnxRuntimeUpscaleMode.NONE)
    val installProgress = MutableStateFlow<UpdateProgress?>(null)
    val installError = MutableStateFlow<String?>(null)
    val availableDevices = onnxRuntime?.availableDevices
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
        ?: MutableStateFlow(emptyList<DeviceInfo>())
    val tileSize = MutableStateFlow(0)
    val deviceId = MutableStateFlow(0)
    val currentExecutionProvider = onnxRuntime?.provider ?: OnnxRuntimeExecutionProvider.CPU

    val mangaJaNaiIsInstalled = onnxRuntime?.mangaJaNaiIsAvailable
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, false)
        ?: MutableStateFlow(false)

    private val ortInstallScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun initialize() {
        tileSize.value = settingsRepository.getOnnxRuntimeTileSize().first()
        deviceId.value = settingsRepository.getOnnxRuntimeDeviceId().first()
        onnxRuntimeMode.value = settingsRepository.getOnnxRuntimeMode().first()
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
        this.tileSize.value = tileSize
        coroutineScope.launch { settingsRepository.putOnnxRuntimeTileSize(tileSize) }
    }

    fun onDeviceIdChange(deviceId: Int) {
        this.deviceId.value = deviceId
        coroutineScope.launch { settingsRepository.putOnnxRuntimeDeviceId(deviceId) }
    }

    fun onOnnxRuntimeUpscaleModeChange(mode: OnnxRuntimeUpscaleMode) {
        this.onnxRuntimeMode.value = mode
        coroutineScope.launch { settingsRepository.putOnnxRuntimeMode(mode) }
    }

    fun onOnnxModelSelect(path: String?) {
        this.onnxRuntime?.setOnnxModelPath(path)
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
