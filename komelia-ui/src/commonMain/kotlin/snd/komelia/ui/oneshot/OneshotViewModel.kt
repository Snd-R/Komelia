package snd.komelia.ui.oneshot

import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
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
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.collection.SeriesCollectionsState
import snd.komelia.ui.common.cards.defaultCardWidth
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.readlist.BookReadListsState
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.search.allOfBooks
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookAdded
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressDeleted
import snd.komga.client.sse.KomgaEvent.SeriesAdded
import snd.komga.client.sse.KomgaEvent.SeriesChanged

class OneshotViewModel(
    series: KomgaSeries?,
    book: KomeliaBook?,
    private val seriesId: KomgaSeriesId,
    private val seriesApi: KomgaSeriesApi,
    private val bookApi: KomgaBookApi,
    private val events: SharedFlow<KomgaEvent>,
    private val notifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    private val taskEmitter: OfflineTaskEmitter,
    settingsRepository: CommonSettingsRepository,
    readListApi: KomgaReadListApi,
    collectionApi: KomgaCollectionsApi,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)

    val series = MutableStateFlow(series)
    val library = MutableStateFlow<KomgaLibrary?>(null)
    val book = MutableStateFlow(book)
    val bookMenuActions = BookMenuActions(bookApi, notifications, screenModelScope, taskEmitter)

    val cardWidth = settingsRepository.getCardWidth().map { it.dp }
        .stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    val readListsState = BookReadListsState(
        book = this.book,
        bookApi = bookApi,
        readListApi = readListApi,
        notifications = notifications,
        komgaEvents = events,
        stateScope = screenModelScope,
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
        if (state.value != Uninitialized) return
        initState()
        book.filterNotNull().combine(libraries) { book, libraries ->
            val newLibrary = libraries.firstOrNull { it.id == book.libraryId }
            if (newLibrary == null) {
                mutableState.value =
                    Error(IllegalStateException("Failed to find library for oneshot ${book.metadata.title}"))
            }
            library.value = newLibrary
        }.launchIn(screenModelScope)

        startKomgaEventListener()

        reloadFlow.onEach {
            reloadEventsEnabled.first { it }
            loadSeries()
            loadBook()
        }.launchIn(screenModelScope)
    }

    private suspend fun initState() {
        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            if (this.series.value == null) {
                this.series.value = seriesApi.getOneSeries(seriesId)
            }

            val currentBook = this.book.value
                ?: bookApi.getBookList(allOfBooks { seriesId { isEqualTo(seriesId) } })
                    .content.first()
                    .also { this.book.value = it }


            this.library.value = getLibraryOrThrow(currentBook)
        }
            .onSuccess { mutableState.value = Success(Unit) }
            .onFailure { mutableState.value = Error(it) }
    }

    fun reload() {
        screenModelScope.launch {
            notifications.runCatchingToNotifications {
                mutableState.value = Loading
                val currentBook = book.value
                    ?: bookApi.getBookList(allOfBooks { seriesId { isEqualTo(seriesId) } })
                        .content.first()
                        .also { book.value = it }
                book.value = bookApi.getOne(currentBook.id)
                series.value = seriesApi.getOneSeries(seriesId)
                library.value = getLibraryOrThrow(currentBook)
            }
                .onSuccess { mutableState.value = Success(Unit) }
                .onFailure { mutableState.value = Error(it) }
        }
    }

    fun onBookDownload() {
        screenModelScope.launch {
            book.value?.let { taskEmitter.downloadBook(it.id) }
        }
    }

    fun onBookDownloadDelete() {
        screenModelScope.launch {
            taskEmitter.deleteSeries(seriesId)
        }
    }

    private suspend fun loadBook() {
        notifications.runCatchingToNotifications {
            val currentBook = requireNotNull(book.value)
            this.book.value = bookApi.getOne(currentBook.id)
        }.onFailure { mutableState.value = Error(it) }
    }

    private suspend fun loadSeries() {
        notifications.runCatchingToNotifications {
            series.value = seriesApi.getOneSeries(seriesId)
        }.onFailure { mutableState.value = Error(it) }
    }

    private fun getLibraryOrThrow(book: KomeliaBook): KomgaLibrary {
        val library = this.libraries.value.firstOrNull { it.id == book.libraryId }
        if (library == null) {
            throw IllegalStateException("Failed to find library for oneshot ${book.metadata.title}")
        }
        return library
    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
        readListsState.stopKomgaEventHandler()
        collectionsState.stopKomgaEventHandler()
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
        readListsState.startKomgaEventHandler()
        collectionsState.startKomgaEventHandler()
    }

    private fun startKomgaEventListener() {
        events.onEach { event ->
            when (event) {
                is SeriesChanged, is SeriesAdded ->
                    if (event.seriesId == seriesId) reloadFlow.tryEmit(Unit)

                is BookChanged, is BookAdded ->
                    if (event.bookId == book.value?.id) reloadFlow.tryEmit(Unit)

                is ReadProgressChanged, is ReadProgressDeleted ->
                    if (event.bookId == book.value?.id) reloadFlow.tryEmit(Unit)

                else -> {}
            }
        }.launchIn(screenModelScope)
    }
}
