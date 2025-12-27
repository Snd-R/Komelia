package snd.komelia.ui.settings.imagereader

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.image.KomeliaPanelDetector
import snd.komelia.image.KomeliaUpscaler
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.availableReduceKernels
import snd.komelia.image.availableUpsamplingModes
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsState
import snd.komelia.updates.OnnxModelDownloader
import snd.komelia.updates.OnnxRuntimeInstaller

class ImageReaderSettingsViewModel(
    private val settingsRepository: ImageReaderSettingsRepository,
    private val appNotifications: AppNotifications,
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller?,
    private val onnxRuntime: OnnxRuntime?,
    private val upscaler: KomeliaUpscaler?,
    private val panelDetector: KomeliaPanelDetector?,
    private val onnxModelDownloader: OnnxModelDownloader?,
    private val coilMemoryCache: MemoryCache?,
    private val coilDiskCache: DiskCache?,
    private val readerDiskCache: DiskCache?,
) : ScreenModel {

    val onnxRuntimeSettingsState = OnnxRuntimeSettingsState(
        onnxRuntimeInstaller = onnxRuntimeInstaller,
        onnxModelDownloader = onnxModelDownloader,

        onnxRuntime = onnxRuntime,
        panelDetector = panelDetector,
        upscaler = upscaler,

        settingsRepository = settingsRepository,
        coroutineScope = screenModelScope
    )

    val upsamplingMode = MutableStateFlow(UpsamplingMode.NEAREST)
    val downsamplingKernel = MutableStateFlow(ReduceKernel.NEAREST)
    val linearLightDownsampling = MutableStateFlow(false)
    val loadThumbnailsPreview = MutableStateFlow(false)
    val volumeKeysNavigation = MutableStateFlow(false)
    val availableUpsamplingModes = availableUpsamplingModes()
    val availableDownsamplingKernels = availableReduceKernels()


    suspend fun initialize() {

        upsamplingMode.value = settingsRepository.getUpsamplingMode().first()
        downsamplingKernel.value = settingsRepository.getDownsamplingKernel().first()
        linearLightDownsampling.value = settingsRepository.getLinearLightDownsampling().first()
        loadThumbnailsPreview.value = settingsRepository.getLoadThumbnailPreviews().first()
        volumeKeysNavigation.value = settingsRepository.getVolumeKeysNavigation().first()
        onnxRuntimeSettingsState.initialize()
    }

    fun onUpsamplingModeChange(mode: UpsamplingMode) {
        upsamplingMode.value = mode
        screenModelScope.launch { settingsRepository.putUpsamplingMode(mode) }
    }

    fun onDownsamplingKernelChange(kernel: ReduceKernel) {
        downsamplingKernel.value = kernel
        screenModelScope.launch { settingsRepository.putDownsamplingKernel(kernel) }
    }

    fun onLinearLightDownsamplingChange(linear: Boolean) {
        linearLightDownsampling.value = linear
        screenModelScope.launch { settingsRepository.putLinearLightDownsampling(linear) }
    }

    fun onLoadThumbnailsPreviewChange(load: Boolean) {
        loadThumbnailsPreview.value = load
        screenModelScope.launch { settingsRepository.putLoadThumbnailPreviews(load) }
    }

    fun onVolumeKeysNavigationChange(enable: Boolean) {
        volumeKeysNavigation.value = enable
        screenModelScope.launch { settingsRepository.putVolumeKeysNavigation(enable) }
    }

    fun onClearImageCache() {
        clearImageCache()
        appNotifications.add(AppNotification.Success("Cleared image cache"))
    }

    private fun clearImageCache() {
        coilMemoryCache?.clear()
        coilDiskCache?.clear()
        readerDiskCache?.clear()
        upscaler?.clearCache()
    }
}