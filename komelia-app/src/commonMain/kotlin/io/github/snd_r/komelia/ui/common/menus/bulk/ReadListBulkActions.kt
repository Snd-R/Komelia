package io.github.snd_r.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListUpdateRequest

@Composable
fun ReadListBulkActionsContent(
    readList: KomgaReadList,
    books: List<KomgaBook>,
    iconOnly: Boolean,
) {
    val factory = LocalViewModelFactory.current
    val actions = remember { factory.getReadListBulkActions() }
    val coroutineScope = rememberCoroutineScope()

    var showRemovalConfirmationDialog by remember { mutableStateOf(false) }
    BulkActionButton(
        description = "Remove from this read list",
        icon = Icons.Default.LayersClear,
        iconOnly = iconOnly,
        onClick = { showRemovalConfirmationDialog = true })

    if (showRemovalConfirmationDialog) {
        ConfirmationDialog(
            body = "Remove selected books from this read list?",
            onDialogConfirm = {
                coroutineScope.launch { actions.removeFromReadList(readList, books) }
                showRemovalConfirmationDialog = false

            },
            onDialogDismiss = { showRemovalConfirmationDialog = false },
        )
    }

}

data class ReadListBulkActions(
    val removeFromReadList: suspend (KomgaReadList, List<KomgaBook>) -> Unit
) {
    constructor(
        readListClient: KomgaReadListClient,
        notifications: AppNotifications,
    ) : this(
        removeFromReadList = { readList, books ->
            notifications.runCatchingToNotifications {

                val selectedIds = books.map { it.id }
                readListClient.updateOne(
                    readList.id,
                    KomgaReadListUpdateRequest(
                        bookIds = PatchValue.Some(readList.bookIds.filter { it !in selectedIds })
                    )
                )

            }
        },
    )

}
