package io.github.snd_r.komelia.ui.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookEvent
import snd.komga.client.sse.KomgaEvent.ReadProgressEvent
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesEvent
import snd.komga.client.sse.KomgaEvent.SeriesEvent

class HomeViewModel(
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val filterRepository: HomeScreenFilterRepository,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)

    val filters = MutableStateFlow(emptyList<HomeFilterData>())
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

            filters.value = filterRepository.getFilters().first()
                .mapNotNull { fetchFilterData(it) }

            mutableState.value = LoadState.Success(Unit)

        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun fetchFilterData(filter: HomeScreenFilter): HomeFilterData? {
        return when (filter) {
            is BooksHomeScreenFilter.CustomFilter -> {
                val books = bookClient.getBookList(
                    search = KomgaBookSearch(filter.filter, filter.textSearch),
                    pageRequest = filter.pageRequest
                ).content

                BookFilterData(books = books, filter = filter)
            }

            is BooksHomeScreenFilter.OnDeck -> {
                val books = bookClient.getBooksOnDeck(pageRequest = KomgaPageRequest(size = filter.pageSize)).content
                BookFilterData(books, filter)
            }

            is SeriesHomeScreenFilter.CustomFilter -> {
                val series = seriesClient.getSeriesList(
                    search = KomgaSeriesSearch(filter.filter, filter.textSearch),
                    pageRequest = filter.pageRequest
                ).content

                SeriesFilterData(series = series, filter = filter)
            }

            is SeriesHomeScreenFilter.RecentlyAdded -> {
                val series = seriesClient.getNewSeries(
                    oneshot = false,
                    pageRequest = KomgaPageRequest(size = filter.pageSize)
                ).content
                SeriesFilterData(
                    series = series,
                    filter = filter
                )
            }

            is SeriesHomeScreenFilter.RecentlyUpdated -> {
                val series = seriesClient.getUpdatedSeries(
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

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, appNotifications, screenModelScope)
    fun bookMenuActions() = BookMenuActions(bookClient, appNotifications, screenModelScope)

    fun stopKomgaEventsHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventsHandler() {
        reloadEventsEnabled.value = true
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is BookEvent -> {
                    reloadJobsFlow.tryEmit(Unit)
                }

                is SeriesEvent -> {
                    reloadJobsFlow.tryEmit(Unit)
                }

                is ReadProgressEvent -> {
                    val matches = false
                    if (matches) reloadJobsFlow.tryEmit(Unit)

                }

                is ReadProgressSeriesEvent -> {
                    val matches = false
                    if (matches) reloadJobsFlow.tryEmit(Unit)
                }

                else -> {}
            }
        }.launchIn(screenModelScope)
    }

    fun onFilterChange(number: Int) {
        this.activeFilterNumber.value = number
    }

}
