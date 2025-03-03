package io.github.snd_r.komelia.ui.readlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.ReadListEvent

class BookReadListsState(
    val book: StateFlow<KomgaBook?>,
    private val bookClient: KomgaBookClient,
    private val readListClient: KomgaReadListClient,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val stateScope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow<LoadState<Unit>>(Uninitialized)
    val state = mutableState.asStateFlow()

    var readLists by mutableStateOf<Map<KomgaReadList, List<KomgaBook>>>(emptyMap())
        private set

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)

    suspend fun initialize() {
        if (mutableState.value != Uninitialized) return

        loadReadLists()
        startKomgaEventListener()
        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            loadReadLists()
        }.launchIn(stateScope)
    }

    fun reload() {
        stateScope.launch { loadReadLists() }
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

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is BookChanged -> if (readLists.values.flatten().any { it.id == event.bookId })
                    reloadJobsFlow.tryEmit(Unit)

                is ReadListEvent -> if (readLists.keys.any { it.id == event.readListId })
                    reloadJobsFlow.tryEmit(Unit)

                else -> {}
            }
        }.launchIn(stateScope)
    }
}
