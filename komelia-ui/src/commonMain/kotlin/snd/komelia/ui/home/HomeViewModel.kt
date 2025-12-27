package snd.komelia.ui.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.homefilters.BooksHomeScreenFilter
import snd.komelia.homefilters.HomeScreenFilter
import snd.komelia.homefilters.HomeScreenFilterRepository
import snd.komelia.homefilters.SeriesHomeScreenFilter
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.common.cards.defaultCardWidth
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookEvent
import snd.komga.client.sse.KomgaEvent.ReadProgressEvent
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesEvent
import snd.komga.client.sse.KomgaEvent.SeriesEvent

private val logger = KotlinLogging.logger { }

class HomeViewModel(
    private val seriesApi: KomgaSeriesApi,
    private val bookApi: KomgaBookApi,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val filterRepository: HomeScreenFilterRepository,
    private val taskEmitter: OfflineTaskEmitter,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)

    val currentFilters = MutableStateFlow(emptyList<HomeFilterData>())
    val activeFilterNumber = MutableStateFlow(0)

    suspend fun initialize() {
        if (state.value !is Uninitialized) return

        load()
        startKomgaEventListener()

        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            load()
            delay(5000)
        }.launchIn(screenModelScope)
    }

    fun reload() {
        screenModelScope.launch { load() }
    }

    private suspend fun load() {
        appNotifications.runCatchingToNotifications {
            mutableState.value = LoadState.Loading

            currentFilters.value = filterRepository.getFilters().first()
                .map { screenModelScope.async { fetchFilterData(it) } }
                .awaitAll()
                .filterNotNull()

            mutableState.value = LoadState.Success(Unit)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun fetchFilterData(filter: HomeScreenFilter): HomeFilterData? {
        return when (filter) {
            is BooksHomeScreenFilter.CustomFilter -> {
                val books = bookApi.getBookList(
                    search = KomgaBookSearch(filter.filter, filter.textSearch),
                    pageRequest = filter.pageRequest
                ).content

                BookFilterData(books = books, filter = filter)
            }

            is BooksHomeScreenFilter.OnDeck -> {
                val books = bookApi.getBooksOnDeck(pageRequest = KomgaPageRequest(size = filter.pageSize)).content
                BookFilterData(books, filter)
            }

            is SeriesHomeScreenFilter.CustomFilter -> {
                val series = seriesApi.getSeriesList(
                    search = KomgaSeriesSearch(filter.filter, filter.textSearch),
                    pageRequest = filter.pageRequest
                ).content

                SeriesFilterData(series = series, filter = filter)
            }

            is SeriesHomeScreenFilter.RecentlyAdded -> {
                val series = seriesApi.getNewSeries(
                    oneshot = false,
                    pageRequest = KomgaPageRequest(size = filter.pageSize)
                ).content
                SeriesFilterData(
                    series = series,
                    filter = filter
                )
            }

            is SeriesHomeScreenFilter.RecentlyUpdated -> {
                val series = seriesApi.getUpdatedSeries(
                    oneshot = false,
                    pageRequest = KomgaPageRequest(size = filter.pageSize)
                ).content
                SeriesFilterData(
                    series = series,
                    filter = filter
                )
            }
        }

    }

    fun seriesMenuActions() = SeriesMenuActions(seriesApi, appNotifications, taskEmitter, screenModelScope)
    fun bookMenuActions() = BookMenuActions(bookApi, appNotifications, screenModelScope, taskEmitter)

    fun stopKomgaEventsHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventsHandler() {
        reloadEventsEnabled.value = true
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is BookEvent,
                is SeriesEvent,
                is ReadProgressEvent,
                is ReadProgressSeriesEvent -> reloadJobsFlow.tryEmit(Unit)

                else -> {}
            }
        }.launchIn(screenModelScope)
    }

    fun onFilterChange(number: Int) {
        this.activeFilterNumber.value = number
    }

}
