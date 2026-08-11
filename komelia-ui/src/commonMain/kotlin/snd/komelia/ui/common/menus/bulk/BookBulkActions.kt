package snd.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_add_to_readlist
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_bulk_delete_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_bulk_delete_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_bulk_download_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_delete_downloaded
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_mark_read
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_mark_unread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineAvailable
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
        val textBody = pluralStringResource(
            Res.plurals.book_bulk_delete_confirm_body, booksToDelete.size,
            if (booksToDelete.size == 1) booksToDelete.first().metadata.title
            else booksToDelete.size
        )

        ConfirmationDialog(
            title = stringResource(Res.string.book_bulk_delete_confirm_title),
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

        val bodyText = pluralStringResource(
            Res.plurals.book_bulk_download_confirm,
            state.books.size,
            if (state.books.size == 1) state.books.first().metadata.title
            else state.books.size

        )

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
    val offlineAvailable = LocalOfflineAvailable.current
    return remember(books, actions, isOffline) {
        BookBulkActionsState(
            books = books,
            actions = actions ?: factory.getBookBulkActions(),
            isOffline = isOffline,
            isAdmin = isAdmin,
            offlineAvailable = offlineAvailable,
            coroutineScope = coroutineScope,
        )
    }
}

data class BookBulkActionsState(
    val books: List<KomeliaBook>,
    val actions: BookBulkActions,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
    private val offlineAvailable: Boolean,
    private val coroutineScope: CoroutineScope,
) {

    var showAddToReadListDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)
    var showDeleteDownloadedDialog by mutableStateOf(false)

    val buttons = buildList {
        add(
            BulkActionButtonData(
                description = Res.string.book_mark_read,
                icon = Icons.Default.BookmarkAdd,
                onClick = { coroutineScope.launch { actions.markAsRead(books) } }
            ))

        add(
            BulkActionButtonData(
                description = Res.string.book_mark_unread,
                icon = Icons.Default.BookmarkRemove,
                onClick = { coroutineScope.launch { actions.markAsUnread(books) } }
            ))
        if (!isOffline && isAdmin) add(
            BulkActionButtonData(
                description = Res.string.book_edit,
                icon = Icons.Default.Edit,
                onClick = { showEditDialog = true }
            ))
        if (!isOffline && isAdmin)
            add(
                BulkActionButtonData(
                    description = Res.string.book_add_to_readlist,
                    icon = Icons.AutoMirrored.Default.PlaylistAdd,
                    onClick = { showAddToReadListDialog = true }
                ))
        if (books.any { it.downloaded })
            add(
                BulkActionButtonData(
                    description = Res.string.book_delete_downloaded,
                    icon = Icons.Default.AutoDelete,
                    onClick = { showDeleteDownloadedDialog = true }
                ))

        if (offlineAvailable && !isOffline && books.any { !it.downloaded })
            add(
                BulkActionButtonData(
                    description = Res.string.book_download,
                    icon = Icons.Default.Download,
                    onClick = { showDownloadDialog = true }
                ))
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
        taskEmitter: OfflineTaskEmitter?,
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
                checkNotNull(taskEmitter).downloadBook(book.id)
            }
        },
        deleteDownloaded = { books ->
            books.forEach { book ->
                checkNotNull(taskEmitter).deleteBook(book.id)
            }
        }
    )
}
