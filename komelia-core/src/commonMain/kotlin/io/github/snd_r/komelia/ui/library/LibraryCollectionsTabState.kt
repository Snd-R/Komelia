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
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.CollectionEvent

class LibraryCollectionsTabState(
    private val collectionClient: KomgaCollectionClient,
    private val appNotifications: AppNotifications,
    private val events: SharedFlow<KomgaEvent>,
    private val library: StateFlow<KomgaLibrary?>,
    val cardWidth: StateFlow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    var collections: List<KomgaCollection> by mutableStateOf(emptyList())
        private set
    var totalPages by mutableStateOf(1)
        private set
    var totalCollections by mutableStateOf(0)
        private set
    var currentPage by mutableStateOf(1)
        private set
    var pageSize by mutableStateOf(50)
        private set

    private val collectionsReloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch { startEventListener() }

        collectionsReloadJobsFlow.onEach {
            loadCollections(currentPage)
            delay(1000)
        }.launchIn(screenModelScope)

        screenModelScope.launch { loadCollections(1) }
    }

    fun reload() {
        screenModelScope.launch { loadCollections(1) }
    }

    fun onCollectionDelete(collectionId: KomgaCollectionId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            collectionClient.deleteOne(collectionId)
        }
    }

    fun onPageChange(pageNumber: Int) {
        screenModelScope.launch { loadCollections(pageNumber) }
    }

    fun onPageSizeChange(pageSize: Int) {
        this.pageSize = pageSize
        screenModelScope.launch { loadCollections(1) }
    }

    private suspend fun loadCollections(page: Int) {
        appNotifications.runCatchingToNotifications {

            if (totalCollections > pageSize) mutableState.value = Loading

            val pageRequest = KomgaPageRequest(pageIndex = page - 1, size = pageSize)
            val libraryIds = listOfNotNull(library.value?.id)
            val collectionsPage = collectionClient.getAll(libraryIds = libraryIds, pageRequest = pageRequest)

            currentPage = collectionsPage.number + 1
            totalPages = collectionsPage.totalPages
            totalCollections = collectionsPage.totalElements
            collections = collectionsPage.content
            mutableState.value = Success(Unit)

        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }


    private suspend fun startEventListener() {
        events.collect {
            when (it) {
                is CollectionEvent -> collectionsReloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }
    }
}