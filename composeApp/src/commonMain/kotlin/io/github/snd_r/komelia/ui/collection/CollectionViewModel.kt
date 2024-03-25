package io.github.snd_r.komelia.ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.CollectionChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressSeriesChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressSeriesEvent
import io.github.snd_r.komga.sse.KomgaEvent.SeriesChanged
import io.github.snd_r.komga.sse.KomgaEvent.SeriesDeleted
import io.github.snd_r.komga.sse.KomgaEvent.SeriesEvent
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val collectionId: KomgaCollectionId,
    private val collectionClient: KomgaCollectionClient,
    private val notifications: AppNotifications,
    private val seriesClient: KomgaSeriesClient,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    lateinit var collection: KomgaCollection

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)
    var series by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var totalSeriesPages by mutableStateOf(1)
        private set
    var totalSeriesCount by mutableStateOf(0)
        private set
    var currentSeriesPage by mutableStateOf(1)
        private set
    var pageLoadSize by mutableStateOf(100)
        private set

    private val reloadJobsFlow = MutableSharedFlow<Unit>(0, 1, DROP_OLDEST)
    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            loadCollection()
            loadSeriesPage(1)
        }

        screenModelScope.launch { startEventListener() }

        reloadJobsFlow.onEach {
            loadSeriesPage(currentSeriesPage)
            delay(1000)
        }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, notifications, screenModelScope)

    fun onCollectionDelete() {
        notifications.runCatchingToNotifications(screenModelScope) {
            collectionClient.deleteOne(collectionId)
        }
    }

    fun onPageSizeChange(pageSize: Int) {
        pageLoadSize = pageSize
        screenModelScope.launch { loadSeriesPage(1) }
    }

    fun onPageChange(pageNumber: Int) {
        screenModelScope.launch { loadSeriesPage(pageNumber) }
    }

    private suspend fun loadCollection() {
        notifications.runCatchingToNotifications {
            collection = collectionClient.getOne(collectionId)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun loadSeriesPage(page: Int) {
        if (state.value is Error) return

        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            val pageRequest = KomgaPageRequest(
                page = page - 1,
                size = pageLoadSize,
            )
            val collectionPage = collectionClient.getSeriesForCollection(collectionId, pageRequest = pageRequest)

            currentSeriesPage = collectionPage.number + 1
            totalSeriesPages = collectionPage.totalPages
            totalSeriesCount = collectionPage.totalElements
            series = collectionPage.content

            mutableState.value = Success(Unit)
        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }

    private suspend fun startEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is CollectionChanged -> onCollectionChanged(event)
                is SeriesChanged -> onSeriesChanged(event)
                is SeriesDeleted -> onSeriesChanged(event)
                is ReadProgressSeriesChanged -> onReadProgressChanged(event)
                is ReadProgressSeriesEvent -> onReadProgressChanged(event)
                else -> {}
            }
        }
    }

    private suspend fun onCollectionChanged(event: CollectionChanged) {
        if (event.collectionId == collectionId) loadCollection()
    }

    private fun onSeriesChanged(event: SeriesEvent) {
        if (series.any { it.id == event.seriesId }) reloadJobsFlow.tryEmit(Unit)
    }

    private fun onReadProgressChanged(event: ReadProgressSeriesEvent) {
        if (series.any { it.id == event.seriesId }) reloadJobsFlow.tryEmit(Unit)
    }
}
