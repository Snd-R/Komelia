package io.github.snd_r.komelia

import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.readlist.KomgaReadListId
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.LibraryEvent
import io.github.snd_r.komga.sse.KomgaEvent.TaskQueueStatus
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailBookAdded
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailBookDeleted
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailCollectionEvent
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailReadListEvent
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailSeriesAdded
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailSeriesDeleted
import io.github.snd_r.komga.sse.KomgaSSESession
import io.github.snd_r.komga.user.KomgaUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

private val logger = KotlinLogging.logger {}

class ManagedKomgaEvents(
    authenticatedUser: StateFlow<KomgaUser?>,
    private val eventSourceFactory: suspend () -> KomgaSSESession,
    private val memoryCache: MemoryCache?,
    private val diskCache: DiskCache?,

    private val libraryClient: KomgaLibraryClient,
    private val librariesFlow: MutableStateFlow<List<KomgaLibrary>>
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val manageScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + SupervisorJob())
    private val broadcastScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var session: KomgaSSESession? = null

    init {
        authenticatedUser.onEach { newUser ->
            broadcastScope.coroutineContext.cancelChildren()
            session?.cancel()

            if (newUser != null) {
                val newSession = eventSourceFactory()
                session = newSession
                startBroadcast(newSession.incoming)
            }
        }.launchIn(manageScope)
    }

    private val _events = MutableSharedFlow<KomgaEvent>()
    val events: SharedFlow<KomgaEvent> = _events

    private fun startBroadcast(events: Flow<KomgaEvent>) {
        events.onEach { event ->
            logger.info { event }

            when (event) {
                is TaskQueueStatus -> {}
                is ThumbnailBookAdded -> removeBookThumbnailCache(event.bookId)
                is ThumbnailBookDeleted -> removeBookThumbnailCache(event.bookId)

                is ThumbnailSeriesAdded -> removeSeriesThumbnailCache(event.seriesId)
                is ThumbnailSeriesDeleted -> removeSeriesThumbnailCache(event.seriesId)

                is ThumbnailCollectionEvent -> removeCollectionThumbnailCache(event.collectionId)
                is ThumbnailReadListEvent -> removeReadListThumbnailCache(event.readListId)

                is LibraryEvent -> updateLibraries()

                else -> {}
            }

            _events.emit(event)
        }.launchIn(broadcastScope)
    }

    private fun updateLibraries() {
        manageScope.launch {
            librariesFlow.value = libraryClient.getLibraries()
        }
    }

    private fun removeSeriesThumbnailCache(seriesId: KomgaSeriesId) {
        removeMemCacheValues(seriesId.value)
        diskCache?.remove((seriesId.value))
    }

    private fun removeBookThumbnailCache(bookId: KomgaBookId) {
        removeMemCacheValues(bookId.value)
        diskCache?.remove((bookId.value))
    }

    private fun removeCollectionThumbnailCache(collectionId: KomgaCollectionId) {
        removeMemCacheValues(collectionId.value)
        diskCache?.remove((collectionId.value))
    }

    private fun removeReadListThumbnailCache(readListId: KomgaReadListId) {
        removeMemCacheValues(readListId.value)
        diskCache?.remove((readListId.value))

    }

    private fun removeMemCacheValues(key: String) {
        memoryCache?.remove(MemoryCache.Key(key))
        memoryCache?.remove(MemoryCache.Key(key, mapOf("scale" to "Fit")))
        memoryCache?.remove(MemoryCache.Key(key, mapOf("scale" to "Crop")))
        memoryCache?.remove(MemoryCache.Key(key, mapOf("scale" to "")))

    }
}