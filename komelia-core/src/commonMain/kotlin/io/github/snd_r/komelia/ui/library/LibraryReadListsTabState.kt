package io.github.snd_r.komelia.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.sse.KomgaEvent

class LibraryReadListsTabState(
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    val library: StateFlow<KomgaLibrary?>?,
    val cardWidth: StateFlow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var readLists: List<KomgaReadList> by mutableStateOf(emptyList())
        private set
    var totalPages by mutableStateOf(1)
        private set
    var totalReadLists by mutableStateOf(0)
        private set
    var currentPage by mutableStateOf(1)
        private set
    var pageSize by mutableStateOf(50)
        private set

    private val komgaEventsScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val readListsReloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

    fun initialize() {
        if (state.value !is Uninitialized) return

        readListsReloadJobsFlow.onEach {
            loadReadLists(currentPage)
            delay(1000)
        }.launchIn(screenModelScope)

        screenModelScope.launch { loadReadLists(1) }
    }

    fun reload() {
        screenModelScope.launch { loadReadLists(1) }
    }

    fun onReadListDelete(readListId: KomgaReadListId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            readListClient.deleteOne(readListId)
        }
    }

    fun onPageChange(pageNumber: Int) {
        screenModelScope.launch { loadReadLists(pageNumber) }
    }

    fun onPageSizeChange(pageSize: Int) {
        this.pageSize = pageSize
        screenModelScope.launch { loadReadLists(1) }
    }

    private suspend fun loadReadLists(page: Int) {
        appNotifications.runCatchingToNotifications {

            if (totalReadLists > pageSize) mutableState.value = Loading

            val library = this.library?.value
            val libraryIds = if (library != null) listOf(library.id) else emptyList()
            val pageRequest = KomgaPageRequest(pageIndex = page - 1, size = pageSize)
            val readListsPage = readListClient.getAll(libraryIds = libraryIds, pageRequest = pageRequest)

            currentPage = readListsPage.number + 1
            totalPages = readListsPage.totalPages
            totalReadLists = readListsPage.totalElements
            readLists = readListsPage.content
            mutableState.value = LoadState.Success(Unit)

        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun stopKomgaEventListener() {
        komgaEventsScope.coroutineContext.cancelChildren()
    }

    fun startKomgaEventListener() {
        komgaEventsScope.coroutineContext.cancelChildren()
        komgaEvents.onEach {
            when (it) {
                is KomgaEvent.ReadListEvent -> readListsReloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }.launchIn(komgaEventsScope)
    }

    override fun onDispose() {
        komgaEventsScope.cancel()
    }
}