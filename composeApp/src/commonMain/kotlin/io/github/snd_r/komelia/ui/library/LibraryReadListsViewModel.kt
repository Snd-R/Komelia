package io.github.snd_r.komelia.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.defaultCardWidth
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.readlist.KomgaReadList
import io.github.snd_r.komga.readlist.KomgaReadListClient
import io.github.snd_r.komga.readlist.KomgaReadListId
import io.github.snd_r.komga.sse.KomgaEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryReadListsViewModel(
    val library: StateFlow<KomgaLibrary>?,
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)

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

    private val readListsReloadJobsFlow = MutableSharedFlow<Unit>(0, 1, BufferOverflow.DROP_OLDEST)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch { startEventListener() }

        readListsReloadJobsFlow.onEach {
            loadReadLists(currentPage)
            delay(1000)
        }.launchIn(screenModelScope)

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

            val libraryIds = if (library != null) listOf(library.value.id) else emptyList()
            val pageRequest = KomgaPageRequest(page = page - 1, size = pageSize)
            val readListsPage = readListClient.getAll(libraryIds = libraryIds, pageRequest = pageRequest)

            currentPage = readListsPage.number + 1
            totalPages = readListsPage.totalPages
            totalReadLists = readListsPage.totalElements
            readLists = readListsPage.content
            mutableState.value = LoadState.Success(Unit)

        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    private suspend fun startEventListener() {
        komgaEvents.collect {
            when (it) {
                is KomgaEvent.ReadListEvent -> readListsReloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }
    }
}