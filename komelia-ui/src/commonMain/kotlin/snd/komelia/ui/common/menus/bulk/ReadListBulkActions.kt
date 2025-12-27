package snd.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListUpdateRequest

@Composable
fun ReadListBulkActionsContent(
    readList: KomgaReadList,
    books: List<KomeliaBook>,
    compact: Boolean,
) {
    val readListState = rememberReadListBulkActionsState(readList, books)
    val bookState = rememberBookBulkActionsState(books)
    val buttons = remember(readListState, bookState) { readListState.buttons + bookState.buttons }
    BulkActionsButtonsLayout(buttons, compact)
    ReadListBulkActionsDialogs(readListState)
    BookBulkActionDialogs(bookState)
}

@Composable
fun ReadListBulkActionsDialogs(state: ReadListBulkActinsState) {
    val coroutineScope = rememberCoroutineScope()
    if (state.showDeleteDialog) {
        ConfirmationDialog(
            body = "Remove selected books from this read list?",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.removeFromReadList(state.readList, state.books) }
                state.showDeleteDialog = false
            },
            onDialogDismiss = { state.showDeleteDialog = false },
        )
    }
}

@Composable
fun rememberReadListBulkActionsState(
    readList: KomgaReadList,
    books: List<KomeliaBook>,
): ReadListBulkActinsState {
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    return remember(readList, books) {
        val actions = factory.getReadListBulkActions()
        ReadListBulkActinsState(
            readList = readList,
            books = books,
            actions = actions,
            isOffline = isOffline,
            isAdmin = isAdmin
        )
    }
}

data class ReadListBulkActions(
    val removeFromReadList: suspend (KomgaReadList, List<KomeliaBook>) -> Unit
) {
    constructor(
        readListApi: KomgaReadListApi,
        notifications: AppNotifications,
    ) : this(
        removeFromReadList = { readList, books ->
            notifications.runCatchingToNotifications {

                val selectedIds = books.map { it.id }
                readListApi.updateOne(
                    readList.id,
                    KomgaReadListUpdateRequest(
                        bookIds = PatchValue.Some(readList.bookIds.filter { it !in selectedIds })
                    )
                )

            }
        },
    )

}


data class ReadListBulkActinsState(
    val readList: KomgaReadList,
    val books: List<KomeliaBook>,
    val actions: ReadListBulkActions,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
) {
    var showDeleteDialog by mutableStateOf(false)

    val buttons = buildList {
        if (!isOffline && isAdmin) {
            add(
                BulkActionButtonData(
                    description = "Remove from read list",
                    icon = Icons.Default.LayersClear,
                    onClick = { showDeleteDialog = true }
                )
            )
        }
    }
}