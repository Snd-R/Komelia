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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_mark_read
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_mark_unread
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_refresh_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_add_to_collection
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_identify
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_reset_metadata
import org.jetbrains.compose.resources.stringResource
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalKomfIntegration
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.collectionadd.AddToCollectionDialog
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialog
import snd.komelia.ui.dialogs.komf.reset.KomfResetSeriesMetadataDialog
import snd.komelia.ui.dialogs.readlistadd.AddToReadListDialog
import snd.komga.client.series.KomgaSeries

@Composable
fun OneshotActionsMenu(
    series: KomgaSeries,
    book: KomeliaBook,
    actions: BookMenuActions,
    expanded: Boolean,
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
        KomfResetSeriesMetadataDialog(
            series = series,
            onDismissRequest = {
                showKomfResetDialog = false
                onDismissRequest()
            }
        )
    }

    val showDropdown = derivedStateOf {
        expanded &&
                !showKomfDialog &&
                !showKomfResetDialog &&
                !showAddToCollectionDialog &&
                !showAddToReadListDialog
    }

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
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.series_add_to_collection)) },
                onClick = { showAddToCollectionDialog = true },
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

        val komfIntegration = LocalKomfIntegration.current.collectAsState(false)
        if (komfIntegration.value) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.series_identify)) },
                onClick = { showKomfDialog = true },
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.series_reset_metadata)) },
                onClick = { showKomfResetDialog = true },
            )
        }

        val deleteInteractionSource = remember { MutableInteractionSource() }
        val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
        val deleteColor =
            if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
            else Modifier

        if (isOffline) {
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
