package snd.komelia.ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.common.cards.defaultCardWidth
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue.Some
import snd.komga.client.series.KomgaSeries
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.CollectionChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesEvent
import snd.komga.client.sse.KomgaEvent.SeriesChanged
import snd.komga.client.sse.KomgaEvent.SeriesDeleted
import snd.komga.client.sse.KomgaEvent.SeriesEvent

class CollectionViewModel(
    private val collectionId: KomgaCollectionId,
    private val collectionApi: KomgaCollectionsApi,
    private val notifications: AppNotifications,
    private val seriesApi: KomgaSeriesApi,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val taskEmitter: OfflineTaskEmitter,
    cardWidthFlow: Flow<Dp>
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var collection by mutableStateOf<KomgaCollection?>(null)
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
    var isInEditMode by mutableStateOf(false)
        private set
    var selectedSeries by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set

    private var isAnyItemDragging = MutableStateFlow(false)

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)
    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            loadCollection()
            loadSeriesPage(1)
        }


        isAnyItemDragging
            .filter { isDragging -> !isDragging && state.value != Uninitialized }
            .onEach {
                notifications.runCatchingToNotifications {
                    collectionApi.updateOne(
                        collectionId,
                        KomgaCollectionUpdateRequest(seriesIds = Some(series.map { it.id }))
                    )
                }
            }.launchIn(screenModelScope)
        startKomgaEventListener()

        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            reload()
            delay(1000)
        }.launchIn(screenModelScope)
    }

    suspend fun reload() {
        isAnyItemDragging.first { !it } // suspend while drag is in progress

        loadCollection()
        if (isInEditMode) loadAllSeries()
        else loadSeriesPage(currentSeriesPage)

        if (selectedSeries.isNotEmpty()) {
            val selectedIds = selectedSeries.map { it.id }
            selectedSeries = series.filter { it.id in selectedIds }
        }

    }

    fun seriesMenuActions() = SeriesMenuActions(seriesApi, notifications,taskEmitter, screenModelScope)

    fun onCollectionDelete() {
        notifications.runCatchingToNotifications(screenModelScope) {
            collectionApi.deleteOne(collectionId)
        }
    }

    fun onPageSizeChange(pageSize: Int) {
        pageLoadSize = pageSize
        screenModelScope.launch { loadSeriesPage(1) }
    }

    fun onPageChange(pageNumber: Int) {
        screenModelScope.launch { loadSeriesPage(pageNumber) }
    }

    fun setEditMode(editMode: Boolean) {
        this.isInEditMode = editMode

        if (editMode) {
            if (totalSeriesCount != series.size) screenModelScope.launch { loadAllSeries() }
        } else {
            if (pageLoadSize != series.size) screenModelScope.launch { loadSeriesPage(1) }
            selectedSeries = emptyList()
        }

    }

    fun onSeriesSelect(series: KomgaSeries) {
        if (selectedSeries.any { it.id == series.id }) {
            selectedSeries = selectedSeries.filter { it.id != series.id }
        } else this.selectedSeries += series
        if (selectedSeries.isNotEmpty()) setEditMode(true)
    }

    fun onSeriesReorder(fromIndex: Int, toIndex: Int) {
        val mutable = series.toMutableList()
        val moved = mutable.removeAt(fromIndex)
        mutable.add(toIndex, moved)

        series = mutable
    }

    fun onSeriesReorderDragStateChange(isDragging: Boolean) {
        isAnyItemDragging.value = isDragging
    }

    private suspend fun loadCollection() {
        notifications.runCatchingToNotifications {
            collection = collectionApi.getOne(collectionId)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun loadSeriesPage(page: Int) {
        loadSeries(
            KomgaPageRequest(pageIndex = page - 1, size = pageLoadSize)
        )
    }

    private suspend fun loadAllSeries() {
        loadSeries(KomgaPageRequest(unpaged = true))
    }

    private suspend fun loadSeries(pageRequest: KomgaPageRequest) {
        if (state.value is Error) return

        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            val collectionPage = collectionApi.getSeriesForCollection(collectionId, pageRequest = pageRequest)

            currentSeriesPage = collectionPage.number + 1
            totalSeriesPages = collectionPage.totalPages
            totalSeriesCount = collectionPage.totalElements
            series = collectionPage.content

            mutableState.value = Success(Unit)
        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventsHandler() {
        reloadEventsEnabled.value = true
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is CollectionChanged -> onCollectionChanged(event)
                is SeriesChanged -> onSeriesChanged(event)
                is SeriesDeleted -> onSeriesChanged(event)
                is ReadProgressSeriesChanged -> onReadProgressChanged(event)
                is ReadProgressSeriesEvent -> onReadProgressChanged(event)
                else -> {}
            }
        }.launchIn(screenModelScope)
    }

    private fun onCollectionChanged(event: CollectionChanged) {
        if (event.collectionId == collectionId) {
            reloadJobsFlow.tryEmit(Unit)
        }
    }

    private fun onSeriesChanged(event: SeriesEvent) {
        if (series.any { it.id == event.seriesId }) reloadJobsFlow.tryEmit(Unit)
    }

    private fun onReadProgressChanged(event: ReadProgressSeriesEvent) {
        if (series.any { it.id == event.seriesId }) reloadJobsFlow.tryEmit(Unit)
    }
}
