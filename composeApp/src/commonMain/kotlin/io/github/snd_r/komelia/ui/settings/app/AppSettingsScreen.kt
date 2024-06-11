package io.github.snd_r.komelia.ui.settings.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class AppSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAppearanceViewModel() }

        SettingsScreenContainer("Appearance") {
            AppearanceSettingsContent(
                cardWidth = vm.cardWidth,
                onCardWidthChange = vm::onCardWidthChange,
            )
        }
    }
}