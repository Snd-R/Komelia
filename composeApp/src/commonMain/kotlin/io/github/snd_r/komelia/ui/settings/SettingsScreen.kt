package io.github.snd_r.komelia.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.verticalScrollWithScrollbar
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsTab
import io.github.snd_r.komelia.ui.settings.announcements.AnnouncementsScreen
import io.github.snd_r.komelia.ui.settings.app.AppSettingsScreen
import io.github.snd_r.komelia.ui.settings.authactivity.AuthenticationActivityScreen
import io.github.snd_r.komelia.ui.settings.server.ServerSettingsScreen
import io.github.snd_r.komelia.ui.settings.users.UsersScreen
import kotlinx.coroutines.flow.SharedFlow

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val parentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val navigationViewModel = rememberScreenModel {
            viewModelFactory.getSettingsNavigationViewModel(parentNavigator)
        }

        Navigator(AccountSettingsTab()) { navigator ->
            val enableScroll = navigator.lastItem !is AuthenticationActivityScreen
            SettingsContent(
                navMenuContent = { SettingsNavigation(navigator, onLogout = navigationViewModel::logout) },
                screenContent = { CurrentScreen() },
                enableScroll = enableScroll,
                onDismiss = { parentNavigator.pop() }
            )
        }
    }
}

@Composable
fun SettingsContent(
    navMenuContent: @Composable () -> Unit,
    screenContent: @Composable () -> Unit,
    enableScroll: Boolean,
    onDismiss: () -> Unit,
) {
    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(Unit) { keyEvents.collect { if (it.key == Key.Escape) onDismiss() } }

    val leftColor = MaterialTheme.colorScheme.surfaceVariant
    val rightColor = MaterialTheme.colorScheme.surface
    Row {
        Spacer(Modifier.weight(1f).background(leftColor).fillMaxSize())
        Surface(color = leftColor) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 50.dp)
                    .width(250.dp)
                    .fillMaxHeight()
                    .verticalScrollWithScrollbar(rememberScrollState()),
                contentAlignment = Alignment.TopEnd,
            ) {
                navMenuContent()
            }
        }

        val scrollState = rememberScrollState()
        val scrollModifier = if (enableScroll) Modifier.verticalScroll(scrollState) else Modifier
        Surface(
            color = rightColor,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = scrollModifier
                        .padding(start = 20.dp, end = 20.dp, top = 50.dp)
                        .width(650.dp),
//                        .then(scrollModifier),
                    contentAlignment = Alignment.TopStart,
                ) {
                    screenContent()
                }

                OutlinedIconButton(onClick = onDismiss, modifier = Modifier.padding(vertical = 50.dp)) {
                    Column {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }

        }
        Spacer(Modifier.weight(1f).background(rightColor).fillMaxSize())
        VerticalScrollbar(scrollState, Modifier.align(Alignment.Top))
    }
}

@Composable
fun SettingsNavigation(navigator: Navigator, onLogout: () -> Unit) {
    Column {
        Text("User Settings")
        val currentTab = navigator.lastItem
        NavigationButton(
            label = "My Account",
            onClick = { navigator.replaceAll(AccountSettingsTab()) },
            isSelected = currentTab is AccountSettingsTab
        )

        NavigationButton(
            label = "My Authentication Activity",
            onClick = { navigator.replaceAll(AuthenticationActivityScreen(true)) },
            isSelected = currentTab is AuthenticationActivityScreen && currentTab.forMe
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        Text("Server Settings")

        NavigationButton(
            label = "General",
            onClick = { navigator.replaceAll(ServerSettingsScreen()) },
            isSelected = currentTab is ServerSettingsScreen
        )

        NavigationButton(
            label = "Users",
            onClick = { navigator.replaceAll(UsersScreen()) },
            isSelected = currentTab is UsersScreen
        )
        NavigationButton(
            label = "Authentication Activity",
            onClick = { navigator.replaceAll(AuthenticationActivityScreen(false)) },
            isSelected = currentTab is AuthenticationActivityScreen && !currentTab.forMe
        )

        NavigationButton(
            label = "Announcements",
            onClick = { navigator.replaceAll(AnnouncementsScreen()) },
            isSelected = currentTab is AnnouncementsScreen
        )
        HorizontalDivider(Modifier.padding(vertical = 10.dp))

        Text("App Settings")
        NavigationButton(
            label = "General",
            onClick = { navigator.replaceAll(AppSettingsScreen()) },
            isSelected = currentTab is AppSettingsScreen
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))

        var showLogoutConfirmation by remember { mutableStateOf(false) }
        NavigationButton(
            label = "Log Out",
            onClick = { showLogoutConfirmation = true },
            isSelected = false
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
) {
    val containerColor =
        if (isSelected) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(3.dp),
        color = containerColor,
        modifier = Modifier
            .height(40.dp)
            .width(250.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }

}
