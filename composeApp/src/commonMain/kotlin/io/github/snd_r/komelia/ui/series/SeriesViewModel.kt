package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.*
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SeriesViewModel(
    series: KomgaSeries?,
    private val seriesId: KomgaSeriesId,
    private val seriesClient: KomgaSeriesClient,
    private val notifications: AppNotifications,
    private val events: SharedFlow<KomgaEvent>,
    bookClient: KomgaBookClient,
    collectionClient: KomgaCollectionClient,
    settingsRepository: SettingsRepository,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val series = MutableStateFlow(series)
    var currentTab by mutableStateOf(SeriesTab.BOOKS)
    val cardWidth = settingsRepository.getCardWidth().stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    val booksState = SeriesBooksState(
        series = this.series,
        settingsRepository = settingsRepository,
        notifications = notifications,
        seriesClient = seriesClient,
        bookClient = bookClient,
        events = events,
        screenModelScope = screenModelScope,
        cardWidth = cardWidth,
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

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            if (series.value == null) loadSeries()
            else mutableState.value = Success(Unit)
        }
        screenModelScope.launch { booksState.initialize() }
        screenModelScope.launch { collectionsState.initialize() }
        screenModelScope.launch { registerEventListener() }
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
            series.value = seriesClient.getOneSeries(seriesId)
            mutableState.value = Success(Unit)
        }.onFailure { mutableState.value = Error(it) }
    }

    private suspend fun registerEventListener() {
        events.collect { event ->
            when (event) {
                is KomgaEvent.SeriesChanged -> onSeriesChanged(event.seriesId)
                else -> {}
            }
        }
    }

    private suspend fun onSeriesChanged(eventSeriesId: KomgaSeriesId) {
        if (eventSeriesId == seriesId) {
            loadSeries()
        }
    }

    enum class SeriesTab {
        BOOKS,
        COLLECTIONS
    }
}

enum class BooksLayout {
    GRID,
    LIST
}