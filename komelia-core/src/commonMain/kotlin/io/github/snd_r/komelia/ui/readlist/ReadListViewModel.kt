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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.readlist.KomgaReadListUpdateRequest
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.BookDeleted
import snd.komga.client.sse.KomgaEvent.ReadListChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressChanged
import snd.komga.client.sse.KomgaEvent.ReadProgressDeleted

class ReadListViewModel(
    private val readListId: KomgaReadListId,
    private val readListClient: KomgaReadListClient,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    var readList by mutableStateOf<KomgaReadList?>(null)
    var books by mutableStateOf<List<KomgaBook>>(emptyList())

    var totalBookPages by mutableStateOf(1)
        private set
    var totalBookCount by mutableStateOf(0)
        private set
    var currentBookPage by mutableStateOf(1)
        private set
    var pageLoadSize by mutableStateOf(100)
        private set

    var isInEditMode by mutableStateOf(false)
        private set
    var selectedBooks by mutableStateOf<List<KomgaBook>>(emptyList())
        private set

    private var isAnyItemDragging = MutableStateFlow(false)

    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)
    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            loadReadList()
            loadBooks(1)
        }

        reloadJobsFlow.onEach {
            reload()
            delay(1000)
        }.launchIn(screenModelScope)

        isAnyItemDragging
            .filter { isDragging -> !isDragging && state.value != Uninitialized }
            .onEach {
                notifications.runCatchingToNotifications {
                    readListClient.updateOne(
                        readListId,
                        KomgaReadListUpdateRequest(bookIds = PatchValue.Some(books.map { it.id }))
                    )
                }
            }.launchIn(screenModelScope)
        screenModelScope.launch { startEventListener() }
    }

    suspend fun reload() {
        isAnyItemDragging.first { !it } // suspend while drag is in progress

        loadReadList()

        if (isInEditMode) loadAllBooks()
        else loadBooks(currentBookPage)

        if (selectedBooks.isNotEmpty()) {
            val selectedIds = selectedBooks.map { it.id }
            selectedBooks = books.filter { it.id in selectedIds }
        }

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

    fun setEditMode(editMode: Boolean) {
        this.isInEditMode = editMode

        if (editMode) {
            if (totalBookCount != books.size) screenModelScope.launch { loadAllBooks() }
        } else {
            if (pageLoadSize != books.size) screenModelScope.launch { loadBooks(1) }
            selectedBooks = emptyList()
        }

    }

    fun onBookSelect(book: KomgaBook) {
        if (selectedBooks.any { it.id == book.id }) {
            selectedBooks = selectedBooks.filter { it.id != book.id }
        } else this.selectedBooks += book
        if (selectedBooks.isNotEmpty()) setEditMode(true)
    }

    fun onBookReorder(fromIndex: Int, toIndex: Int) {
        val mutable = books.toMutableList()
        val moved = mutable.removeAt(fromIndex)
        mutable.add(toIndex, moved)

        books = mutable
    }

    fun onSeriesReorderDragStateChange(isDragging: Boolean) {
        isAnyItemDragging.value = isDragging
    }

    private suspend fun loadReadList() {
        notifications.runCatchingToNotifications {
            readList = readListClient.getOne(readListId)
        }.onFailure { mutableState.value = Error(it) }

    }

    private suspend fun loadAllBooks() {
        loadBooks(KomgaPageRequest(unpaged = true))
    }

    private suspend fun loadBooks(page: Int) {
        loadBooks(
            KomgaPageRequest(pageIndex = page - 1, size = pageLoadSize)
        )
    }

    private suspend fun loadBooks(pageRequest: KomgaPageRequest) {
        if (state.value is Error) return

        notifications.runCatchingToNotifications {
            mutableState.value = LoadState.Loading
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

    private fun onReadListChange(event: ReadListChanged) {
        if (event.readListId == readListId) reloadJobsFlow.tryEmit(Unit)
    }

    private fun onBookChange(event: KomgaEvent.BookEvent) {
        if (books.any { it.id == event.bookId }) reloadJobsFlow.tryEmit(Unit)
    }

    private fun onReadProgressChange(event: KomgaEvent.ReadProgressEvent) {
        if (books.any { it.id == event.bookId }) reloadJobsFlow.tryEmit(Unit)
    }

}