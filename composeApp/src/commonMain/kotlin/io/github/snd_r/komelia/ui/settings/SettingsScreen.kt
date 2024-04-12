package io.github.snd_r.komelia.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
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
        LaunchedEffect(Unit) { vm.initialize() }
        val width = LocalWindowWidth.current

        Navigator(
            screen = AccountSettingsTab(),
            onBackPressed = null
        ) { navigator ->
            when (width) {
                COMPACT, MEDIUM -> {
                    CompactSettingsContent(
                        navigator = navigator,
                        screenContent = { CurrentScreen() },
                        hasMediaErrors = vm.hasMediaErrors,
                        onDismiss = { currentNavigator.pop() },
                        onLogout = vm::logout
                    )
                }

                else -> {
                    RegularSettingsContent(
                        navigator = navigator,
                        screenContent = { CurrentScreen() },
                        hasMediaErrors = vm.hasMediaErrors,
                        onDismiss = { currentNavigator.pop() },
                        onLogout = vm::logout
                    )
                }
            }
        }

        BackPressHandler { currentNavigator.pop() }
    }
}
