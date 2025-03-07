package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.bulk.BookBulkActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest
import snd.komga.client.book.KomgaBooksSort
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent

class SeriesBooksState(
    private val series: StateFlow<KomgaSeries?>,
    private val settingsRepository: CommonSettingsRepository,
    private val notifications: AppNotifications,
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val events: SharedFlow<KomgaEvent>,
    private val screenModelScope: CoroutineScope,
    val cardWidth: StateFlow<Dp>,
    referentialClient: KomgaReferentialClient,
) {
    data class BooksData(
        val books: List<KomgaBook> = emptyList(),
        val pageSize: Int = 20,
        val totalPages: Int = 1,
        val currentPage: Int = 1,
        val layout: BooksLayout = BooksLayout.GRID,
        val selectionMode: Boolean = false,
        val selectedBooks: List<KomgaBook> = emptyList(),
    )

    private val mutableState = MutableStateFlow<LoadState<BooksData>>(LoadState.Uninitialized)
    val state = mutableState.asStateFlow()


    val filterState = BooksFilterState(
        series = this.series,
        referentialClient = referentialClient,
        appNotifications = notifications,
        onChange = { screenModelScope.launch { loadBookData(1) } },
    )

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)
    private val reloadMutex = Mutex()

    suspend fun initialize() {
        if (state.value != LoadState.Uninitialized) return

        loadBookData(1)
        filterState.initialize()

        screenModelScope.launch { startKomgaEventListener() }
        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            val state = state.value
            if (state is LoadState.Success) loadBookData(state.value.currentPage)
            delay(1000)
        }.launchIn(screenModelScope)
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

            val pageResponse = seriesClient.getAllBooksBySeries(
                seriesId = series.id,
                readStatus = filterState.readStatus,
                tag = filterState.tags,
                authors = filterState.authors,
                pageRequest = KomgaPageRequest(
                    pageIndex = page - 1,
                    size = pageLoadSize,
                    sort = filterState.sortOrder.komgaSort
                )
            )

            val newState = when (currentState) {
                is LoadState.Success<BooksData> -> currentState.value.copy(
                    books = pageResponse.content,
                    pageSize = pageLoadSize,
                    totalPages = pageResponse.totalPages,
                    currentPage = pageResponse.number + 1,
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

    fun bookMenuActions() = BookMenuActions(bookClient, notifications, screenModelScope)
    fun bookBulkMenuActions() = BookBulkActions(
        markAsRead = { books ->
            launchWithReloadLock {
                books.forEach {
                    bookClient.markReadProgress(it.id, KomgaBookReadProgressUpdateRequest(completed = true))
                }
            }
        },
        markAsUnread = { books -> launchWithReloadLock { books.forEach { bookClient.deleteReadProgress(it.id) } } },
        delete = { books -> launchWithReloadLock { books.forEach { bookClient.deleteBook(it.id) } } }
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

    fun onBookSelect(book: KomgaBook) {
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

    class BooksFilterState(
        private val series: StateFlow<KomgaSeries?>,
        private val referentialClient: KomgaReferentialClient,
        private val appNotifications: AppNotifications,
        private val onChange: () -> Unit,
    ) {

        var isChanged by mutableStateOf(false)
            private set
        var sortOrder by mutableStateOf(BooksSort.NUMBER_ASC)
            private set
        var readStatus by mutableStateOf<List<KomgaReadStatus>>(emptyList())
            private set
        var tags by mutableStateOf<List<String>>(emptyList())
            private set
        var tagOptions by mutableStateOf<List<String>>(emptyList())
            private set
        var authors by mutableStateOf<List<KomgaAuthor>>(emptyList())
            private set
        var authorsOptions by mutableStateOf<List<KomgaAuthor>>(emptyList())
            private set

        suspend fun initialize() {

            appNotifications.runCatchingToNotifications {
                val series = series.filterNotNull().first()
                tagOptions = referentialClient.getBookTags(seriesId = series.id)
                authorsOptions = referentialClient
                    .getAuthors(seriesId = series.id, pageRequest = KomgaPageRequest(unpaged = true)).content
                    .distinctBy { it.name }
            }
        }

        fun onSortOrderChange(sortOrder: BooksSort) {
            this.sortOrder = sortOrder
            markChanges()
            onChange()
        }

        fun onReadStatusSelect(readStatus: KomgaReadStatus) {
            if (this.readStatus.contains(readStatus)) {
                this.readStatus = this.readStatus.minus(readStatus)
            } else {
                this.readStatus = this.readStatus.plus(readStatus)
            }
            markChanges()
            onChange()
        }

        fun onAuthorSelect(author: KomgaAuthor) {
            val authorsByName = authorsOptions.filter { it.name == author.name }
            authors =
                if (authors.contains(author)) authors.filter { it.name != author.name }
                else authors.plus(authorsByName)

            markChanges()
            onChange()
        }

        fun onTagSelect(tag: String) {
            tags =
                if (tags.contains(tag)) tags.minus(tag)
                else tags.plus(tag)

            markChanges()
            onChange()
        }

        fun resetTagFilters() {
            tags = emptyList()
        }

        private fun markChanges() {
            val hasDefaultValues = sortOrder == BooksSort.NUMBER_ASC &&
                    readStatus.isEmpty() &&
                    tags.isEmpty() &&
                    authors.isEmpty()

            isChanged = !hasDefaultValues
        }

        enum class BooksSort(val komgaSort: KomgaBooksSort) {
            NUMBER_ASC(KomgaBooksSort.byNumberAsc()),
            NUMBER_DESC(KomgaBooksSort.byNumberDesc()),
//        FILENAME_ASC(KomgaBooksSort.byFileNameAsc()),
//        FILENAME_DESC(KomgaBooksSort.byFileNameDesc()),
//        RELEASE_DATE_ASC(KomgaBooksSort.byReleaseDateAsc()),
//        RELEASE_DATE_DESC(KomgaBooksSort.byReleaseDateDesc()),
        }
    }
}