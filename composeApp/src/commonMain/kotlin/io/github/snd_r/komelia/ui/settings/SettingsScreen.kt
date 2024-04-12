package io.github.snd_r.komelia.ui.settings

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsTab
import io.github.snd_r.komelia.ui.settings.navigation.CompactSettingsContent
import io.github.snd_r.komelia.ui.settings.navigation.RegularSettingsContent

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val currentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsNavigationViewModel(currentNavigator) }
        val width = LocalWindowWidth.current

        Navigator(
            screen = AccountSettingsTab(),
            onBackPressed = null
        ) { navigator ->
            when (width) {
                COMPACT -> {
                    CompactSettingsContent(
                        navigator = navigator,
                        screenContent = { CurrentScreen() },
                        onDismiss = { currentNavigator.pop() },
                        onLogout = vm::logout
                    )
                }

                else -> {
                    RegularSettingsContent(
                        navigator = navigator,
                        screenContent = { CurrentScreen() },
                        onDismiss = { currentNavigator.pop() },
                        onLogout = vm::logout
                    )
                }
            }
        }

        BackPressHandler { currentNavigator.pop() }
    }
}
