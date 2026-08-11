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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_add_to_readlist
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_analyze
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_delete_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_delete_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_delete_downloaded
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_download_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_mark_read
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_mark_unread
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_refresh_metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineAvailable
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
    var showDeleteDownloadedDialog by remember { mutableStateOf(false) }

    if (showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.book_delete_confirm_title),
            body = stringResource(Res.string.book_delete_confirm_body, book.metadata.title),
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
                stringResource(Res.string.book_download_confirm, book.metadata.title),
                onDialogConfirm = { actions.download(book) },
                onDialogDismiss = { showDownloadDialog = false }
            )
        }
    }

    val showDropdown = derivedStateOf { expanded && !showEditDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        if (isAdmin && !isOffline) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_analyze)) },
                onClick = {
                    actions.analyze(book)
                    onDismissRequest()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_refresh_metadata)) },
                onClick = {
                    actions.refreshMetadata(book)
                    onDismissRequest()
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_add_to_readlist)) },
                onClick = { showAddToReadListDialog = true },
            )
        }

        val isRead = remember { book.readProgress?.completed ?: false }
        val isUnread = remember { book.readProgress == null }

        if (!isRead) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_mark_read)) },
                onClick = {
                    actions.markAsRead(book)
                    onDismissRequest()
                },
            )
        }

        if (!isUnread) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_mark_unread)) },
                onClick = {
                    actions.markAsUnread(book)
                    onDismissRequest()
                },
            )
        }

        if (isAdmin && !isOffline && showEditOption) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_edit)) },
                onClick = { showEditDialog = true },
            )
        }
        val offlineAvailable = LocalOfflineAvailable.current
        if (!isOffline && showDownloadOption && offlineAvailable) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.book_download)) },
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
                text = { Text(stringResource(Res.string.book_delete_downloaded)) },
                onClick = { showDeleteDownloadedDialog = true },
                modifier = Modifier
                    .hoverable(deleteInteractionSource)
                    .then(deleteColor)
            )

        }
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
        taskEmitter: OfflineTaskEmitter?
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
        download = { scope.launch { checkNotNull(taskEmitter).downloadBook(it.id) } },
        deleteDownloaded = { scope.launch { checkNotNull(taskEmitter).deleteBook(it.id) } }
    )
}
