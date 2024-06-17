package io.github.snd_r.komelia.ui.settings.decoder

import cafe.adriel.voyager.core.screen.Screen

//class DecoderSettingsScreen : Screen {
//
//    @Composable
//    override fun Content() {
//        val viewModelFactory = LocalViewModelFactory.current
//        val vm = rememberScreenModel { viewModelFactory.getDecoderSettingsViewModel() }
//        LaunchedEffect(Unit) { vm.initialize() }
//
//        SettingsScreenContainer("Image Decoder") {
//            val currentDecoderDescriptor = vm.currentDecoderDescriptor.collectAsState().value
//            val availableDecoders = vm.availableDecoders.collectAsState(emptyList()).value
//            val decoder = vm.decoderType.collectAsState().value
//            val upscale = vm.upscaleOption.collectAsState().value
//            val downscale = vm.downscaleOption.collectAsState().value
//            val onnxPath = vm.onnxPath.collectAsState().value
//
//            if (currentDecoderDescriptor == null || decoder == null || upscale == null || downscale == null || onnxPath == null) {
//                LoadingMaxSizeIndicator()
//            } else
//                DecoderSettingsContent(
//                    availableDecoders = availableDecoders,
//                    decoderDescriptor = currentDecoderDescriptor,
//                    decoder = decoder,
//                    onDecoderChange = vm::onDecoderChange,
//                    upscaleOption = upscale,
//                    onUpscaleOptionChange = vm::onUpscaleOptionChange,
//                    downscaleOption = downscale,
//                    onDownscaleOptionChange = vm::onDownscaleOptionChange,
//                    onnxPath = onnxPath,
//                    onOnnxPathChange = vm::onOnnxPathChange
//                )
//        }
//    }
//}

interface DecoderSettingsScreen : Screen

expect fun getDecoderSettingsScreen(): DecoderSettingsScreen