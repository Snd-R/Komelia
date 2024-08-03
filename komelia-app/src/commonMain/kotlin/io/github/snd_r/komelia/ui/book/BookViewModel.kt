package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.ReadListEvent
import snd.komga.client.sse.KomgaEvent.ReadProgressChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressDeleted

class BookViewModel(
    book: KomgaBook?,
    private val bookId: KomgaBookId,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    settingsRepository: SettingsRepository,
    readListClient: KomgaReadListClient,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var library by mutableStateOf<KomgaLibrary?>(null)
        private set
    val book = MutableStateFlow(book)

    val readListsState = BookReadListsState(
        book = this.book,
        bookClient = bookClient,
        readListClient = readListClient,
        notifications = notifications,
        komgaEvents = komgaEvents,
        screenModelScope = screenModelScope,
    )
    val cardWidth = settingsRepository.getCardWidth().stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)


    fun initialize() {
        if (state.value != Uninitialized) return

        screenModelScope.launch { registerEventListener() }
        screenModelScope.launch {
            if (book.value == null) loadBook()
            else mutableState.value = Success(Unit)

            loadLibrary()
        }
        screenModelScope.launch { readListsState.initialize() }
    }

    private suspend fun loadBook() {
        mutableState.value = Loading
        val loadedBook = bookClient.getBook(bookId)
        book.value = loadedBook
        mutableState.value = Success(Unit)
    }

    private fun loadLibrary() {
        val book = requireNotNull(book.value)
        library = libraries.value.firstOrNull { library -> library.id == book.libraryId }
    }

    fun bookMenuActions() = BookMenuActions(bookClient, notifications, screenModelScope)

    private suspend fun registerEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is BookChanged -> if (event.bookId == bookId) loadBook()
                is ReadProgressChanged -> if (event.bookId == bookId) loadBook()
                is ReadProgressDeleted -> if (event.bookId == bookId) loadBook()
                else -> {}
            }
        }
    }

    class BookReadListsState(
        val book: StateFlow<KomgaBook?>,
        private val bookClient: KomgaBookClient,
        private val readListClient: KomgaReadListClient,
        private val notifications: AppNotifications,
        private val komgaEvents: SharedFlow<KomgaEvent>,
        private val screenModelScope: CoroutineScope,
    ) {
        private val mutableState = MutableStateFlow<LoadState<Unit>>(Uninitialized)
        val state = mutableState.asStateFlow()

        var readLists by mutableStateOf<Map<KomgaReadList, List<KomgaBook>>>(emptyMap())
            private set

        suspend fun initialize() {
            if (mutableState.value != Uninitialized) return

            loadReadLists()
            screenModelScope.launch { registerEventListener() }
        }

        private suspend fun loadReadLists() {
            notifications.runCatchingToNotifications {
                mutableState.value = Loading
                val book = book.filterNotNull().first()
                val readLists = bookClient.getAllReadListsByBook(book.id)

                this.readLists = readLists.associateWith { readList ->
                    readListClient.getBooksForReadList(
                        id = readList.id,
                        pageRequest = KomgaPageRequest(size = 500)
                    ).content
                }
                mutableState.value = Success(Unit)
            }.onFailure { mutableState.value = Error(it) }
        }

        private suspend fun registerEventListener() {
            komgaEvents.collect { event ->
                when (event) {
                    is BookChanged -> if (readLists.values.flatten().any { it.id == event.bookId }) loadReadLists()
                    is ReadListEvent -> if (readLists.keys.any { it.id == event.readListId }) loadReadLists()

                    else -> {}
                }
            }
        }
    }
}