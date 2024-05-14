package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.CollectionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SeriesCollectionsState(
    val series: StateFlow<KomgaSeries?>,
    private val notifications: AppNotifications,
    private val seriesClient: KomgaSeriesClient,
    private val collectionClient: KomgaCollectionClient,
    private val events: SharedFlow<KomgaEvent>,
    private val screenModelScope: CoroutineScope,
    val cardWidth: StateFlow<Dp>,
) {
    private val mutableState = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val state = mutableState.asStateFlow()

    var collections by mutableStateOf<Map<KomgaCollection, List<KomgaSeries>>>(emptyMap())
        private set

    suspend fun initialize() {
        if (mutableState.value != LoadState.Uninitialized) return

        loadCollections()
        screenModelScope.launch { registerEventListener() }
    }

    private suspend fun loadCollections() {
        notifications.runCatchingToNotifications {
            mutableState.value = LoadState.Loading
            val series = series.filterNotNull().first()
            val collections = seriesClient.getAllCollectionsBySeries(series.id)

            this.collections = collections.associateWith { collection ->
                collectionClient.getSeriesForCollection(
                    id = collection.id,
                    pageRequest = KomgaPageRequest(size = 500)
                ).content
            }
            mutableState.value = LoadState.Success(Unit)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun registerEventListener() {
        events.collect { event ->
            if (event is CollectionEvent && collections.keys.any { it.id == event.collectionId }) {
                loadCollections()
            }
        }
    }
}