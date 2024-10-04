package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.collection.SeriesCollectionsState
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.settings.CommonSettingsRepository

class SeriesViewModel(
    series: KomgaSeries?,
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

    val series = MutableStateFlow(series)
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
        if (series.value == null) loadSeries()
        else mutableState.value = Success(Unit)

        booksState.initialize()
        collectionsState.initialize()
        registerEventListener()
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

    private fun registerEventListener() {
        events.onEach { event ->
            when (event) {
                is KomgaEvent.SeriesChanged -> if (event.seriesId == seriesId) loadSeries()
                else -> {}
            }
        }.launchIn(screenModelScope)
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