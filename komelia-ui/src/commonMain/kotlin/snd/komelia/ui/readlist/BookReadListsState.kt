package snd.komelia.ui.readlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.ReadListEvent

class BookReadListsState(
    val book: StateFlow<KomeliaBook?>,
    private val bookApi: KomgaBookApi,
    private val readListApi: KomgaReadListApi,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val stateScope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow<LoadState<Unit>>(Uninitialized)
    val state = mutableState.asStateFlow()

    var readLists by mutableStateOf<Map<KomgaReadList, List<KomeliaBook>>>(emptyMap())
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
            val readLists = bookApi.getAllReadListsByBook(book.id)

            this.readLists = readLists.associateWith { readList ->
                readListApi.getBooksForReadList(
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
