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
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditDialog
import io.github.snd_r.komelia.ui.dialogs.readlistadd.AddToReadListDialog
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookReadProgressUpdateRequest
import kotlinx.coroutines.CoroutineScope

@Composable
fun BookActionsMenu(
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

    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        BookEditDialog(book, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
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

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog && !showEditDialog }
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

        if (actions.editDialog)
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { showEditDialog = true },
            )

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

data class BookMenuActions(
    val analyze: (KomgaBook) -> Unit,
    val refreshMetadata: (KomgaBook) -> Unit,
    val addToReadList: (KomgaBook) -> Unit,
    val markAsRead: (KomgaBook) -> Unit,
    val markAsUnread: (KomgaBook) -> Unit,
    val delete: (KomgaBook) -> Unit,
    val editDialog: Boolean = true,
) {
    constructor(
        bookClient: KomgaBookClient,
        notifications: AppNotifications,
        scope: CoroutineScope,
    ) : this(
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                bookClient.analyze(it.id)
                notifications.add(AppNotification.Normal("Launched book analysis"))
            }
        },
        refreshMetadata = {
            notifications.runCatchingToNotifications(scope) {
                bookClient.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("Launched book metadata refresh"))
            }
        },
        addToReadList = { },
        markAsRead = { book ->
            notifications.runCatchingToNotifications(scope) {
                bookClient.markReadProgress(
                    book.id,
                    KomgaBookReadProgressUpdateRequest(completed = true)
                )
            }
        },
        markAsUnread = {
            notifications.runCatchingToNotifications(scope) { bookClient.deleteReadProgress(it.id) }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { bookClient.deleteBook(it.id) }
        },
    )
}
