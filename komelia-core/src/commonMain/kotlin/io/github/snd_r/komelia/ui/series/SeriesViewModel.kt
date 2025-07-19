package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.collection.SeriesCollectionsState
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
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
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent

class SeriesViewModel(
    series: KomgaSeries?,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    private val seriesId: KomgaSeriesId,
    private val notifications: AppNotifications,
    private val events: SharedFlow<KomgaEvent>,
    private val seriesClient: KomgaSeriesClient,
    bookClient: KomgaBookClient,
    collectionClient: KomgaCollectionClient,
    referentialClient: KomgaReferentialClient,
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
        seriesClient = seriesClient,
        bookClient = bookClient,
        events = events,
        screenModelScope = screenModelScope,
        cardWidth = cardWidth,
        referentialClient = referentialClient,
    )
    val collectionsState = SeriesCollectionsState(
        series = this.series,
        notifications = notifications,
        seriesClient = seriesClient,
        collectionClient = collectionClient,
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

        series.filterNotNull().combine(libraries) { series, libraries ->
            val newLibrary = libraries.firstOrNull { it.id == series.libraryId }
            if (newLibrary == null) {
                mutableState.value =
                    Error(IllegalStateException("Failed to find library for series ${series.metadata.title}"))
            }
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

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, notifications, screenModelScope)

    fun onTabChange(tab: SeriesTab) {
        this.currentTab = tab
    }

    private suspend fun loadSeries() {
        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            val series = seriesClient.getOneSeries(seriesId)
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

enum class BooksLayout {
    GRID,
    LIST
}