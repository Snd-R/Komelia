package snd.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.collection.SeriesCollectionsState
import snd.komelia.ui.common.cards.defaultCardWidth
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent

class SeriesViewModel(
    series: KomgaSeries?,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    private val seriesId: KomgaSeriesId,
    private val notifications: AppNotifications,
    private val events: SharedFlow<KomgaEvent>,
    private val seriesApi: KomgaSeriesApi,
    private val taskEmitter: OfflineTaskEmitter,
    bookApi: KomgaBookApi,
    collectionApi: KomgaCollectionsApi,
    referentialApi: KomgaReferentialApi,
    settingsRepository: CommonSettingsRepository,
    defaultTab: SeriesTab,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

    val series = MutableStateFlow(series?.withSortedTags())
    val library = MutableStateFlow<KomgaLibrary?>(null)
    var currentTab by mutableStateOf(defaultTab)
    val cardWidth = settingsRepository.getCardWidth().map { it.dp }
        .stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    val booksState = SeriesBooksState(
        series = this.series,
        settingsRepository = settingsRepository,
        notifications = notifications,
        bookApi = bookApi,
        events = events,
        screenModelScope = screenModelScope,
        cardWidth = cardWidth,
        referentialApi = referentialApi,
        taskEmitter = taskEmitter
    )
    val collectionsState = SeriesCollectionsState(
        series = this.series,
        notifications = notifications,
        seriesApi = seriesApi,
        collectionApi = collectionApi,
        events = events,
        screenModelScope = screenModelScope,
        cardWidth = cardWidth,
    )

    suspend fun initialize() {
        if (state.value !is Uninitialized) return

        val providedSeries = series.value
        if (providedSeries == null) loadSeries()
        else {
            runCatching {
                library.value = getLibraryOrThrow(providedSeries)
                mutableState.value = Success(Unit)
            }.onFailure { mutableState.value = Error(it) }
        }

        series.filterNotNull()
            .combine(libraries) { series, libraries ->
                val newLibrary = libraries.firstOrNull { it.id == series.libraryId }
                library.value = newLibrary
            }.launchIn(screenModelScope)

        booksState.initialize()
        collectionsState.initialize()
        startKomgaEventListener()

        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            loadSeries()
        }.launchIn(screenModelScope)
    }

    fun reload() {
        screenModelScope.launch {
            mutableState.value = Loading
            loadSeries()
            booksState.reload()
        }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesApi, notifications, taskEmitter, screenModelScope)

    fun onTabChange(tab: SeriesTab) {
        this.currentTab = tab
    }

    fun onDownload() {
        screenModelScope.launch {
            series.value?.let { taskEmitter.downloadSeries(it.id) }
        }
    }

    private suspend fun loadSeries() {
        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            val series = seriesApi.getOneSeries(seriesId)
            this.series.value = series.withSortedTags()
            this.library.value = getLibraryOrThrow(series)

            mutableState.value = Success(Unit)

        }.onFailure { mutableState.value = Error(it) }
    }

    private fun getLibraryOrThrow(series: KomgaSeries): KomgaLibrary {
        val library = this.libraries.value.firstOrNull { it.id == series.libraryId }
        if (library == null) {
            throw IllegalStateException("Failed to find library for series ${series.metadata.title}")
        }
        return library

    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
        booksState.stopKomgaEventHandler()
        collectionsState.stopKomgaEventHandler()
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
        booksState.startKomgaEventHandler()
        collectionsState.startKomgaEventHandler()
    }

    private fun startKomgaEventListener() {
        events.onEach { event ->
            when (event) {
                is KomgaEvent.SeriesChanged -> if (event.seriesId == seriesId) reloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }.launchIn(screenModelScope)
    }

    enum class SeriesTab {
        BOOKS,
        COLLECTIONS
    }

    private fun KomgaSeries.withSortedTags() = this.copy(
        metadata = this.metadata.copy(
            tags = this.metadata.tags.sorted(),
            genres = this.metadata.genres.sorted()
        )
    )
}
