package io.github.snd_r.komelia.ui.settings.appearance

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory

class AppearanceScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAppearanceViewModel() }

        AppearanceContent(
            cardWidth = vm.cardWidth,
            onCardWidthChange = vm::onCardWidthChange
        )
    }
}