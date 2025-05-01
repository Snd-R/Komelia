package snd.komelia.settings

import androidx.compose.runtime.*
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog

@Composable
fun KomfSettingsDialog(onDismiss: () -> Unit) {
    val tabs = remember { listOf(ConnectionTab(), ProcessingTab(), ProvidersTab(), NotificationsTab(), JobsTab()) }
    var currentTab by remember { mutableStateOf(tabs.first()) }
    TabDialog(
        title = "Komf Settings",
        currentTab = currentTab,
        tabs = tabs,
        onTabChange = { currentTab = it },
        onDismissRequest = onDismiss,
        onConfirm = onDismiss,
        confirmationText = "Close",
        showCancelButton = false
    )
}

