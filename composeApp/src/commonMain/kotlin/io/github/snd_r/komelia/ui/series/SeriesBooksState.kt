package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SeriesBooksState(
    val series: StateFlow<KomgaSeries?>,
    private val settingsRepository: SettingsRepository,
    private val notifications: AppNotifications,
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val events: SharedFlow<KomgaEvent>,
    private val screenModelScope: CoroutineScope,
    val cardWidth: StateFlow<Dp>
) {
    private val mutableState = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val state = mutableState.asStateFlow()

    val booksPageSize = MutableStateFlow(20)
    val booksLayout = MutableStateFlow(BooksLayout.GRID)

    var books by mutableStateOf<List<KomgaBook>>(emptyList())
    var booksSelectionMode by mutableStateOf(false)
    var selectedBooks by mutableStateOf<List<KomgaBook>>(emptyList())

    var totalBookPages by mutableStateOf(1)
    var currentBookPage by mutableStateOf(1)

    suspend fun initialize() {
        if (state.value != LoadState.Uninitialized) return

        booksPageSize.value = settingsRepository.getBookPageLoadSize().first()
        booksLayout.value = settingsRepository.getBookListLayout().first()
        loadBooksPage(1)

        settingsRepository.getBookPageLoadSize()
            .onEach {
                if (booksPageSize.value != it) {
                    booksPageSize.value = it
                    loadBooksPage(1)
                }
            }.launchIn(screenModelScope)

        settingsRepository.getBookListLayout()
            .onEach { booksLayout.value = it }
            .launchIn(screenModelScope)

        screenModelScope.launch { registerEventListener() }
    }

    suspend fun reload() {
        loadBooksPage(1)
    }

    private suspend fun loadBooksPage(page: Int) {
        notifications.runCatchingToNotifications {
            mutableState.value = LoadState.Loading

            if (page !in 0..totalBookPages) return@runCatchingToNotifications

            val series = series.filterNotNull().first()
            val pageResponse = seriesClient.getBooks(
                series.id,
                KomgaPageRequest(
                    pageIndex = page - 1,
                    size = booksPageSize.value,
                )
            )
            books = pageResponse.content
            currentBookPage = pageResponse.number + 1
            totalBookPages = pageResponse.totalPages

            mutableState.value = LoadState.Success(Unit)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun onBookPageSizeChange(pageSize: Int) {
        booksPageSize.value = pageSize
        screenModelScope.launch {
            settingsRepository.putBookPageLoadSize(pageSize)
            loadBooksPage(1)
        }
    }

    suspend fun onPageChange(page: Int) {
        setSelectionMode(false)
        loadBooksPage(page)
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, notifications, screenModelScope)
    fun bookMenuActions() = BookMenuActions(bookClient, notifications, screenModelScope)

    fun onBookLayoutChange(layout: BooksLayout) {
        booksLayout.value = layout
        screenModelScope.launch { settingsRepository.putBookListLayout(layout) }
    }

    fun setSelectionMode(editMode: Boolean) {
        this.booksSelectionMode = editMode
        if (!editMode) selectedBooks = emptyList()

    }

    fun onBookSelect(book: KomgaBook) {
        if (selectedBooks.any { it.id == book.id }) {
            selectedBooks = selectedBooks.filter { it.id != book.id }
        } else this.selectedBooks += book

        if (selectedBooks.isNotEmpty()) setSelectionMode(true)
    }


    private suspend fun registerEventListener() {
        events.collect { event ->
            when (event) {
                is KomgaEvent.BookEvent -> onBookChanged(event.seriesId)
                is KomgaEvent.ReadProgressEvent -> onBookReadProgressChanged(event.bookId)
                else -> {}
            }
        }
    }

    private suspend fun onBookChanged(eventSeriesId: KomgaSeriesId) {
        if (eventSeriesId == series.value?.id) {
            loadBooksPage(currentBookPage)
        }
    }

    private suspend fun onBookReadProgressChanged(eventBookId: KomgaBookId) {
        if (books.any { it.id == eventBookId }) {
            loadBooksPage(currentBookPage)
        }
    }
}