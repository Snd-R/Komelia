package snd.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.book.edit.BookEditDialog
import snd.komelia.ui.dialogs.book.editbulk.BookBulkEditDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.dialogs.readlistadd.AddToReadListDialog
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest

@Composable
fun BooksBulkActionsContent(
    books: List<KomeliaBook>,
    actions: BookBulkActions,
    compact: Boolean,
) {
    val state = rememberBookBulkActionsState(books, actions)
    BulkActionsButtonsLayout(state.buttons, compact)
    BookBulkActionDialogs(state)
}

@Composable
fun BookBulkActionDialogs(state: BookBulkActionsState) {
    val coroutineScope = rememberCoroutineScope()

    if (state.showAddToReadListDialog) {
        AddToReadListDialog(
            books = state.books,
            onDismissRequest = { state.showAddToReadListDialog = false })
    }
    if (state.showEditDialog) {
        if (state.books.size == 1)
            BookEditDialog(book = state.books.first(), onDismissRequest = { state.showEditDialog = false })
        else
            BookBulkEditDialog(books = state.books, onDismissRequest = { state.showEditDialog = false })
    }

    if (state.showDeleteDownloadedDialog) {
        val booksToDelete = remember(state.books) { state.books.filter { it.downloaded } }
        val textBody = remember(booksToDelete.size) {
            buildString {
                if (booksToDelete.size == 1) {
                    append("Book ${booksToDelete.first().metadata.title} will be removed from this device")
                } else {
                    append("${booksToDelete.size} books and their files will be removed from this device")
                }
            }
        }

        ConfirmationDialog(
            title = "Delete downloaded books",
            body = textBody,
            onDialogConfirm = {
                coroutineScope.launch {
                    state.actions.deleteDownloaded(booksToDelete)
                    state.showDeleteDownloadedDialog = false
                }
            },
            onDialogDismiss = {
                state.showDeleteDownloadedDialog = false
            }
        )
    }
    if (state.showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        val bodyText = remember(state.books) {
            buildString {
                append("Download ")
                if (state.books.size == 1) append("${state.books.first().metadata.title}?")
                else append("${state.books.size} books?")
            }
        }
        if (permissionRequested) {
            ConfirmationDialog(
                body = bodyText,
                onDialogConfirm = {
                    coroutineScope.launch {
                        state.actions.download(state.books)
                    }
                },
                onDialogDismiss = { state.showDownloadDialog = false }
            )
        }
    }

    if (state.showDeleteDialog) {
        val textBody = remember(state.books.size) {
            buildString {
                if (state.books.size == 1) {
                    append("Book ")
                } else {
                    append("${state.books.size} books ")
                }
                append("will be removed from this server alongside with stored media files. This cannot be undone. Continue?")
            }
        }

        val confirmationText = remember(state.books.size) {
            buildString {
                append("Yes, delete ")
                if (state.books.size == 1) {
                    append("book and its files")
                } else {
                    append("${state.books.size} books and their files")
                }
            }
        }
        ConfirmationDialog(
            title = "Delete Books",
            body = textBody,
            confirmText = confirmationText,
            onDialogConfirm = {
                coroutineScope.launch { state.actions.delete(state.books) }
                state.showDeleteDialog = false
            },
            onDialogDismiss = { state.showDeleteDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

}

@Composable
fun rememberBookBulkActionsState(
    books: List<KomeliaBook>,
    actions: BookBulkActions? = null
): BookBulkActionsState {
    val coroutineScope = rememberCoroutineScope()
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true

    return remember(books, actions, isOffline) {
        BookBulkActionsState(
            books = books,
            actions = actions ?: factory.getBookBulkActions(),
            isOffline = isOffline,
            isAdmin = isAdmin,
            coroutineScope = coroutineScope
        )
    }
}

data class BookBulkActionsState(
    val books: List<KomeliaBook>,
    val actions: BookBulkActions,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
    private val coroutineScope: CoroutineScope,
) {

    var showAddToReadListDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)
    var showDeleteDownloadedDialog by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)

    val buttons = buildList {
        add(
            BulkActionButtonData(
                description = "Mark read",
                icon = Icons.Default.BookmarkAdd,
                onClick = { coroutineScope.launch { actions.markAsRead(books) } }
            ))

        add(
            BulkActionButtonData(
                description = "Mark unread",
                icon = Icons.Default.BookmarkRemove,
                onClick = { coroutineScope.launch { actions.markAsUnread(books) } }
            ))
        if (!isOffline && isAdmin) add(
            BulkActionButtonData(
                description = "Edit",
                icon = Icons.Default.Edit,
                onClick = { showEditDialog = true }
            ))
        if (!isOffline && isAdmin)
            add(
                BulkActionButtonData(
                    description = "Add to read list",
                    icon = Icons.AutoMirrored.Default.PlaylistAdd,
                    onClick = { showAddToReadListDialog = true }
                ))
        if (books.any { it.downloaded })
            add(
                BulkActionButtonData(
                    description = "Deleted downloaded",
                    icon = Icons.Default.AutoDelete,
                    onClick = { showDeleteDownloadedDialog = true }
                ))
        if (!isOffline && books.any { !it.downloaded })
            add(
                BulkActionButtonData(
                    description = "Download",
                    icon = Icons.Default.Download,
                    onClick = { showDownloadDialog = true }
                ))

//        if (!isOffline && isAdmin) {
//            add(
//                BulkActionButtonData(
//                    description = "Delete from server",
//                    icon = Icons.Default.Delete,
//                    onClick =
//                        { showDeleteDialog = true }
//                ))
//        }
    }
}

data class BookBulkActions(
    val markAsRead: suspend (List<KomeliaBook>) -> Unit,
    val markAsUnread: suspend (List<KomeliaBook>) -> Unit,
    val delete: suspend (List<KomeliaBook>) -> Unit,
    val download: suspend (List<KomeliaBook>) -> Unit,
    val deleteDownloaded: suspend (List<KomeliaBook>) -> Unit,
) {

    constructor(
        bookApi: KomgaBookApi,
        taskEmitter: OfflineTaskEmitter,
        notifications: AppNotifications,
    ) : this(
        markAsRead = { books ->
            notifications.runCatchingToNotifications {
                books.forEach {
                    bookApi.markReadProgress(it.id, KomgaBookReadProgressUpdateRequest(completed = true))
                }
            }
        },
        markAsUnread = { books ->
            notifications.runCatchingToNotifications {
                books.forEach { bookApi.deleteReadProgress(it.id) }
            }
        },
        delete = { books ->
            notifications.runCatchingToNotifications {
                books.forEach { bookApi.deleteBook(it.id) }
            }
        },
        download = { books ->
            books.forEach { book ->
                taskEmitter.downloadBook(book.id)
            }
        },
        deleteDownloaded = { books ->
            books.forEach { book ->
                taskEmitter.deleteBook(book.id)
            }
        }
    )
}
