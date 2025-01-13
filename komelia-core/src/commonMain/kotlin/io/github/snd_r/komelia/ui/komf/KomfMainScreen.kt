package io.github.snd_r.komelia.ui.komf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.ui.settings.SettingsScreenLayout
import io.github.snd_r.komelia.ui.settings.komf.general.KomfSettingsScreen
import io.github.snd_r.komelia.ui.settings.settingsDesktopNavMenuWidth
import io.github.snd_r.komelia.ui.settings.settingsDesktopTopPadding

class KomfMainScreen : Screen {

    @Composable
    override fun Content() {
        Navigator(
            screen = KomfSettingsScreen(integrationToggleEnabled = false, showKavitaSettings = true),
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
                            KomfNavigationContent(
                                currentScreen = navigator.lastItem,
                                onNavigation = { navigator.replaceAll(it) },
                                contentColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.width(settingsDesktopNavMenuWidth)
                            )
                        }
                    },
                    dismissButton = {},
                    content = { CrossfadeTransition(navigator) },
                )
            }
        }
    }
}