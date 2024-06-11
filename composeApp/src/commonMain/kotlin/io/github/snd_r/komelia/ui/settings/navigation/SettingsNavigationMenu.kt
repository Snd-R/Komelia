package io.github.snd_r.komelia.ui.settings.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.platform.PlatformType.DESKTOP
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.platform.PlatformType.WEB
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsScreen
import io.github.snd_r.komelia.ui.settings.analysis.MediaAnalysisScreen
import io.github.snd_r.komelia.ui.settings.announcements.AnnouncementsScreen
import io.github.snd_r.komelia.ui.settings.app.AppSettingsScreen
import io.github.snd_r.komelia.ui.settings.authactivity.AuthenticationActivityScreen
import io.github.snd_r.komelia.ui.settings.decoder.DecoderSettingsScreen
import io.github.snd_r.komelia.ui.settings.server.ServerSettingsScreen
import io.github.snd_r.komelia.ui.settings.updates.AppUpdatesScreen
import io.github.snd_r.komelia.ui.settings.users.UsersScreen

@Composable
fun SettingsNavigationMenu(
    hasMediaErrors: Boolean,
    newVersionIsAvailable: Boolean,
    currentScreen: Screen,
    onNavigation: (Screen) -> Unit = {},
    onLogout: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val platform = LocalPlatform.current
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Text("User Settings", style = MaterialTheme.typography.titleSmall)
        NavigationButton(
            label = "My Account",
            onClick = { onNavigation(AccountSettingsScreen()) },
            isSelected = currentScreen is AccountSettingsScreen,
            color = contentColor,
        )

        NavigationButton(
            label = "My Authentication Activity",
            onClick = { onNavigation(AuthenticationActivityScreen(true)) },
            isSelected = currentScreen is AuthenticationActivityScreen && currentScreen.forMe,
            color = contentColor,
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        Text("Server Settings", style = MaterialTheme.typography.titleSmall)

        NavigationButton(
            label = "General",
            onClick = { onNavigation(ServerSettingsScreen()) },
            isSelected = currentScreen is ServerSettingsScreen,
            color = contentColor,
        )

        NavigationButton(
            label = "Users",
            onClick = { onNavigation(UsersScreen()) },
            isSelected = currentScreen is UsersScreen,
            color = contentColor,
        )
        NavigationButton(
            label = "Authentication Activity",
            onClick = { onNavigation(AuthenticationActivityScreen(false)) },
            isSelected = currentScreen is AuthenticationActivityScreen && !currentScreen.forMe,
            color = contentColor,
        )
        NavigationButton(
            label = "Media Analysis",
            onClick = { onNavigation(MediaAnalysisScreen()) },
            isSelected = currentScreen is MediaAnalysisScreen,
            error = hasMediaErrors,
            color = contentColor,
        )

        NavigationButton(
            label = "Announcements",
            onClick = { onNavigation(AnnouncementsScreen()) },
            isSelected = currentScreen is AnnouncementsScreen,
            color = contentColor,
        )
        HorizontalDivider(Modifier.padding(vertical = 10.dp))

        Text("App Settings", style = MaterialTheme.typography.titleSmall)
        NavigationButton(
            label = "Appearance",
            onClick = { onNavigation(AppSettingsScreen()) },
            isSelected = currentScreen is AppSettingsScreen,
            color = contentColor,
        )
        if (platform == DESKTOP)
            NavigationButton(
                label = "Decoder",
                onClick = { onNavigation(DecoderSettingsScreen()) },
                isSelected = currentScreen is DecoderSettingsScreen,
                color = contentColor,
            )
        NavigationButton(
            label = "Updates",
            onClick = { onNavigation(AppUpdatesScreen()) },
            isSelected = currentScreen is AppUpdatesScreen,
            error = newVersionIsAvailable,
            color = contentColor,
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))

        var showLogoutConfirmation by remember { mutableStateOf(false) }
        NavigationButton(
            label = "Log Out",
            onClick = { showLogoutConfirmation = true },
            isSelected = false,
            color = contentColor,
        )
        if (showLogoutConfirmation) {
            ConfirmationDialog(
                title = "Log Out",
                body = "Are you sure you want to logout?",
                buttonConfirm = "Log Out",
                buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,

                onDialogConfirm = onLogout,
                onDialogDismiss = { showLogoutConfirmation = false })
        }
    }
}


@Composable
private fun NavigationButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    warn: Boolean = false,
    error: Boolean = false,
    color: Color
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainer else color

    val height = when (LocalPlatform.current) {
        MOBILE -> 50.dp
        DESKTOP, WEB -> 40.dp
    }

    Surface(
        onClick = { if (!isSelected) onClick() },
        shape = RoundedCornerShape(3.dp),
        color = containerColor,
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .cursorForHand()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(5.dp))
            if (error) {
                val color = MaterialTheme.colorScheme.error
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = color)
                }
            } else if (warn) {
                val color = MaterialTheme.colorScheme.tertiary
                Canvas(modifier = Modifier.size(30.dp)) {
                    drawCircle(color = color)
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null)

        }
    }

}
