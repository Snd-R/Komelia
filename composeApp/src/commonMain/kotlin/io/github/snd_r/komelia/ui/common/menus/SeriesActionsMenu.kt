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
import io.github.snd_r.komelia.ui.dialogs.collectionadd.AddToCollectionDialog
import io.github.snd_r.komelia.ui.dialogs.seriesedit.SeriesEditDialog
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import kotlinx.coroutines.CoroutineScope

@Composable
fun SeriesActionsMenu(
    series: KomgaSeries,
    actions: SeriesMenuActions,
    expanded: Boolean,
    showEditOption: Boolean,
    onDismissRequest: () -> Unit,
) {

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Series",
            body = "The Series ${series.metadata.title} will be removed from this server alongside with stored media files. This cannot be undone. Continue?",
            confirmText = "Yes, delete series \"${series.metadata.title}\"",
            onDialogConfirm = {
                actions.delete(series)
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
        SeriesEditDialog(series, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
    }
    var showAddToCollectionDialog by remember { mutableStateOf(false) }
    if (showAddToCollectionDialog) {
        AddToCollectionDialog(
            series = series,
            onDismissRequest = {
                showAddToCollectionDialog = false
                onDismissRequest()
            })
    }

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog && !showEditDialog && !showAddToCollectionDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Analyze") },
            onClick = {
                actions.analyze(series)
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Refresh metadata") },
            onClick = {
                actions.refreshMetadata(series)
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Add to collection") },
            onClick = { showAddToCollectionDialog = true },
        )

        val isRead = remember { series.booksReadCount == series.booksCount }
        val isUnread = remember { series.booksUnreadCount == series.booksCount }
        if (!isRead) {
            DropdownMenuItem(
                text = { Text("Mark as read") },
                onClick = {
                    actions.markAsRead(series)
                    onDismissRequest()
                },
            )
        }

        if (!isUnread) {
            DropdownMenuItem(
                text = { Text("Mark as unread") },
                onClick = {
                    actions.markAsUnread(series)
                    onDismissRequest()
                },
            )
        }

        if (showEditOption) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { showEditDialog = true },
            )
        }

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

data class SeriesMenuActions(
    val analyze: (KomgaSeries) -> Unit,
    val refreshMetadata: (KomgaSeries) -> Unit,
    val addToCollection: (KomgaSeries) -> Unit,
    val markAsRead: (KomgaSeries) -> Unit,
    val markAsUnread: (KomgaSeries) -> Unit,
    val delete: (KomgaSeries) -> Unit,
) {
    constructor(
        seriesClient: KomgaSeriesClient,
        notifications: AppNotifications,
        scope: CoroutineScope,
    ) : this(
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                seriesClient.analyze(it.id)
                notifications.add(AppNotification.Normal("Launched series analysis"))
            }
        },
        refreshMetadata = {
            notifications.runCatchingToNotifications(scope) {
                seriesClient.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("Launched series metadata refresh"))
            }
        },
        addToCollection = { },
        markAsRead = {
            notifications.runCatchingToNotifications(scope) { seriesClient.markAsRead(it.id) }
        },
        markAsUnread = {
            notifications.runCatchingToNotifications(scope) { seriesClient.markAsUnread(it.id) }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { seriesClient.deleteSeries(it.id) }
        },
    )
}
