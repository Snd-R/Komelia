package io.github.snd_r.komelia.ui.oneshot

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
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.readlist.BookReadListsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressDeleted
import snd.komga.client.sse.KomgaEvent.SeriesChanged

class OneshotViewModel(
    series: KomgaSeries?,
    book: KomgaBook?,
    private val seriesId: KomgaSeriesId,
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val events: SharedFlow<KomgaEvent>,
    private val notifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    settingsRepository: CommonSettingsRepository,
    readListClient: KomgaReadListClient,
    collectionClient: KomgaCollectionClient,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val series = MutableStateFlow(series)
    val library = MutableStateFlow<KomgaLibrary?>(null)
    val book = MutableStateFlow(book)
    val bookMenuActions = BookMenuActions(bookClient, notifications, screenModelScope)

    val cardWidth = settingsRepository.getCardWidth().map { it.dp }
        .stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    val readListsState = BookReadListsState(
        book = this.book,
        bookClient = bookClient,
        readListClient = readListClient,
        notifications = notifications,
        komgaEvents = events,
        stateScope = screenModelScope,
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
    private val komgaEventsScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
    }

    private suspend fun initState() {
        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            if (this.series.value == null) {
                this.series.value = seriesClient.getOneSeries(seriesId)
            }

            val currentBook = this.book.value
                ?: seriesClient.getAllBooksBySeries(seriesId).content.first()
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
                    ?: seriesClient.getAllBooksBySeries(seriesId).content.first().also { book.value = it }
                book.value = bookClient.getBook(currentBook.id)
                series.value = seriesClient.getOneSeries(seriesId)
                library.value = getLibraryOrThrow(currentBook)
            }
                .onSuccess { mutableState.value = Success(Unit) }
                .onFailure { mutableState.value = Error(it) }
        }
    }

    private suspend fun loadBook() {
        notifications.runCatchingToNotifications {
            val currentBook = requireNotNull(book.value)
            this.book.value = bookClient.getBook(currentBook.id)
        }.onFailure { mutableState.value = Error(it) }
    }

    private suspend fun loadSeries() {
        notifications.runCatchingToNotifications {
            series.value = seriesClient.getOneSeries(seriesId)
        }.onFailure { mutableState.value = Error(it) }
    }

    private fun getLibraryOrThrow(book: KomgaBook): KomgaLibrary {
        val library = this.libraries.value.firstOrNull { it.id == book.libraryId }
        if (library == null) {
            throw IllegalStateException("Failed to find library for oneshot ${book.metadata.title}")
        }
        return library

    }

    fun stopKomgaEventListener() {
        komgaEventsScope.coroutineContext.cancelChildren()
    }

    fun startKomgaEventListener() {
        komgaEventsScope.coroutineContext.cancelChildren()
        events.onEach { event ->
            when (event) {
                is SeriesChanged -> if (event.seriesId == seriesId) loadSeries()
                is BookChanged -> if (event.bookId == book.value?.id) loadBook()
                is ReadProgressChanged -> if (event.bookId == book.value?.id) loadBook()
                is ReadProgressDeleted -> if (event.bookId == book.value?.id) loadBook()
                else -> {}
            }
        }.launchIn(komgaEventsScope)
    }

    override fun onDispose() {
        komgaEventsScope.cancel()
    }
}
