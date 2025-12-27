package snd.komelia.ui.common.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.book.edit.BookEditDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.dialogs.readlistadd.AddToReadListDialog
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest

@Composable
fun BookActionsMenu(
    book: KomeliaBook,
    actions: BookMenuActions,
    expanded: Boolean,
    showEditOption: Boolean,
    showDownloadOption: Boolean,
    onDismissRequest: () -> Unit,
) {
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isOffline = LocalOfflineMode.current.collectAsState().value
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteDownloadedDialog by remember { mutableStateOf(false) }
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

    if (showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = "Delete downloaded Book",
            body = "Book ${book.metadata.title} will be removed from this device",
            onDialogConfirm = {
                actions.deleteDownloaded(book)
                onDismissRequest()
            },
            onDialogDismiss = {
                showDeleteDownloadedDialog = false
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
    var showDownloadDialog by remember { mutableStateOf(false) }
    if (showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        if (permissionRequested) {
            ConfirmationDialog(
                "Download book \"${book.metadata.title}\"?",
                onDialogConfirm = { actions.download(book) },
                onDialogDismiss = { showDownloadDialog = false }
            )
        }
    }

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog && !showEditDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        if (isAdmin && !isOffline) {
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
        }

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

        if (isAdmin && !isOffline && showEditOption) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { showEditDialog = true },
            )
        }
        if (!isOffline && showDownloadOption) {
            DropdownMenuItem(
                text = { Text("Download") },
                onClick = { showDownloadDialog = true },
            )
        }

        if (book.downloaded) {
            val deleteInteractionSource = remember { MutableInteractionSource() }
            val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
            val deleteColor =
                if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
                else Modifier
            DropdownMenuItem(
                text = { Text("Delete downloaded") },
                onClick = { showDeleteDownloadedDialog = true },
                modifier = Modifier
                    .hoverable(deleteInteractionSource)
                    .then(deleteColor)
            )

        }

//        if (isAdmin && !isOffline) {
//            val deleteInteractionSource = remember { MutableInteractionSource() }
//            val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
//            val deleteColor =
//                if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
//                else Modifier
//            DropdownMenuItem(
//                text = { Text("Delete from server") },
//                onClick = { showDeleteDialog = true },
//                modifier = Modifier
//                    .hoverable(deleteInteractionSource)
//                    .then(deleteColor)
//            )
//        }
    }
}

data class BookMenuActions(
    val analyze: (KomeliaBook) -> Unit,
    val refreshMetadata: (KomeliaBook) -> Unit,
    val markAsRead: (KomeliaBook) -> Unit,
    val markAsUnread: (KomeliaBook) -> Unit,
    val delete: (KomeliaBook) -> Unit,
    val download: (KomeliaBook) -> Unit,
    val deleteDownloaded: (KomeliaBook) -> Unit,
) {
    constructor(
        bookApi: KomgaBookApi,
        notifications: AppNotifications,
        scope: CoroutineScope,
        taskEmitter: OfflineTaskEmitter
    ) : this(
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                bookApi.analyze(it.id)
                notifications.add(AppNotification.Normal("Launched book analysis"))
            }
        },
        refreshMetadata = {
            notifications.runCatchingToNotifications(scope) {
                bookApi.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("Launched book metadata refresh"))
            }
        },
        markAsRead = { book ->
            notifications.runCatchingToNotifications(scope) {
                bookApi.markReadProgress(
                    book.id,
                    KomgaBookReadProgressUpdateRequest(completed = true)
                )
            }
        },
        markAsUnread = {
            notifications.runCatchingToNotifications(scope) { bookApi.deleteReadProgress(it.id) }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { bookApi.deleteBook(it.id) }
        },
        download = { scope.launch { taskEmitter.downloadBook(it.id) } },
        deleteDownloaded = { scope.launch { taskEmitter.deleteBook(it.id) } }
    )
}
