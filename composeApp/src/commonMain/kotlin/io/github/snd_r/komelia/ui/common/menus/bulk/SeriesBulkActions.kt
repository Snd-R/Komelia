package io.github.snd_r.komelia.ui.common.menus.bulk

import androidx.compose.foundation.layout.RowScope
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
import io.github.snd_r.komelia.ui.dialogs.collectionadd.AddToCollectionDialog
import io.github.snd_r.komelia.ui.dialogs.series.edit.SeriesEditDialog
import io.github.snd_r.komelia.ui.dialogs.series.editbulk.SeriesBulkEditDialog
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import kotlinx.coroutines.launch

@Composable
fun RowScope.SeriesBulkActionsContent(
    series: List<KomgaSeries>,
    iconOnly: Boolean,
) {
    val factory = LocalViewModelFactory.current
    val actions = remember { factory.getSeriesBulkActions() }
    val coroutineScope = rememberCoroutineScope()

    var showAddToCollectionDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BulkActionButton(
        description = "Mark as read",
        icon = Icons.Default.BookmarkAdd,
        iconOnly = iconOnly,
        onClick = { coroutineScope.launch { actions.markAsRead(series) } }
    )
    BulkActionButton(
        description = "Mark as unread",
        icon = Icons.Default.BookmarkRemove,
        iconOnly = iconOnly,
        onClick = { coroutineScope.launch { actions.markAsUnread(series) } }
    )
    BulkActionButton(
        description = "Add to collection",
        icon = Icons.AutoMirrored.Default.PlaylistAdd,
        iconOnly = iconOnly,
        onClick = { showAddToCollectionDialog = true }
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
    if (showAddToCollectionDialog) {
        AddToCollectionDialog(
            series = series,
            onDismissRequest = { showAddToCollectionDialog = false })
    }
    if (showEditDialog) {
        if (series.size == 1)
            SeriesEditDialog(series = series.first(), onDismissRequest = { showEditDialog = false })
        else
            SeriesBulkEditDialog(series = series, onDismissRequest = { showEditDialog = false })
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Series",
            body = "${series.size} series will be removed from this server alongside with stored media files. This cannot be undone. Continue?",
            confirmText = "Yes, delete ${series.size} series and their files",
            onDialogConfirm = {
                coroutineScope.launch { actions.delete(series) }
                showDeleteDialog = false
            },
            onDialogDismiss = { showDeleteDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }
}

data class SeriesBulkActions(
    val markAsRead: suspend (List<KomgaSeries>) -> Unit,
    val markAsUnread: suspend (List<KomgaSeries>) -> Unit,
    val delete: suspend (List<KomgaSeries>) -> Unit,
) {

    constructor(
        seriesClient: KomgaSeriesClient,
        notifications: AppNotifications,
    ) : this(
        markAsRead = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesClient.markAsRead(it.id) }
            }

        },
        markAsUnread = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesClient.markAsUnread(it.id) }
            }
        },
        delete = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesClient.deleteSeries(it.id) }
            }
        }
    )
}