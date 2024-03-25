package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.series.BooksLayout.GRID
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.BookEvent
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SeriesViewModel(
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val seriesId: KomgaSeriesId,
    private val notifications: AppNotifications,
    private val events: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    var series by mutableStateOf<KomgaSeries?>(null)

    var books by mutableStateOf<List<KomgaBook>>(emptyList())
    var booksLoading by mutableStateOf(false)
    var booksPageSize by mutableStateOf(50)
    var booksLayout by mutableStateOf(GRID)

    var totalBookPages by mutableStateOf(1)
    var currentBookPage by mutableStateOf(1)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            loadSeries()
            loadBooksPage(1)
        }

        screenModelScope.launch { registerEventListener() }
    }

    fun reload() {
        screenModelScope.launch {
            mutableState.value = Loading
            loadSeries()
            loadBooksPage(1)
        }
    }

    private suspend fun loadSeries() {
        notifications.runCatchingToNotifications {
            series = seriesClient.getOneSeries(seriesId)
        }.onFailure { mutableState.value = Error(it) }
    }

    fun onLoadBookPage(page: Int) {
        screenModelScope.launch { loadBooksPage(page) }
    }

    private suspend fun loadBooksPage(page: Int) {
        if (state.value is Error) return
        notifications.runCatchingToNotifications {
            val loadThreshold = screenModelScope.async {
                delay(150)
                booksLoading = true
            }

            if (page !in 0..totalBookPages) return@runCatchingToNotifications

            val pageResponse = seriesClient.getBooks(
                seriesId, KomgaPageRequest(
                    page = page - 1,
                    size = booksPageSize,
                )
            )
            books = pageResponse.content
            currentBookPage = pageResponse.number + 1
            totalBookPages = pageResponse.totalPages

            loadThreshold.cancel()
            booksLoading = false
        }.onFailure { mutableState.value = Error(it) }
    }

    fun onBookPageSizeChange(pageSize: Int) {
        booksPageSize = pageSize
        screenModelScope.launch { loadBooksPage(1) }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, notifications, screenModelScope)
    fun bookMenuActions() = BookMenuActions(bookClient, notifications, screenModelScope)


    private suspend fun onBookReadProgressChanged(eventBookId: KomgaBookId) {
        if (books.any { it.id == eventBookId }) {
            loadBooksPage(currentBookPage)
        }
    }

    private suspend fun registerEventListener() {
        events.collect { event ->
            when (event) {
                is KomgaEvent.SeriesChanged -> onSeriesChanged(event.seriesId)
                is BookEvent -> onBookChanged(event.seriesId)
                is ReadProgressEvent -> onBookReadProgressChanged(event.bookId)
                else -> {}
            }
        }
    }

    private suspend fun onSeriesChanged(eventSeriesId: KomgaSeriesId) {
        if (eventSeriesId == seriesId) {
            loadSeries()
        }
    }

    private suspend fun onBookChanged(eventSeriesId: KomgaSeriesId) {
        if (eventSeriesId == seriesId) {
            loadBooksPage(currentBookPage)
        }
    }
}

enum class BooksLayout {
    GRID,
    LIST
}