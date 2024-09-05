package io.github.snd_r.komelia.ui.common.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.dialogs.collectionadd.AddToCollectionDialog
import io.github.snd_r.komelia.ui.dialogs.komf.identify.KomfIdentifyDialog
import io.github.snd_r.komelia.ui.dialogs.komf.reset.KomfResetMetadataDialog
import io.github.snd_r.komelia.ui.dialogs.readlistadd.AddToReadListDialog
import snd.komga.client.book.KomgaBook
import snd.komga.client.series.KomgaSeries

@Composable
fun OneshotActionsMenu(
    series: KomgaSeries,
    book: KomgaBook,
    actions: BookMenuActions,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Book",
            body = "The Book ${book.metadata.title} will be removed from this server alongside with stored media files. This cannot be undone. Continue?",
            confirmText = "Yes, delete book \"${book.metadata.title}\"",
            onDialogConfirm = {
                actions.delete(book)
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    var showAddToReadListDialog by remember { mutableStateOf(false) }
    if (showAddToReadListDialog) {
        AddToReadListDialog(
            books = listOf(book),
            onDismissRequest = {
                showAddToReadListDialog = false
                onDismissRequest()
            })
    }
    var showAddToCollectionDialog by remember { mutableStateOf(false) }
    if (showAddToCollectionDialog) {
        AddToCollectionDialog(
            series = listOf(series),
            onDismissRequest = {
                showAddToCollectionDialog = false
                onDismissRequest()
            })
    }
    var showKomfDialog by remember { mutableStateOf(false) }
    if (showKomfDialog) {
        KomfIdentifyDialog(
            series = series,
            onDismissRequest = {
                showKomfDialog = false
                onDismissRequest()
            }
        )
    }
    var showKomfResetDialog by remember { mutableStateOf(false) }
    if (showKomfResetDialog) {
        KomfResetMetadataDialog(
            series = series,
            onDismissRequest = {
                showKomfResetDialog = false
                onDismissRequest()
            }
        )
    }

    val showDropdown = derivedStateOf {
        expanded &&
                !showDeleteDialog &&
                !showKomfDialog &&
                !showKomfResetDialog &&
                !showAddToCollectionDialog &&
                !showAddToReadListDialog
    }

    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Analyze") },
            onClick = {
                actions.analyze(book)
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Refresh metadata") },
            onClick = {
                actions.refreshMetadata(book)
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Add to read list") },
            onClick = { showAddToReadListDialog = true },
        )
        DropdownMenuItem(
            text = { Text("Add to collection") },
            onClick = { showAddToCollectionDialog = true },
        )

        val isRead = remember { book.readProgress?.completed ?: false }
        val isUnread = remember { book.readProgress == null }

        if (!isRead) {
            DropdownMenuItem(
                text = { Text("Mark as read") },
                onClick = {
                    actions.markAsRead(book)
                    onDismissRequest()
                },
            )
        }

        if (!isUnread) {
            DropdownMenuItem(
                text = { Text("Mark as unread") },
                onClick = {
                    actions.markAsUnread(book)
                    onDismissRequest()
                },
            )
        }

        val deleteInteractionSource = remember { MutableInteractionSource() }
        val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
        val deleteColor =
            if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
            else Modifier
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                showDeleteDialog = true
            },
            modifier = Modifier
                .hoverable(deleteInteractionSource)
                .then(deleteColor)
        )
    }
}
