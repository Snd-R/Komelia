package io.github.snd_r.komelia.ui.settings.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory

class AppSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAppearanceViewModel() }

        AppSettingsContent(
            cardWidth = vm.cardWidth,
            onCardWidthChange = vm::onCardWidthChange,
            decoder = vm.decoder,
            onDecoderTypeChange = vm::onDecoderChange
        )
    }
}