package snd.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
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
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.collectionadd.AddToCollectionDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.dialogs.series.edit.SeriesEditDialog
import snd.komelia.ui.dialogs.series.editbulk.SeriesBulkEditDialog
import snd.komga.client.series.KomgaSeries


@Composable
fun SeriesBulkActionsContent(
    series: List<KomgaSeries>,
    compact: Boolean
) {
    val state = rememberSeriesBulkActionsState(series)
    BulkActionsButtonsLayout(state.buttons, compact)
    SeriesBulkActionDialogs(state = state)
}

@Composable
fun SeriesBulkActionDialogs(
    state: SeriesBulkActionsState,
) {
    val coroutineScope = rememberCoroutineScope()

    if (state.showAddToCollectionDialog) {
        AddToCollectionDialog(
            series = state.series,
            onDismissRequest = { state.showAddToCollectionDialog = false })
    }
    if (state.showEditDialog) {
        if (state.series.size == 1)
            SeriesEditDialog(series = state.series.first(), onDismissRequest = { state.showEditDialog = false })
        else
            SeriesBulkEditDialog(series = state.series, onDismissRequest = { state.showEditDialog = false })
    }

    if (state.showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Series",
            body = "${state.series.size} series will be removed from this server alongside with stored media files. This cannot be undone. Continue?",
            confirmText = "Yes, delete ${state.series.size} series and their files",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.delete(state.series) }
                state.showDeleteDialog = false
            },
            onDialogDismiss = { state.showDeleteDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    if (state.showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = "Delete downloaded Series",
            body = "${state.series.size} series will be removed from this device",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.deleteDownloaded(state.series) }
                state.showDeleteDownloadedDialog = false
            },
            onDialogDismiss = { state.showDeleteDownloadedDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    if (state.showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        val bodyText = remember(state.series) {
            buildString {
                append("Download ")
                if (state.series.size == 1) append("${state.series.first().metadata.title}?")
                else append("${state.series.size} series?")
            }
        }
        if (permissionRequested) {
            ConfirmationDialog(
                body = bodyText,
                onDialogConfirm = {
                    coroutineScope.launch { state.actions.download(state.series) }
                },
                onDialogDismiss = { state.showDownloadDialog = false }
            )
        }
    }

}

@Composable
fun rememberSeriesBulkActionsState(
    series: List<KomgaSeries>,
): SeriesBulkActionsState {
    val coroutineScope = rememberCoroutineScope()
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true

    return remember(series, coroutineScope, isOffline, isAdmin) {
        SeriesBulkActionsState(
            series = series,
            actions = factory.getSeriesBulkActions(),
            coroutineScope = coroutineScope,
            isOffline = isOffline,
            isAdmin = isAdmin
        )
    }
}

data class SeriesBulkActionsState(
    val series: List<KomgaSeries>,
    val actions: SeriesBulkActions,
    private val coroutineScope: CoroutineScope,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
) {
    var showAddToCollectionDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    var showDeleteDownloadedDialog by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)

    val buttons = buildList {
        add(
            BulkActionButtonData(
                description = "Mark read",
                icon = Icons.Default.BookmarkAdd,
                onClick = { coroutineScope.launch { actions.markAsRead(series) } }
            )
        )
        add(
            BulkActionButtonData(
                description = "Mark unread",
                icon = Icons.Default.BookmarkRemove,
                onClick = { coroutineScope.launch { actions.markAsUnread(series) } }
            )
        )
        if (!isOffline && isAdmin) {
            add(
                BulkActionButtonData(
                    description = "Edit",
                    icon = Icons.Default.Edit,
                    onClick = { showEditDialog = true }
                )
            )
            add(
                BulkActionButtonData(
                    description = "Add to collection",
                    icon = Icons.AutoMirrored.Default.PlaylistAdd,
                    onClick = { showAddToCollectionDialog = true }
                )
            )
        }

        if (!isOffline) {
            add(
                BulkActionButtonData(
                    description = "Download",
                    icon = Icons.Default.Download,
                    onClick = { showDownloadDialog = true }
                )
            )
        }

        if (isOffline) {
            add(
                BulkActionButtonData(
                    description = "Delete downloaded",
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteDownloadedDialog = true }
                )
            )
        }

//        if (!isOffline && isAdmin) {
//            add(
//                BulkActionButtonData(
//                    description = "Delete from server",
//                    icon = Icons.Default.Delete,
//                    onClick = { showDeleteDialog = true }
//                )
//            )
//        }
    }
}

data class SeriesBulkActions(
    val markAsRead: suspend (List<KomgaSeries>) -> Unit,
    val markAsUnread: suspend (List<KomgaSeries>) -> Unit,
    val delete: suspend (List<KomgaSeries>) -> Unit,
    val download: suspend (List<KomgaSeries>) -> Unit,
    val deleteDownloaded: suspend (List<KomgaSeries>) -> Unit,
) {

    constructor(
        seriesApi: KomgaSeriesApi,
        taskEmitter: OfflineTaskEmitter,
        notifications: AppNotifications,
    ) : this(
        markAsRead = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesApi.markAsRead(it.id) }
            }

        },
        markAsUnread = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesApi.markAsUnread(it.id) }
            }
        },
        delete = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesApi.delete(it.id) }
            }
        },
        download = { series ->
            series.forEach { taskEmitter.downloadSeries(it.id) }
        },
        deleteDownloaded = { series ->
            series.forEach { taskEmitter.deleteSeries(it.id) }
        }
    )
}
