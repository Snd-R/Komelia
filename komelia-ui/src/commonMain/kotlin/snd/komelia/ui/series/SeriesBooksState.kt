package snd.komelia.ui.series

import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.model.BooksLayout
import snd.komelia.ui.LoadState
import snd.komelia.ui.book.BooksFilterState
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.common.menus.bulk.BookBulkActions
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.search.allOfBooks
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent

class SeriesBooksState(
    private val series: StateFlow<KomgaSeries?>,
    private val settingsRepository: CommonSettingsRepository,
    private val notifications: AppNotifications,
    private val bookApi: KomgaBookApi,
    private val events: SharedFlow<KomgaEvent>,
    private val taskEmitter: OfflineTaskEmitter,
    private val screenModelScope: CoroutineScope,
    val cardWidth: StateFlow<Dp>,
    referentialApi: KomgaReferentialApi,
) {
    data class BooksData(
        val books: List<KomeliaBook> = emptyList(),
        val pageSize: Int = 20,
        val totalPages: Int = 1,
        val currentPage: Int = 1,
        val layout: BooksLayout = BooksLayout.GRID,
        val selectionMode: Boolean = false,
        val selectedBooks: List<KomeliaBook> = emptyList(),
    )

    private val mutableState = MutableStateFlow<LoadState<BooksData>>(LoadState.Uninitialized)
    val state = mutableState.asStateFlow()


    val filterState = BooksFilterState(
        series = this.series,
        referentialApi = referentialApi,
        appNotifications = notifications,
    )

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)
    private val reloadMutex = Mutex()

    suspend fun initialize() {
        if (state.value != LoadState.Uninitialized) return

        filterState.initialize()
        loadBookData(1)

        screenModelScope.launch { startKomgaEventListener() }
        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            val state = state.value
            if (state is LoadState.Success) loadBookData(state.value.currentPage)
            delay(1000)
        }.launchIn(screenModelScope)

        filterState.state.drop(1)
            .onEach { loadBookData(1) }
            .launchIn(screenModelScope)
    }

    suspend fun reload() {
        when (val currentState = state.value) {
            is LoadState.Success<BooksData> -> loadBookData(currentState.value.currentPage)
            else -> loadBookData(1)
        }
    }

    private suspend fun loadBookData(page: Int) {
        notifications.runCatchingToNotifications {
            val currentState = state.value
            val pageLoadSize = when (currentState) {
                is LoadState.Success<BooksData> -> {
                    currentState.value.pageSize
                }

                else -> {
                    mutableState.value = LoadState.Loading
                    settingsRepository.getBookPageLoadSize().first()
                }
            }

            val series = series.filterNotNull().first()
            val filter = this.filterState.state.value
            val condition = allOfBooks {
                seriesId { isEqualTo(series.id) }
                filter.addConditionTo(this)
            }

            val pageResponse = bookApi.getBookList(
                conditionBuilder = condition,
                fullTextSearch = null,
                pageRequest = KomgaPageRequest(
                    pageIndex = page - 1,
                    size = pageLoadSize,
                    sort = filter.sortOrder.komgaSort
                )
            )

            val newState = when (currentState) {
                is LoadState.Success<BooksData> -> currentState.value.copy(
                    books = pageResponse.content,
                    pageSize = pageLoadSize,
                    totalPages = pageResponse.totalPages,
                    currentPage = pageResponse.number + 1,
                    selectedBooks = pageResponse.content.filter { it.id in currentState.value.selectedBooks.map { it.id } }
                )

                else -> BooksData(
                    books = pageResponse.content,
                    pageSize = pageLoadSize,
                    totalPages = pageResponse.totalPages,
                    currentPage = pageResponse.number + 1,
                    layout = settingsRepository.getBookListLayout().first(),
                    selectionMode = false,
                    selectedBooks = emptyList()
                )
            }
            mutableState.value = LoadState.Success(newState)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun onBookPageSizeChange(pageSize: Int) {
        updateCurrentState { it.copy(pageSize = pageSize) }
        screenModelScope.launch {
            settingsRepository.putBookPageLoadSize(pageSize)
            loadBookData(1)
        }
    }

    fun onPageChange(page: Int) {
        screenModelScope.launch {
            setSelectionMode(false)
            loadBookData(page)
        }
    }

    fun bookMenuActions() = BookMenuActions(bookApi, notifications, screenModelScope, taskEmitter)
    fun bookBulkMenuActions() = BookBulkActions(
        markAsRead = { books ->
            launchWithReloadLock {
                books.forEach {
                    bookApi.markReadProgress(it.id, KomgaBookReadProgressUpdateRequest(completed = true))
                }
            }
        },
        markAsUnread = { books -> launchWithReloadLock { books.forEach { bookApi.deleteReadProgress(it.id) } } },
        delete = { books -> launchWithReloadLock { books.forEach { bookApi.deleteBook(it.id) } } },
        download = { books -> launchWithReloadLock { books.forEach { taskEmitter.downloadBook(it.id) } } },
        deleteDownloaded = { books -> launchWithReloadLock { books.forEach { taskEmitter.deleteBook(it.id) } } }
    )

    private suspend fun launchWithReloadLock(block: suspend () -> Unit) {
        notifications.runCatchingToNotifications {
            reloadMutex.withLock {
                block()
            }
        }
    }

    fun onBookLayoutChange(layout: BooksLayout) {
        updateCurrentState { it.copy(layout = layout) }
        screenModelScope.launch { settingsRepository.putBookListLayout(layout) }
    }

    fun setSelectionMode(editMode: Boolean) {
        updateCurrentState {
            it.copy(
                selectionMode = editMode,
                selectedBooks = if (!editMode) emptyList() else it.selectedBooks
            )
        }
    }

    fun onBookSelect(book: KomeliaBook) {
        val currState = state.value
        if (currState !is LoadState.Success<BooksData>) return
        val currentlySelected = currState.value.selectedBooks

        if (currentlySelected.any { it.id == book.id }) {
            val selection = currentlySelected.filter { it.id != book.id }
            updateCurrentState { state ->
                state.copy(
                    selectedBooks = currentlySelected.filter { it.id != book.id },
                    selectionMode = selection.isNotEmpty()
                )
            }
        } else updateCurrentState { state ->
            state.copy(
                selectedBooks = state.selectedBooks + book,
                selectionMode = true
            )
        }
    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
    }

    private suspend fun startKomgaEventListener() {
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
            reloadMutex.withLock { reloadJobsFlow.tryEmit(Unit) }
        }
    }

    private suspend fun onBookReadProgressChanged(eventBookId: KomgaBookId) {
        val currentState = state.value
        if (currentState !is LoadState.Success<BooksData>) return

        if (currentState.value.books.any { it.id == eventBookId }) {
            reloadMutex.withLock { reloadJobsFlow.tryEmit(Unit) }
        }
    }

    private fun updateCurrentState(transform: (settings: BooksData) -> BooksData) {
        mutableState.update {
            when (it) {
                is LoadState.Success<BooksData> -> LoadState.Success(transform(it.value))
                else -> it
            }
        }
    }


}