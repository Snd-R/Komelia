package snd.komelia.ui.dialogs.collectionadd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionCreateRequest
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.series.KomgaSeries

class AddToCollectionDialogViewModel(
    private val series: List<KomgaSeries>,
    private val onDismissRequest: () -> Unit,
    private val collectionApi: KomgaCollectionsApi,
    private val appNotifications: AppNotifications,
) {

    var collections by mutableStateOf<List<KomgaCollection>>(emptyList())
        private set

    suspend fun initialize() {
        collections = collectionApi.getAll(pageRequest = KomgaPageRequest(unpaged = true))
            .content.sortedByDescending { it.lastModifiedDate }
    }

    suspend fun addTo(collection: KomgaCollection) {
        appNotifications.runCatchingToNotifications {
            collectionApi.updateOne(
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
            collectionApi.addOne(
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