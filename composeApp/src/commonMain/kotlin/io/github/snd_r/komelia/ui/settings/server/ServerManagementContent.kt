package io.github.snd_r.komelia.ui.settings.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog

@Composable
fun ServerManagementContent(
    onScanAllLibraries: (deep: Boolean) -> Unit,
    onEmptyTrash: () -> Unit,
    onCancelAllTasks: () -> Unit,
    onShutdown: () -> Unit
) {

    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showShutdownDialog by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Server Management", style = MaterialTheme.typography.titleLarge)

        FilledTonalButton(
            onClick = { onScanAllLibraries(false) },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("SCAN ALL LIBRARIES")
        }


        FilledTonalButton(
            onClick = { onScanAllLibraries(true) },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Text("SCAN ALL LIBRARIES (DEEP)")
        }

        FilledTonalButton(
            onClick = { showEmptyTrashDialog = true },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("EMPTY TRASH FOR ALL LIBRARIES")
        }

        FilledTonalButton(
            onClick = { onCancelAllTasks() },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Text("CANCEL ALL TASKS")
        }

        FilledTonalButton(
            onClick = { showShutdownDialog = true },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
        ) {
            Text("SHUTDOWN")
        }

        if (showEmptyTrashDialog) {
            ConfirmationDialog(
                title = "Empty trash for library",
                body = "By default the media server doesn't remove information for media right away. This helps if a drive is temporarily disconnected. When you empty the trash for a library, all information about missing media is deleted.",
                buttonConfirm = "EMPTY",
                buttonCancel = "CANCEL",
                onDialogConfirm = onEmptyTrash,
                onDialogDismiss = { showEmptyTrashDialog = false }
            )
        }

        if (showShutdownDialog) {
            ConfirmationDialog(
                title = "Shut down server",
                body = "Are you sure you want to stop Komga?",
                buttonConfirm = "STOP",
                buttonCancel = "CANCEL",
                buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,
                onDialogConfirm = onShutdown,
                onDialogDismiss = { showShutdownDialog = false }
            )
        }

    }
}