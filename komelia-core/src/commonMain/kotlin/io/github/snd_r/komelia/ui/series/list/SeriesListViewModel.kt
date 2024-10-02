package io.github.snd_r.komelia.ui.series.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.library.SeriesScreenFilter
import io.github.snd_r.komelia.ui.series.SeriesFilterState
import io.github.snd_r.komelia.ui.series.SeriesFilterState.Completion
import io.github.snd_r.komelia.ui.series.SeriesFilterState.Format
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesQuery
import snd.komga.client.series.KomgaSeriesSort
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesDeleted
import snd.komga.client.sse.KomgaEvent.ReadProgressSeriesEvent
import snd.komga.client.sse.KomgaEvent.SeriesAdded
import snd.komga.client.sse.KomgaEvent.SeriesChanged
import snd.komga.client.sse.KomgaEvent.SeriesDeleted
import snd.komga.client.sse.KomgaEvent.SeriesEvent

private val logger = KotlinLogging.logger {}

class SeriesListViewModel(
    private val seriesClient: KomgaSeriesClient,
    referentialClient: KomgaReferentialClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val settingsRepository: SettingsRepository,
    defaultSort: SeriesSort,
    libraryFlow: Flow<KomgaLibrary?>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    val library = libraryFlow.stateIn(screenModelScope, SharingStarted.Eagerly, null)
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)
    val pageLoadSize = MutableStateFlow(50)
    var series by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var totalSeriesPages by mutableStateOf(1)
        private set
    var totalSeriesCount by mutableStateOf(0)
        private set
    var currentSeriesPage by mutableStateOf(1)
        private set

    var isInEditMode by mutableStateOf(false)
        private set
    var selectedSeries by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set

    val filterState: SeriesFilterState = SeriesFilterState(
        defaultSort = defaultSort,
        library = library,
        referentialClient = referentialClient,
        appNotifications = notifications,
        onChange = { screenModelScope.launch { loadSeriesPage(1) } },
    )

    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)
    fun initialize(filter: SeriesScreenFilter? = null) {
        if (state.value !is LoadState.Uninitialized) return

        screenModelScope.launch {
            filterState.initialize()
            if (filter != null) filterState.applyFilter(filter)

            pageLoadSize.value = settingsRepository.getSeriesPageLoadSize().first()
            loadSeriesPage(1)

            settingsRepository.getSeriesPageLoadSize()
                .onEach {
                    if (pageLoadSize.value != it) {
                        pageLoadSize.value = it
                        loadSeriesPage(1)
                    }
                }.launchIn(screenModelScope)
        }

        reloadJobsFlow.onEach {
            loadSeriesPage(currentSeriesPage)
            delay(1000)
        }.launchIn(screenModelScope)

        screenModelScope.launch { startEventListener() }
    }

    fun reload() {
        screenModelScope.launch { loadSeriesPage(1) }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, notifications, screenModelScope)

    fun onPageSizeChange(pageSize: Int) {
        pageLoadSize.value = pageSize
        screenModelScope.launch { settingsRepository.putSeriesPageLoadSize(pageSize) }
        notifications.runCatchingToNotifications(screenModelScope) {
            loadSeriesPage(1)
        }
    }

    fun onPageChange(pageNumber: Int) {
        onEditModeChange(false)
        screenModelScope.launch { loadSeriesPage(pageNumber) }
    }

    fun onEditModeChange(editMode: Boolean) {
        this.isInEditMode = editMode
        if (!editMode) selectedSeries = emptyList()

    }

    fun onSeriesSelect(series: KomgaSeries) {
        if (selectedSeries.any { it.id == series.id }) {
            selectedSeries = selectedSeries.filter { it.id != series.id }
        } else this.selectedSeries += series

        if (selectedSeries.isNotEmpty() && !isInEditMode) onEditModeChange(true)
    }

    private suspend fun loadSeriesPage(page: Int) {
        notifications.runCatchingToNotifications {
            val loadStateDelay = delayLoadState()
            currentSeriesPage = page
            val seriesPage = getAllSeries(page)

            loadStateDelay.cancel()

            currentSeriesPage = seriesPage.number + 1
            totalSeriesPages = seriesPage.totalPages
            totalSeriesCount = seriesPage.totalElements
            series = seriesPage.content
            mutableState.value = Success(Unit)
        }.onFailure { mutableState.value = Error(it) }
    }

    private suspend fun getAllSeries(page: Int): Page<KomgaSeries> {
        val query = KomgaSeriesQuery(
            searchTerm = filterState.searchTerm.ifBlank { null },
            libraryIds = library.value?.let { listOf(it.id) },
            status = filterState.publicationStatus,
            readStatus = filterState.readStatus,
            publishers = filterState.publishers,
            languages = filterState.languages,
            genres = filterState.genres,
            tags = filterState.tags,
            ageRatings = filterState.ageRatings,
            releaseYears = filterState.releaseDates,
            authors = filterState.authors,
            complete = when (filterState.complete) {
                Completion.ANY -> null
                Completion.COMPLETE -> true
                Completion.INCOMPLETE -> false
            },
            oneshot = when (filterState.oneshot) {
                Format.ANY -> null
                Format.ONESHOT -> true
                Format.NOT_ONESHOT -> false
            }
        )
        val pageRequest = KomgaPageRequest(
            size = pageLoadSize.value,
            pageIndex = page - 1,
            sort = filterState.sortOrder.komgaSort
        )
        return seriesClient.getAllSeries(query, pageRequest)
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
        if (event.libraryId == library.value?.id) {
            reloadJobsFlow.tryEmit(Unit)
        }
    }

    private fun onReadProgressChange(event: ReadProgressSeriesEvent) {
        if (series.any { it.id == event.seriesId }) {
            reloadJobsFlow.tryEmit(Unit)
        }
    }


    enum class SeriesSort(val komgaSort: KomgaSeriesSort) {
        UPDATED_DESC(KomgaSeriesSort.byLastModifiedDateDesc()),
        UPDATED_ASC(KomgaSeriesSort.byLastModifiedDateAsc()),
        RELEASE_DATE_DESC(KomgaSeriesSort.byReleaseDateDesc()),
        RELEASE_DATE_ASC(KomgaSeriesSort.byReleaseDateAsc()),
        TITLE_ASC(KomgaSeriesSort.byTitleAsc()),
        TITLE_DESC(KomgaSeriesSort.byTitleDesc()),
        DATE_ADDED_DESC(KomgaSeriesSort.byCreatedDateDesc()),
        DATE_ADDED_ASC(KomgaSeriesSort.byCreatedDateAsc()),
        //        FOLDER_NAME_ASC(KomgaSeriesSort.byFolderNameAsc()),
//        FOLDER_NAME_DESC(KomgaSeriesSort.byFolderNameDesc()),
//        BOOKS_COUNT_ASC(KomgaSeriesSort.byBooksCountAsc()),
//        BOOKS_COUNT_DESC(KomgaSeriesSort.byBooksCountDesc())
    }

}
