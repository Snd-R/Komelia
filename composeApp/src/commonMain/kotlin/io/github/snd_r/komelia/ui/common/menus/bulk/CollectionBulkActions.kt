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
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.collection.KomgaCollectionUpdateRequest
import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.series.KomgaSeries
import kotlinx.coroutines.launch

@Composable
fun CollectionBulkActionsContent(
    collection: KomgaCollection,
    series: List<KomgaSeries>,
    iconOnly: Boolean,
) {
    val factory = LocalViewModelFactory.current
    val actions = remember { factory.getCollectionBulkActions() }
    val coroutineScope = rememberCoroutineScope()

    var showRemovalConfirmationDialog by remember { mutableStateOf(false) }
    BulkActionButton(
        description = "Remove from this collection",
        icon = Icons.Default.LayersClear,
        iconOnly = iconOnly,
        onClick = { showRemovalConfirmationDialog = true })

    if (showRemovalConfirmationDialog) {
        ConfirmationDialog(
            body = "Remove selected series from this collection?",
            onDialogConfirm = {
                coroutineScope.launch { actions.removeFromCollection(collection, series) }
                showRemovalConfirmationDialog = false

            },
            onDialogDismiss = { showRemovalConfirmationDialog = false },
        )
    }
}

data class CollectionBulkActions(
    val removeFromCollection: suspend (KomgaCollection, List<KomgaSeries>) -> Unit
) {
    constructor(
        collectionClient: KomgaCollectionClient,
        notifications: AppNotifications,
    ) : this(
        removeFromCollection = { collection, series ->
            notifications.runCatchingToNotifications {

                val selectedIds = series.map { it.id }
                collectionClient.updateOne(
                    collection.id,
                    KomgaCollectionUpdateRequest(
                        seriesIds = PatchValue.Some(collection.seriesIds.filter { it !in selectedIds }.toSet())
                    )
                )

            }
        },
    )

}
