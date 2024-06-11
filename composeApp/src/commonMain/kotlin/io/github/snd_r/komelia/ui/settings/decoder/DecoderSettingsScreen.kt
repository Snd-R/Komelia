package io.github.snd_r.komelia.ui.settings.decoder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class DecoderSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getDecoderSettingsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("Decoder") {
            val currentDecoderDescriptor = vm.currentDecoderDescriptor.collectAsState().value
            val availableDecoders = vm.availableDecoders.collectAsState(emptyList()).value
            val decoder = vm.decoderType.collectAsState().value
            val upscale = vm.upscaleOption.collectAsState().value
            val downscale = vm.downscaleOption.collectAsState().value
            val onnxPath = vm.onnxPath.collectAsState().value

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
                    onOnnxPathChange = vm::onOnnxPathChange
                )
        }
    }
}