package io.github.snd_r.komelia.ui.readlist

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
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.readlist.KomgaReadList
import io.github.snd_r.komga.readlist.KomgaReadListClient
import io.github.snd_r.komga.readlist.KomgaReadListId
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.BookChanged
import io.github.snd_r.komga.sse.KomgaEvent.BookDeleted
import io.github.snd_r.komga.sse.KomgaEvent.ReadListChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressDeleted
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadListViewModel(
    private val readListId: KomgaReadListId,
    private val readListClient: KomgaReadListClient,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    lateinit var readList: KomgaReadList

    var books by mutableStateOf<List<KomgaBook>>(emptyList())

    var totalBookPages by mutableStateOf(1)
        private set
    var totalBookCount by mutableStateOf(0)
        private set
    var currentBookPage by mutableStateOf(1)
        private set
    var pageLoadSize by mutableStateOf(100)
        private set

    private val reloadJobsFlow = MutableSharedFlow<Unit>(0, 1, BufferOverflow.DROP_OLDEST)
    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            loadReadList()
            loadBooks(1)
        }

        reloadJobsFlow.onEach {
            loadBooks(currentBookPage)
            delay(1000)
        }
        screenModelScope.launch { startEventListener() }
    }

    fun bookMenuActions() = BookMenuActions(bookClient, notifications, screenModelScope)

    fun onReadListDelete() {
        notifications.runCatchingToNotifications(screenModelScope) {
            readListClient.deleteOne(readListId)
        }
    }

    fun onPageSizeChange(pageSize: Int) {
        pageLoadSize = pageSize
        notifications.runCatchingToNotifications(screenModelScope) {
            loadBooks(1)
        }
    }

    fun onPageChange(pageNumber: Int) {
        notifications.runCatchingToNotifications(screenModelScope) {
            loadBooks(pageNumber)
        }
    }

    private suspend fun loadReadList() {
        notifications.runCatchingToNotifications {
            readList = readListClient.getOne(readListId)
        }.onFailure { mutableState.value = Error(it) }

    }

    private suspend fun loadBooks(page: Int) {
        if (state.value is Error) return

        notifications.runCatchingToNotifications {
            mutableState.value = LoadState.Loading
            val pageRequest = KomgaPageRequest(
                page = page - 1,
                size = pageLoadSize,
            )
            val readListPage = readListClient.getBooksForReadList(readListId, pageRequest = pageRequest)

            currentBookPage = readListPage.number + 1
            totalBookPages = readListPage.totalPages
            totalBookCount = readListPage.totalElements
            books = readListPage.content
            mutableState.value = LoadState.Success(Unit)

        }.onFailure { mutableState.value = Error(it) }
    }

    private suspend fun startEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is ReadListChanged -> onReadListChange(event)
                is BookChanged -> onBookChange(event)
                is BookDeleted -> onBookChange(event)
                is ReadProgressChanged -> onReadProgressChange(event)
                is ReadProgressDeleted -> onReadProgressChange(event)
                else -> {}
            }
        }
    }

    private suspend fun onReadListChange(event: ReadListChanged) {
        if (event.readListId == readListId) loadReadList()
    }

    private fun onBookChange(event: KomgaEvent.BookEvent) {
        if (books.any { it.id == event.bookId }) reloadJobsFlow.tryEmit(Unit)
    }

    private fun onReadProgressChange(event: KomgaEvent.ReadProgressEvent) {
        if (books.any { it.id == event.bookId }) reloadJobsFlow.tryEmit(Unit)
    }

}