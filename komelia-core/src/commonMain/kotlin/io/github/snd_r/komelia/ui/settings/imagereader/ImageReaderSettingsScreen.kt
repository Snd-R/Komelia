package io.github.snd_r.komelia.ui.settings.imagereader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class ImageReaderSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getImageReaderSettingsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("Image Reader") {
            ImageReaderSettingsContent(
                availableUpsamplingModes = vm.availableUpsamplingModes,
                upsamplingMode = vm.upsamplingMode.collectAsState().value,
                onUpsamplingModeChange = vm::onUpsamplingModeChange,

                availableDownsamplingKernels = vm.availableDownsamplingKernels,
                downsamplingKernel = vm.downsamplingKernel.collectAsState().value,
                onDownsamplingKernelChange = vm::onDownsamplingKernelChange,
                downsampleInLinearLight = vm.linearLightDownsampling.collectAsState().value,
                onDownsampleInLinearLightChange = vm::onLinearLightDownsamplingChange,
                loadThumbnailPreviews = vm.loadThumbnailsPreview.collectAsState().value,
                onLoadThumbnailPreviewsChange = vm::onLoadThumbnailsPreviewChange,
                volumeKeysNavigation = vm.volumeKeysNavigation.collectAsState().value,
                onVolumeKeysNavigationChange = vm::onVolumeKeysNavigationChange,

                onCacheClear = vm::onClearImageCache,
                onnxRuntimeSettingsState = vm.onnxRuntimeSettingsState,
            )
        }
    }
}
