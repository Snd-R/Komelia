package snd.komelia.ui.settings.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import snd.komelia.ui.dialogs.ConfirmationDialog

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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Server Management", style = MaterialTheme.typography.titleLarge)
        HorizontalDivider()
        Button(
            title = "Scan all libraries",
            description = "Check folders for new or removed books.\nUses the last modified time of parent folders",
            buttonText = "Scan",
            level = WarningLevel.NORMAL,
            onClick = { onScanAllLibraries(false) }
        )
        HorizontalDivider()
        Button(
            title = "Deep scan all libraries",
            description = "Force the scanner to compare all scanned books with the ones stored in the database",
            buttonText = "Deep Scan",
            level = WarningLevel.NORMAL,
            onClick = { onScanAllLibraries(true) }
        )
        HorizontalDivider()
        Button(
            title = "Empty trash for all libraries",
            description = "Delete items marked as unavailable",
            buttonText = "Empty",
            level = WarningLevel.NORMAL,
            onClick = { showEmptyTrashDialog = true }
        )
        HorizontalDivider()
        Button(
            title = "Cancel all tasks",
            description = "Cancel all currently running tasks",
            buttonText = "Cancel",
            level = WarningLevel.WARNING,
            onClick = { onCancelAllTasks() }
        )
        HorizontalDivider()
        Button(
            title = "Shutdown",
            description = "Stop Komga application process",
            buttonText = "Shutdown",
            level = WarningLevel.DANGER,
            onClick = { showShutdownDialog = true }
        )
        HorizontalDivider()

        if (showEmptyTrashDialog) {
            ConfirmationDialog(
                title = "Empty trash for library",
                body = "By default the media server doesn't remove information for media right away. This helps if a drive is temporarily disconnected. When you empty the trash for a library, all information about missing media is deleted.",
                buttonConfirm = "Empty",
                buttonCancel = "Cancel",
                onDialogConfirm = onEmptyTrash,
                onDialogDismiss = { showEmptyTrashDialog = false }
            )
        }

        if (showShutdownDialog) {
            ConfirmationDialog(
                title = "Shut down server",
                body = "Are you sure you want to stop Komga?",
                buttonConfirm = "Stop",
                buttonCancel = "Cancel",
                buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,
                onDialogConfirm = onShutdown,
                onDialogDismiss = { showShutdownDialog = false }
            )
        }

    }
}

@Composable
private fun Button(
    title: String,
    description: String,
    buttonText: String,
    level: WarningLevel,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelLarge)
        }

        val colors = when (level) {
            WarningLevel.NORMAL -> ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )

            WarningLevel.WARNING -> ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )

            WarningLevel.DANGER -> ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(5.dp),
            colors = colors,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(buttonText)
        }

    }
}


private enum class WarningLevel {
    NORMAL,
    WARNING,
    DANGER
}