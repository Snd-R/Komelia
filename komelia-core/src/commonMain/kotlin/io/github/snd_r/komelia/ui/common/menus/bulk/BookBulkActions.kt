package io.github.snd_r.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditDialog
import io.github.snd_r.komelia.ui.dialogs.book.editbulk.BookBulkEditDialog
import io.github.snd_r.komelia.ui.dialogs.readlistadd.AddToReadListDialog
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest

@Composable
fun BooksBulkActionsContent(
    books: List<KomgaBook>,
    iconOnly: Boolean,
) {
    val factory = LocalViewModelFactory.current
    val actions = remember { factory.getBookBulkActions() }
    BooksBulkActionsContent(books, actions, iconOnly)
}

@Composable
fun BooksBulkActionsContent(
    books: List<KomgaBook>,
    actions: BookBulkActions,
    iconOnly: Boolean,
) {
    val coroutineScope = rememberCoroutineScope()

    var showAddToReadListDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BulkActionButton(
        description = "Mark as read",
        icon = Icons.Default.BookmarkAdd,
        iconOnly = iconOnly,
        onClick = { coroutineScope.launch { actions.markAsRead(books) } }
    )
    BulkActionButton(
        description = "Mark as unread",
        icon = Icons.Default.BookmarkRemove,
        iconOnly = iconOnly,
        onClick = { coroutineScope.launch { actions.markAsUnread(books) } }
    )
    BulkActionButton(
        description = "Add to read list",
        icon = Icons.AutoMirrored.Default.PlaylistAdd,
        iconOnly = iconOnly,
        onClick = { showAddToReadListDialog = true }
    )
    BulkActionButton(
        description = "Edit",
        icon = Icons.Default.Edit,
        iconOnly = iconOnly,
        onClick = { showEditDialog = true }
    )
    BulkActionButton(
        description = "Delete",
        icon = Icons.Default.Delete,
        iconOnly = iconOnly,
        onClick = { showDeleteDialog = true }
    )
    if (showAddToReadListDialog) {
        AddToReadListDialog(
            books = books,
            onDismissRequest = { showAddToReadListDialog = false })
    }
    if (showEditDialog) {
        if (books.size == 1)
            BookEditDialog(book = books.first(), onDismissRequest = { showEditDialog = false })
        else
            BookBulkEditDialog(books = books, onDismissRequest = { showEditDialog = false })
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Books",
            body = "${books.size} books will be removed from this server alongside with stored media files. This cannot be undone. Continue?",
            confirmText = "Yes, delete ${books.size} books and their files",
            onDialogConfirm = {
                coroutineScope.launch { actions.delete(books) }
                showDeleteDialog = false
            },
            onDialogDismiss = { showDeleteDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

}

data class BookBulkActions(
    val markAsRead: suspend (List<KomgaBook>) -> Unit,
    val markAsUnread: suspend (List<KomgaBook>) -> Unit,
    val delete: suspend (List<KomgaBook>) -> Unit,
) {

    constructor(
        bookClient: KomgaBookClient,
        notifications: AppNotifications,
    ) : this(
        markAsRead = { books ->
            notifications.runCatchingToNotifications {
                books.forEach {
                    bookClient.markReadProgress(it.id, KomgaBookReadProgressUpdateRequest(completed = true))
                }
            }
        },
        markAsUnread = { books ->
            notifications.runCatchingToNotifications {
                books.forEach { bookClient.deleteReadProgress(it.id) }
            }
        },
        delete = { books ->
            notifications.runCatchingToNotifications {
                books.forEach { bookClient.deleteBook(it.id) }
            }
        }
    )
}
