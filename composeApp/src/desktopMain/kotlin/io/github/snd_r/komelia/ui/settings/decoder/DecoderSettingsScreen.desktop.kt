package io.github.snd_r.komelia.ui.settings.decoder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.LocalDesktopViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

actual fun getDecoderSettingsScreen(): DecoderSettingsScreen {
    return DesktopDecoderSettingsScreen()
}

class DesktopDecoderSettingsScreen : DecoderSettingsScreen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalDesktopViewModelFactory.currentOrThrow
        val vm = rememberScreenModel { viewModelFactory.getDecoderSettingsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("Image Decoder") {
            val currentDecoderDescriptor = vm.currentDecoderDescriptor.collectAsState().value
            val availableDecoders = vm.availableDecoders.collectAsState(emptyList()).value
            val decoder = vm.decoderType.collectAsState().value
            val upscale = vm.upscaleOption.collectAsState().value
            val downscale = vm.downscaleOption.collectAsState().value
            val onnxPath = vm.onnxModelsPath.collectAsState().value
            val updateProgress = vm.ortUpdateProgress.collectAsState().value
            val installError = vm.ortInstallError.collectAsState().value

            if (currentDecoderDescriptor == null || decoder == null || upscale == null || downscale == null || onnxPath == null) {
                LoadingMaxSizeIndicator()
            } else
                DecoderSettingsContent(
                    availableDecoders = availableDecoders,
                    decoderDescriptor = currentDecoderDescriptor,
                    decoder = decoder,
                    onDecoderChange = vm::onDecoderChange,
                    upscaleOption = upscale,
                    onUpscaleOptionChange = vm::onUpscaleOptionChange,
                    downscaleOption = downscale,
                    onDownscaleOptionChange = vm::onDownscaleOptionChange,
                    onnxPath = onnxPath,
                    onOnnxPathChange = vm::onOnnxPathChange,
                    onOrtProviderInstall = vm::onOrtInstallRequest,
                    onOrtProviderInstallCancel = vm::onOrtInstallCancel,
                    ortInstallProgress = updateProgress,
                    ortInstallError = installError,
                    onOrtInstallErrorDismiss = vm::onOrtInstallErrorDismiss,
                    onCacheClear = vm::onClearImageCache
                )
        }
    }
}
