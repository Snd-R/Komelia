package snd.komelia.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.ui.LoadState
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komelia.ui.series.SeriesFilter
import snd.komelia.ui.series.SeriesFilterState
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort.KomgaSeriesSort
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.search.allOfSeries
import snd.komga.client.series.KomgaSeries
import snd.komga.client.sse.KomgaEvent

class LibrarySeriesTabState(
    private val seriesApi: KomgaSeriesApi,
    referentialApi: KomgaReferentialApi,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val settingsRepository: CommonSettingsRepository,
    private val library: StateFlow<KomgaLibrary?>,
    private val taskEmitter: OfflineTaskEmitter,
    val cardWidth: StateFlow<Dp>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    val pageLoadSize = MutableStateFlow(50)
    var series by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var totalSeriesPages by mutableStateOf(1)
        private set
    var totalSeriesCount by mutableStateOf(0)
        private set
    var currentSeriesPage by mutableStateOf(1)
        private set

    val isInEditMode = MutableStateFlow(false)
    var selectedSeries by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set

    val filterState: SeriesFilterState = SeriesFilterState(
        defaultSort = SeriesSort.TITLE_ASC,
        library = library,
        referentialApi = referentialApi,
        appNotifications = notifications,
    )

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

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
        startKomgaEventListener()

        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            loadSeriesPage(currentSeriesPage)
            delay(1000)
        }.launchIn(screenModelScope)

        filterState.state.drop(1).onEach { loadSeriesPage(1) }
            .launchIn(screenModelScope)
    }

    fun reload() {
        screenModelScope.launch {
            loadSeriesPage(1)
        }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesApi, notifications, taskEmitter, screenModelScope)

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
        this.isInEditMode.value = editMode
        if (!editMode) {
            selectedSeries = emptyList()
        }

    }

    fun onSeriesSelect(series: KomgaSeries) {
        if (selectedSeries.any { it.id == series.id }) {
            selectedSeries = selectedSeries.filter { it.id != series.id }
        } else this.selectedSeries += series

        if (selectedSeries.isNotEmpty() && !isInEditMode.value) onEditModeChange(true)
    }

    private suspend fun loadSeriesPage(page: Int) {
        notifications.runCatchingToNotifications {
            val loadStateDelay = delayLoadState()
            currentSeriesPage = page
            val seriesPage = getAllSeries(page, filterState.state.value)

            loadStateDelay.cancel()

            currentSeriesPage = seriesPage.number + 1
            totalSeriesPages = seriesPage.totalPages
            totalSeriesCount = seriesPage.totalElements
            series = seriesPage.content
            mutableState.value = LoadState.Success(Unit)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun getAllSeries(
        page: Int,
        filter: SeriesFilter
    ): Page<KomgaSeries> {
        val condition = allOfSeries {
            library.value?.let { library { isEqualTo(it.id) } }
            filter.addConditionTo(this)
        }

        return seriesApi.getSeriesList(
            conditionBuilder = condition,
            fulltextSearch = filter.searchTerm.ifBlank { null },
            pageRequest = KomgaPageRequest(
                size = pageLoadSize.value,
                pageIndex = page - 1,
                sort = filter.sortOrder.komgaSort
            )
        )
    }

    private fun delayLoadState(): Deferred<Unit> {
        return screenModelScope.async {
            delay(200)
            if (state.value !is LoadState.Error) mutableState.value = LoadState.Loading
        }
    }


    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is KomgaEvent.SeriesChanged -> onSeriesChange(event)
                is KomgaEvent.SeriesAdded -> onSeriesChange(event)
                is KomgaEvent.SeriesDeleted -> onSeriesChange(event)
                is KomgaEvent.ReadProgressSeriesChanged -> onReadProgressChange(event)
                is KomgaEvent.ReadProgressSeriesDeleted -> onReadProgressChange(event)
                else -> {}
            }
        }.launchIn(screenModelScope)
    }

    private fun onSeriesChange(event: KomgaEvent.SeriesEvent) {
        if (library.value == null || event.libraryId == library.value?.id) {
            reloadJobsFlow.tryEmit(Unit)
        }
    }

    private fun onReadProgressChange(event: KomgaEvent.ReadProgressSeriesEvent) {
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