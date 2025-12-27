package snd.komelia.ui.settings.imagereader.onnxruntime

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.image.KomeliaPanelDetector
import snd.komelia.image.KomeliaUpscaler
import snd.komelia.image.UpscaleMode
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.updates.OnnxModelDownloader
import snd.komelia.updates.OnnxRuntimeInstaller
import snd.komelia.updates.UpdateProgress

private val logger = KotlinLogging.logger { }

class OnnxRuntimeSettingsState(
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller?,
    private val onnxModelDownloader: OnnxModelDownloader?,

    private val onnxRuntime: OnnxRuntime?,
    private val upscaler: KomeliaUpscaler?,
    private val panelDetector: KomeliaPanelDetector?,

    private val settingsRepository: ImageReaderSettingsRepository,
    private val coroutineScope: CoroutineScope,
) {
    val deviceId = MutableStateFlow(0)
    val availableDevices = runCatching { onnxRuntime?.enumerateDevices() ?: emptyList() }
        .onFailure { logger.catching(it) }
        .getOrDefault(emptyList())

    val upscaleModelPath = upscaler?.userModelPath
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, null)
        ?: MutableStateFlow<PlatformFile?>(null)
    val upscaleMode = MutableStateFlow(UpscaleMode.NONE)
    val upscalerTileSize = MutableStateFlow(0)
    val currentExecutionProvider = ortExecutionProvider ?: CPU

    val mangaJaNaiIsInstalled = upscaler?.mangaJaNaiIsAvailable
        ?.stateIn(coroutineScope, SharingStarted.Eagerly, false)
        ?: MutableStateFlow(false)
    val panelModelIsDownloaded = panelDetector?.isAvailable ?: MutableStateFlow(false)


    suspend fun initialize() {
        upscalerTileSize.value = settingsRepository.getOnnxRuntimeTileSize().first()
        deviceId.value = settingsRepository.getOnnxRuntimeDeviceId().first()
        upscaleMode.value = settingsRepository.getUpscalerMode().first()
    }

    fun onInstallRequest(provider: OnnxRuntimeExecutionProvider): Flow<UpdateProgress> {
        checkNotNull(onnxRuntimeInstaller)
        return onnxRuntimeInstaller.install(provider)
    }

    fun onMangaJaNaiDownloadRequest(): Flow<UpdateProgress> {
        checkNotNull(onnxModelDownloader) { "onnx model downloader is not initialized" }
        return onnxModelDownloader.mangaJaNaiDownload()
    }

    fun onPanelDetectionModelDownloadRequest(): Flow<UpdateProgress> {
        checkNotNull(onnxModelDownloader) { "onnx model downloader is not initialized" }
        return onnxModelDownloader.panelDownload()
    }

    fun onTileSizeChange(tileSize: Int) {
        this.upscalerTileSize.value = tileSize
        coroutineScope.launch { settingsRepository.putOnnxRuntimeTileSize(tileSize) }
    }

    fun onDeviceIdChange(deviceId: Int) {
        this.deviceId.value = deviceId
        coroutineScope.launch { settingsRepository.putOnnxRuntimeDeviceId(deviceId) }
    }

    fun onUpscaleModeChange(mode: UpscaleMode) {
        this.upscaleMode.value = mode
        coroutineScope.launch { settingsRepository.putUpscalerMode(mode) }
    }

    fun onUpscaleModelPathChange(path: PlatformFile?) {
        this.upscaler?.setOnnxModelPath(path)
    }
}

expect fun isOnnxRuntimeSupported(): Boolean
expect fun isOnnxRuntimeInstalled(): Boolean
expect fun onnxRuntimeLoadError(): String?
expect fun supportedOnnxRuntimeExecutionProviders(): List<OnnxRuntimeExecutionProvider>
expect val ortExecutionProvider: OnnxRuntimeExecutionProvider?
