package io.github.snd_r.komelia.ui.book

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
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.readlist.BookReadListsState
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressDeleted

class BookViewModel(
    book: KomgaBook?,
    private val bookId: KomgaBookId,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    settingsRepository: CommonSettingsRepository,
    readListClient: KomgaReadListClient,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var library by mutableStateOf<KomgaLibrary?>(null)
        private set
    val book = MutableStateFlow(book)

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)

    val readListsState = BookReadListsState(
        book = this.book,
        bookClient = bookClient,
        readListClient = readListClient,
        notifications = notifications,
        komgaEvents = komgaEvents,
        stateScope = screenModelScope,
    )
    val cardWidth = settingsRepository.getCardWidth().map { it.dp }
        .stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    val bookMenuActions = BookMenuActions(bookClient, notifications, screenModelScope)

    suspend fun initialize() {
        if (state.value != Uninitialized) return

        if (book.value == null) loadBook()
        else mutableState.value = Success(Unit)
        loadLibrary()
        readListsState.initialize()
        startKomgaEventListener()

        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            reload()
        }.launchIn(screenModelScope)
    }

    fun reload() {
        screenModelScope.launch {
            loadBook()
            loadLibrary()
            readListsState.reload()
        }
    }

    private suspend fun loadBook() {
        notifications.runCatchingToNotifications {
            mutableState.value = Loading
            val loadedBook = bookClient.getBook(bookId)
            book.value = loadedBook
        }
            .onSuccess { mutableState.value = Success(Unit) }
            .onFailure { mutableState.value = Error(it) }
    }

    private fun loadLibrary() {
        val book = requireNotNull(book.value)
        library = libraries.value.firstOrNull { library -> library.id == book.libraryId }
    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
        readListsState.stopKomgaEventHandler()
    }

    fun startKomgaEventsHandler() {
        reloadEventsEnabled.value = true
        readListsState.startKomgaEventHandler()
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is BookChanged -> if (event.bookId == bookId) reloadJobsFlow.tryEmit(Unit)
                is ReadProgressChanged -> if (event.bookId == bookId) reloadJobsFlow.tryEmit(Unit)
                is ReadProgressDeleted -> if (event.bookId == bookId) reloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }.launchIn(screenModelScope)
    }
}