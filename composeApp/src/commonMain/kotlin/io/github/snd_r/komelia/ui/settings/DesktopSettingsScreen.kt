package io.github.snd_r.komelia.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.CrossfadeTransition
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsScreen
import io.github.snd_r.komelia.ui.settings.navigation.SettingsNavigationMenu
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.roundToInt

val settingsDesktopNavMenuWidth = 250.dp
val settingsDesktopContentWidth = 700.dp
val settingsDesktopTopPadding = 50.dp

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val currentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsNavigationViewModel(currentNavigator) }

        LaunchedEffect(Unit) { vm.initialize() }
        LaunchedEffect(Unit) { keyEvents.collect { if (it.key == Key.Escape) currentNavigator.pop() } }

        Navigator(
            screen = AccountSettingsScreen(),
            onBackPressed = null
        ) { navigator ->
            Column {
                PlatformTitleBar()
                SettingsScreenLayout(
                    navMenu = {
                        Row(
                            Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(top = settingsDesktopTopPadding, start = 10.dp, end = 10.dp)
                        ) {
                            Spacer(Modifier.weight(1f))
                            SettingsNavigationMenu(
                                currentScreen = navigator.lastItem,
                                onNavigation = { navigator.replaceAll(it) },
                                hasMediaErrors = vm.hasMediaErrors,
                                onLogout = vm::logout,
                                contentColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.width(settingsDesktopNavMenuWidth)
                            )
                        }
                    },
                    dismissButton = {
                        OutlinedIconButton(
                            onClick = { currentNavigator.pop() },
                            modifier = Modifier.cursorForHand().padding(top = settingsDesktopTopPadding),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            content = { Icon(Icons.Default.Close, null) }
                        )
                    },
                    content = { CrossfadeTransition(navigator) },
                )

            }
        }
        BackPressHandler { currentNavigator.pop() }

    }

    @Composable
    private fun SettingsScreenLayout(
        navMenu: @Composable () -> Unit,
        content: @Composable () -> Unit,
        dismissButton: @Composable () -> Unit,
    ) = Layout(
        modifier = Modifier.fillMaxSize(),
        contents = listOf(navMenu, content, dismissButton)
    ) { (navMenuMeasurable, contentMeasurable, dismissMeasurable), constraints ->
        val navWidth = settingsDesktopNavMenuWidth.roundToPx()
        val contentWidth = settingsDesktopContentWidth.roundToPx()
        val padding =
            ((constraints.maxWidth - (navWidth + contentWidth)).toFloat() / 2).roundToInt().coerceAtLeast(0)

        val contentPlaceable = contentMeasurable.first()
            .measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = padding + contentWidth.coerceAtMost(constraints.maxWidth - navWidth)
                )
            )

        val navMenuPlaceable = navMenuMeasurable.first()
            .measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = padding + navWidth
                )
            )
        val dismissPlaceable = dismissMeasurable.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(constraints.maxWidth, constraints.maxHeight) {
            navMenuPlaceable.placeRelative(
                0,
                0
            )
            contentPlaceable.placeRelative(
                padding + navWidth,
                0
            )
            dismissPlaceable.placeRelative(
                (padding + navWidth + contentWidth).coerceAtMost(constraints.maxWidth - dismissPlaceable.width),
                0
            )
        }
    }
}
