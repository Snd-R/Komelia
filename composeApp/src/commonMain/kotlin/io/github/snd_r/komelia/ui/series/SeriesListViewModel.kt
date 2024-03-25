package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.series.KomgaSeriesQuery
import io.github.snd_r.komga.series.KomgaSeriesSort
import io.github.snd_r.komga.series.KomgaSeriesSort.TITLE_ASC
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressSeriesChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressSeriesDeleted
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressSeriesEvent
import io.github.snd_r.komga.sse.KomgaEvent.SeriesAdded
import io.github.snd_r.komga.sse.KomgaEvent.SeriesChanged
import io.github.snd_r.komga.sse.KomgaEvent.SeriesDeleted
import io.github.snd_r.komga.sse.KomgaEvent.SeriesEvent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class SeriesListViewModel(
    val library: StateFlow<KomgaLibrary>?,
    private val seriesClient: KomgaSeriesClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

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

    var sortOrder by mutableStateOf(TITLE_ASC)

    private val reloadJobsFlow = MutableSharedFlow<Unit>(0, 1, DROP_OLDEST)
    fun initialize() {
        if (state.value !is LoadState.Uninitialized) return

        reloadJobsFlow.onEach {
            loadSeriesPage(currentSeriesPage)
            delay(1000)
        }.launchIn(screenModelScope)

        screenModelScope.launch { loadSeriesPage(1) }

        screenModelScope.launch { startEventListener() }
    }

    fun reload() {
        screenModelScope.launch { loadSeriesPage(1) }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, notifications, screenModelScope)

    fun onPageSizeChange(pageSize: Int) {
        pageLoadSize = pageSize
        notifications.runCatchingToNotifications(screenModelScope) {
            loadSeriesPage(1)
        }
    }

    fun onPageChange(pageNumber: Int) {
        screenModelScope.launch { loadSeriesPage(pageNumber) }
    }

    fun onSortOrderChange(sort: KomgaSeriesSort) {
        sortOrder = sort
        screenModelScope.launch { loadSeriesPage(currentSeriesPage) }
    }

    private suspend fun loadSeriesPage(page: Int) {
        notifications.runCatchingToNotifications {
            val loadStateDelay = delayLoadState()
            currentSeriesPage = page

            val query = library?.value?.let { KomgaSeriesQuery(libraryIds = listOf(it.id)) }
            val pageRequest = KomgaPageRequest(
                size = pageLoadSize,
                page = page - 1,
                sort = sortOrder.query
            )

            val seriesPage = seriesClient.getAllSeries(query, pageRequest)
            loadStateDelay.cancel()

            currentSeriesPage = seriesPage.number + 1
            totalSeriesPages = seriesPage.totalPages
            totalSeriesCount = seriesPage.totalElements
            series = seriesPage.content
            mutableState.value = Success(Unit)
        }.onFailure { mutableState.value = Error(it) }
    }

    private fun delayLoadState(): Deferred<Unit> {
        return screenModelScope.async {
            delay(200)
            if (state.value !is Error) mutableState.value = Loading
        }
    }

    private suspend fun startEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is SeriesChanged -> onSeriesChange(event)
                is SeriesAdded -> onSeriesChange(event)
                is SeriesDeleted -> onSeriesChange(event)
                is ReadProgressSeriesChanged -> onReadProgressChange(event)
                is ReadProgressSeriesDeleted -> onReadProgressChange(event)
                else -> {}
            }
        }
    }

    private fun onSeriesChange(event: SeriesEvent) {
        if (library == null || event.libraryId == library.value.id) {
            reloadJobsFlow.tryEmit(Unit)
        }
    }

    private fun onReadProgressChange(event: ReadProgressSeriesEvent) {
        if (series.any { it.id == event.seriesId }) {
            reloadJobsFlow.tryEmit(Unit)
        }
    }
}
