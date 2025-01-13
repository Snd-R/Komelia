package io.github.snd_r.komelia.ui.komf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komelia.ui.settings.komf.general.KomfSettingsScreen
import io.github.snd_r.komelia.ui.settings.komf.jobs.KomfJobsScreen
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsScreen
import io.github.snd_r.komelia.ui.settings.komf.processing.KomfProcessingSettingsScreen
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsScreen
import io.github.snd_r.komelia.ui.settings.navigation.NavigationButton
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA

@Composable
fun KomfNavigationContent(
    currentScreen: Screen,
    onNavigation: (Screen) -> Unit = {},
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        NavigationButton(
            label = "Komga webui",
            onClick = { onNavigation(LoginScreen()) },
            isSelected = currentScreen is LoginScreen,
            color = contentColor,
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        Text("Komf Settings", style = MaterialTheme.typography.titleSmall)
        NavigationButton(
            label = "Connection",
            onClick = { onNavigation(KomfSettingsScreen(integrationToggleEnabled = false, showKavitaSettings = true)) },
            isSelected = currentScreen is KomfSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = "Komga Processing",
            onClick = { onNavigation(KomfProcessingSettingsScreen(KOMGA)) },
            isSelected = currentScreen is KomfProcessingSettingsScreen && currentScreen.serverType == KOMGA,
            color = contentColor,
        )
        NavigationButton(
            label = "Kavita Processing",
            onClick = { onNavigation(KomfProcessingSettingsScreen(KAVITA)) },
            isSelected = currentScreen is KomfProcessingSettingsScreen && currentScreen.serverType == KAVITA,
            color = contentColor,
        )
        NavigationButton(
            label = "Providers",
            onClick = { onNavigation(KomfProvidersSettingsScreen()) },
            isSelected = currentScreen is KomfProvidersSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = "Notifications",
            onClick = { onNavigation(KomfNotificationSettingsScreen()) },
            isSelected = currentScreen is KomfNotificationSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = "Job History",
            onClick = { onNavigation(KomfJobsScreen(false)) },
            isSelected = currentScreen is KomfJobsScreen,
            color = contentColor,
        )
    }
}
