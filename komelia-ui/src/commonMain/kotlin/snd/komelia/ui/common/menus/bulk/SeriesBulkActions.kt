package snd.komelia.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_add_to_collection
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_auto_identify
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_bulk_delete_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_bulk_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_delete_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_delete_downloaded
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_komf_auto_identify_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_komf_auto_identify_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_mark_read
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_mark_unread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomfIntegration
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.collectionadd.AddToCollectionDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.dialogs.series.edit.SeriesEditDialog
import snd.komelia.ui.dialogs.series.editbulk.SeriesBulkEditDialog
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.client.KomfMetadataClient
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

    if (state.showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.series_delete_confirm_title),
            body = pluralStringResource(Res.plurals.series_bulk_delete_confirm_body, state.series.size, state.series.size),
            onDialogConfirm = {
                coroutineScope.launch { state.actions.deleteDownloaded(state.series) }
                state.showDeleteDownloadedDialog = false
            },
            onDialogDismiss = { state.showDeleteDownloadedDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    if (state.showKomfIdentifyDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.series_komf_auto_identify_title),
            body = pluralStringResource(
                Res.plurals.series_komf_auto_identify_body,
                state.series.size,
                state.series.size
            ),
            onDialogConfirm = {
                coroutineScope.launch { state.actions.komfIdentify(state.series) }
                state.showKomfIdentifyDialog = false
            },
            onDialogDismiss = { state.showKomfIdentifyDialog = false },
        )
    }

    if (state.showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        val bodyText = pluralStringResource(
            Res.plurals.series_bulk_download,
            state.series.size,
            if (state.series.size == 1) state.series.first().metadata.title
            else state.series.size
        )
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
    val isKomfEnabled = LocalKomfIntegration.current.collectAsState(false).value

    return remember(series, coroutineScope, isOffline, isAdmin, isKomfEnabled) {
        SeriesBulkActionsState(
            series = series,
            actions = factory.getSeriesBulkActions(),
            coroutineScope = coroutineScope,
            isOffline = isOffline,
            isAdmin = isAdmin,
            isKomfEnabled = isKomfEnabled
        )
    }
}

data class SeriesBulkActionsState(
    val series: List<KomgaSeries>,
    val actions: SeriesBulkActions,
    private val coroutineScope: CoroutineScope,
    private val isOffline: Boolean,
    private val isKomfEnabled: Boolean,
    private val isAdmin: Boolean,
) {
    var showAddToCollectionDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)

    //    var showDeleteDialog by mutableStateOf(false)
    var showDeleteDownloadedDialog by mutableStateOf(false)
    var showKomfIdentifyDialog by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)

    val buttons = buildList {
        add(
            BulkActionButtonData(
                description = Res.string.series_mark_read,
                icon = Icons.Default.BookmarkAdd,
                onClick = { coroutineScope.launch { actions.markAsRead(series) } }
            )
        )
        add(
            BulkActionButtonData(
                description = Res.string.series_mark_unread,
                icon = Icons.Default.BookmarkRemove,
                onClick = { coroutineScope.launch { actions.markAsUnread(series) } }
            )
        )
        if (!isOffline && isAdmin) {
            add(
                BulkActionButtonData(
                    description = Res.string.series_edit,
                    icon = Icons.Default.Edit,
                    onClick = { showEditDialog = true }
                )
            )
            add(
                BulkActionButtonData(
                    description = Res.string.series_add_to_collection,
                    icon = Icons.AutoMirrored.Default.PlaylistAdd,
                    onClick = { showAddToCollectionDialog = true }
                )
            )
        }

        if (!isOffline) {
            add(
                BulkActionButtonData(
                    description = Res.string.series_download,
                    icon = Icons.Default.Download,
                    onClick = { showDownloadDialog = true }
                )
            )
        }

        if (isOffline) {
            add(
                BulkActionButtonData(
                    description = Res.string.series_delete_downloaded,
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteDownloadedDialog = true }
                )
            )
        }
        if (isKomfEnabled) {
            add(
                BulkActionButtonData(
                    description = Res.string.series_auto_identify,
                    icon = Icons.Default.Extension,
                    onClick = { showKomfIdentifyDialog = true }
                )
            )
        }
    }
}

data class SeriesBulkActions(
    val markAsRead: suspend (List<KomgaSeries>) -> Unit,
    val markAsUnread: suspend (List<KomgaSeries>) -> Unit,
    val delete: suspend (List<KomgaSeries>) -> Unit,
    val download: suspend (List<KomgaSeries>) -> Unit,
    val deleteDownloaded: suspend (List<KomgaSeries>) -> Unit,
    val komfIdentify: suspend (List<KomgaSeries>) -> Unit,
) {

    constructor(
        seriesApi: KomgaSeriesApi,
        komfClient: KomfMetadataClient,
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
        },
        komfIdentify = { series ->
            series.forEach {
                komfClient.matchSeries(
                    KomfServerLibraryId(it.libraryId.value),
                    KomfServerSeriesId(it.id.value),
                )
            }
        }
    )
}
