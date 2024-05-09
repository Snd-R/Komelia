package io.github.snd_r.komelia.ui.dialogs.collectionadd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.collection.KomgaCollectionCreateRequest
import io.github.snd_r.komga.collection.KomgaCollectionUpdateRequest
import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.series.KomgaSeries

class AddToCollectionDialogViewModel(
    private val series: List<KomgaSeries>,
    private val onDismissRequest: () -> Unit,
    private val collectionClient: KomgaCollectionClient,
    private val appNotifications: AppNotifications,
) {

    var collections by mutableStateOf<List<KomgaCollection>>(emptyList())
        private set

    suspend fun initialize() {
        collections = collectionClient.getAll().content.sortedByDescending { it.lastModifiedDate }
    }

    suspend fun addTo(collection: KomgaCollection) {
        appNotifications.runCatchingToNotifications {
            collectionClient.updateOne(
                collection.id,
                KomgaCollectionUpdateRequest(
                    seriesIds = PatchValue.Some(
                        (collection.seriesIds + series.map { it.id }).distinct()
                    )
                )
            )
            onDismissRequest()
        }
    }

    suspend fun createNew(name: String) {
        appNotifications.runCatchingToNotifications {
            collectionClient.addOne(
                KomgaCollectionCreateRequest(
                    name = name,
                    ordered = false,
                    seriesIds = series.map { it.id }
                )
            )
            onDismissRequest()
        }
    }
}