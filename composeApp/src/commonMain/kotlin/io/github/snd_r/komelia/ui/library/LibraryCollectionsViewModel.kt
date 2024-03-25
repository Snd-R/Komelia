package io.github.snd_r.komelia.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.CollectionEvent
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

class LibraryCollectionsViewModel(
    val library: StateFlow<KomgaLibrary>?,
    private val collectionClient: KomgaCollectionClient,
    private val appNotifications: AppNotifications,
    private val events: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)
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

    private val collectionsReloadJobsFlow = MutableSharedFlow<Unit>(0, 1, BufferOverflow.DROP_OLDEST)

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

            val pageRequest = KomgaPageRequest(page = page - 1, size = pageSize)
            val libraryIds = if (library != null) listOf(library.value.id) else emptyList()
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