package io.github.snd_r.komelia.ui.settings.imagereader

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.image.availableReduceKernels
import io.github.snd_r.komelia.image.availableUpsamplingModes
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.image.OnnxRuntime
import snd.komelia.image.ReduceKernel

class ImageReaderSettingsViewModel(
    private val settingsRepository: ImageReaderSettingsRepository,
    private val appNotifications: AppNotifications,
    private val onnxRuntimeInstaller: OnnxRuntimeInstaller?,
    private val onnxRuntime: OnnxRuntime?,
    private val mangaJaNaiDownloader: MangaJaNaiDownloader?,
    private val coilMemoryCache: MemoryCache?,
    private val coilDiskCache: DiskCache?,
    private val readerDiskCache: DiskCache?,
) : ScreenModel {

    val onnxRuntimeSettingsState = OnnxRuntimeSettingsState(
        onnxRuntime = onnxRuntime,
        onnxRuntimeInstaller = onnxRuntimeInstaller,
        mangaJaNaiDownloader = mangaJaNaiDownloader,
        appNotifications = appNotifications,
        settingsRepository = settingsRepository,
        coroutineScope = screenModelScope
    )

    val upsamplingMode = MutableStateFlow<UpsamplingMode>(UpsamplingMode.NEAREST)
    val downsamplingKernel = MutableStateFlow<ReduceKernel>(ReduceKernel.NEAREST)
    val linearLightDownsampling = MutableStateFlow(false)
    val availableUpsamplingModes = availableUpsamplingModes()
    val availableDownsamplingKernels = availableReduceKernels()


    suspend fun initialize() {

        upsamplingMode.value = settingsRepository.getUpsamplingMode().first()
        downsamplingKernel.value = settingsRepository.getDownsamplingKernel().first()
        linearLightDownsampling.value = settingsRepository.getLinearLightDownsampling().first()
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

    fun onClearImageCache() {
        clearImageCache()
        appNotifications.add(AppNotification.Success("Cleared image cache"))
    }

    private fun clearImageCache() {
        coilMemoryCache?.clear()
        coilDiskCache?.clear()
        readerDiskCache?.clear()
        onnxRuntime?.clearCache()
    }

    override fun onDispose() {
        onnxRuntimeSettingsState.onDispose()
    }
}