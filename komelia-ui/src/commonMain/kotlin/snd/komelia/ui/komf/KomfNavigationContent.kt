package snd.komelia.ui.komf

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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_connection
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_job_history
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_komga_webui
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_notifications
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_kavita
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_processing_komga
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_providers
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_settings_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.login.LoginScreen
import snd.komelia.ui.settings.komf.general.KomfSettingsScreen
import snd.komelia.ui.settings.komf.jobs.KomfJobsScreen
import snd.komelia.ui.settings.komf.notifications.KomfNotificationSettingsScreen
import snd.komelia.ui.settings.komf.processing.KomfProcessingSettingsScreen
import snd.komelia.ui.settings.komf.providers.KomfProvidersSettingsScreen
import snd.komelia.ui.settings.navigation.NavigationButton
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
            label = stringResource(Res.string.komf_komga_webui),
            onClick = { onNavigation(LoginScreen()) },
            isSelected = currentScreen is LoginScreen,
            color = contentColor,
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        Text(
            stringResource(Res.string.komf_settings_title),
            style = MaterialTheme.typography.titleSmall
        )
        NavigationButton(
            label = stringResource(Res.string.komf_connection),
            onClick = { onNavigation(KomfSettingsScreen(integrationToggleEnabled = false, showKavitaSettings = true)) },
            isSelected = currentScreen is KomfSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = stringResource(Res.string.komf_processing_komga),
            onClick = { onNavigation(KomfProcessingSettingsScreen(KOMGA)) },
            isSelected = currentScreen is KomfProcessingSettingsScreen && currentScreen.serverType == KOMGA,
            color = contentColor,
        )
        NavigationButton(
            label = stringResource(Res.string.komf_processing_kavita),
            onClick = { onNavigation(KomfProcessingSettingsScreen(KAVITA)) },
            isSelected = currentScreen is KomfProcessingSettingsScreen && currentScreen.serverType == KAVITA,
            color = contentColor,
        )
        NavigationButton(
            label = stringResource(Res.string.komf_providers),
            onClick = { onNavigation(KomfProvidersSettingsScreen()) },
            isSelected = currentScreen is KomfProvidersSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = stringResource(Res.string.komf_notifications),
            onClick = { onNavigation(KomfNotificationSettingsScreen()) },
            isSelected = currentScreen is KomfNotificationSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = stringResource(Res.string.komf_job_history),
            onClick = { onNavigation(KomfJobsScreen(false)) },
            isSelected = currentScreen is KomfJobsScreen,
            color = contentColor,
        )
    }
}
