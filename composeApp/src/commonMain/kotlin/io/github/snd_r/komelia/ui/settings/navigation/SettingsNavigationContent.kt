package io.github.snd_r.komelia.ui.settings.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsTab
import io.github.snd_r.komelia.ui.settings.announcements.AnnouncementsScreen
import io.github.snd_r.komelia.ui.settings.app.AppSettingsScreen
import io.github.snd_r.komelia.ui.settings.authactivity.AuthenticationActivityScreen
import io.github.snd_r.komelia.ui.settings.server.ServerSettingsScreen
import io.github.snd_r.komelia.ui.settings.users.UsersScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun RegularSettingsContent(
    navigator: Navigator,
    screenContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
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
                    .padding(start = 5.dp, end = 5.dp, top = 50.dp)
                    .width(250.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopEnd,
            ) {
                SettingsNavigationContent(navigator, onLogout = onLogout)
            }
        }

        val scrollState = rememberScrollState()
        Surface(
            color = rightColor,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.padding(top = 50.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(start = 10.dp, end = 5.dp)
                        .width(700.dp)
                        .weight(1f, false),
                    contentAlignment = Alignment.TopStart,
                ) {
                    screenContent()
                }
                when (LocalWindowWidth.current) {
                    FULL, EXPANDED ->
                        OutlinedIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.cursorForHand().weight(.1f, false)
                        ) { Icon(Icons.Default.Close, null) }

                    else -> {}
                }
            }

        }
        Spacer(Modifier.weight(1f).background(rightColor).fillMaxSize())
        VerticalScrollbar(scrollState, Modifier.align(Alignment.Top))
    }
}

@Composable
fun CompactSettingsContent(
    navigator: Navigator,
    screenContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Open)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CompactNavigationContent(
                navigator = navigator,
                drawerState = drawerState,
                onDismiss = onDismiss,
                onLogout = onLogout
            )
        }
    ) {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            screenContent()
        }
    }
}

@Composable
private fun CompactNavigationContent(
    navigator: Navigator,
    drawerState: DrawerState,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .sizeIn(
                minWidth = 240.dp,
                maxWidth = DrawerDefaults.MaximumDrawerWidth
            )
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp)) {
            Row(
                modifier = Modifier
                    .clickable { onDismiss() }
                    .cursorForHand()
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.ChevronLeft, null)
                Text("Settings")
            }
            SettingsNavigationContent(
                navigator,
                onNavigation = { coroutineScope.launch { drawerState.snapTo(DrawerValue.Closed) } },
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun SettingsNavigationContent(
    navigator: Navigator,
    onNavigation: () -> Unit = {},
    onLogout: () -> Unit
) {
    Column {
        Text("User Settings")
        val currentTab = navigator.lastItem
        NavigationButton(
            label = "My Account",
            onClick = {
                onNavigation()
                navigator.replaceAll(AccountSettingsTab())
            },
            isSelected = currentTab is AccountSettingsTab
        )

        NavigationButton(
            label = "My Authentication Activity",
            onClick = {
                onNavigation()
                navigator.replaceAll(AuthenticationActivityScreen(true))
            },
            isSelected = currentTab is AuthenticationActivityScreen && currentTab.forMe
        )

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        Text("Server Settings")

        NavigationButton(
            label = "General",
            onClick = {
                onNavigation()
                navigator.replaceAll(ServerSettingsScreen())
            },
            isSelected = currentTab is ServerSettingsScreen
        )

        NavigationButton(
            label = "Users",
            onClick = {
                onNavigation()
                navigator.replaceAll(UsersScreen())
            },
            isSelected = currentTab is UsersScreen
        )
        NavigationButton(
            label = "Authentication Activity",
            onClick = {
                onNavigation()
                navigator.replaceAll(AuthenticationActivityScreen(false))
            },
            isSelected = currentTab is AuthenticationActivityScreen && !currentTab.forMe
        )

        NavigationButton(
            label = "Announcements",
            onClick = {
                onNavigation()
                navigator.replaceAll(AnnouncementsScreen())
            },
            isSelected = currentTab is AnnouncementsScreen
        )
        HorizontalDivider(Modifier.padding(vertical = 10.dp))

        Text("App Settings")
        NavigationButton(
            label = "General",
            onClick = {
                onNavigation()
                navigator.replaceAll(AppSettingsScreen())
            },
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
            .fillMaxWidth()
//            .width(225.dp)
            .cursorForHand()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null)
        }
    }

}
