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
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.series.KomgaSeries

@Composable
fun CollectionBulkActionsContent(
    collection: KomgaCollection,
    series: List<KomgaSeries>,
    compact: Boolean,
) {
    val collectionsState = rememberCollectionBulkActionsState(collection, series)
    val seriesState = rememberSeriesBulkActionsState(series)
    val buttons = remember(collectionsState, seriesState) { collectionsState.buttons + seriesState.buttons }
    BulkActionsButtonsLayout(buttons, compact)
    CollectionBulkActionsDialogs(collectionsState)
    SeriesBulkActionDialogs(seriesState)
}

@Composable
fun CollectionBulkActionsDialogs(
    state: CollectionBulkActionsState
) {
    val coroutineScope = rememberCoroutineScope()

    if (state.showDeleteDialog) {
        ConfirmationDialog(
            body = "Remove selected series from this collection?",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.removeFromCollection(state.collection, state.series) }
                state.showDeleteDialog = false

            },
            onDialogDismiss = { state.showDeleteDialog = false },
        )
    }
}

@Composable
fun rememberCollectionBulkActionsState(
    collection: KomgaCollection,
    series: List<KomgaSeries>,
): CollectionBulkActionsState {
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    return remember(collection, series, isOffline, isAdmin) {
        val actions = factory.getCollectionBulkActions()
        CollectionBulkActionsState(
            collection = collection,
            series = series,
            actions = actions,
            isOffline = isOffline,
            isAdmin = isAdmin
        )
    }
}

data class CollectionBulkActions(
    val removeFromCollection: suspend (KomgaCollection, List<KomgaSeries>) -> Unit
) {
    constructor(
        collectionApi: KomgaCollectionsApi,
        notifications: AppNotifications,
    ) : this(
        removeFromCollection = { collection, series ->
            notifications.runCatchingToNotifications {

                val selectedIds = series.map { it.id }
                collectionApi.updateOne(
                    collection.id,
                    KomgaCollectionUpdateRequest(
                        seriesIds = PatchValue.Some(collection.seriesIds.filter { it !in selectedIds })
                    )
                )

            }
        },
    )

}

data class CollectionBulkActionsState(
    val collection: KomgaCollection,
    val series: List<KomgaSeries>,
    val actions: CollectionBulkActions,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
) {
    var showDeleteDialog by mutableStateOf(false)

    val buttons = buildList {
        if (!isOffline && isAdmin) {
            add(
                BulkActionButtonData(
                    description = "Remove from collection",
                    icon = Icons.Default.LayersClear,
                    onClick = { showDeleteDialog = true }
                )
            )
        }
    }
}
