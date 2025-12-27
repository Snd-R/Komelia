package snd.komelia.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.CollectionEvent

class LibraryCollectionsTabState(
    private val collectionApi: KomgaCollectionsApi,
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

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val collectionsReloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch { loadCollections(1) }
        startKomgaEventListener()

        collectionsReloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            loadCollections(currentPage)
            delay(1000)
        }.launchIn(screenModelScope)
    }

    fun reload() {
        screenModelScope.launch { loadCollections(1) }
    }

    fun onCollectionDelete(collectionId: KomgaCollectionId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            collectionApi.deleteOne(collectionId)
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
            val collectionsPage = collectionApi.getAll(libraryIds = libraryIds, pageRequest = pageRequest)

            currentPage = collectionsPage.number + 1
            totalPages = collectionsPage.totalPages
            totalCollections = collectionsPage.totalElements
            collections = collectionsPage.content
            mutableState.value = Success(Unit)

        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
    }

    private fun startKomgaEventListener() {
        events.onEach {
            when (it) {
                is CollectionEvent -> collectionsReloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }.launchIn(screenModelScope)
    }
}
