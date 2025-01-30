package io.github.snd_r.komelia

import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.ui.KomgaSharedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.LibraryEvent
import snd.komga.client.sse.KomgaEvent.TaskQueueStatus
import snd.komga.client.sse.KomgaEvent.ThumbnailBookAdded
import snd.komga.client.sse.KomgaEvent.ThumbnailBookDeleted
import snd.komga.client.sse.KomgaEvent.ThumbnailCollectionEvent
import snd.komga.client.sse.KomgaEvent.ThumbnailReadListEvent
import snd.komga.client.sse.KomgaEvent.ThumbnailSeriesAdded
import snd.komga.client.sse.KomgaEvent.ThumbnailSeriesDeleted
import snd.komga.client.sse.KomgaSSESession
import kotlin.concurrent.Volatile

private val logger = KotlinLogging.logger {}

class ManagedKomgaEvents(
    private val eventSourceFactory: suspend () -> KomgaSSESession,
    private val memoryCache: MemoryCache?,
    private val diskCache: DiskCache?,

    private val libraryClient: KomgaLibraryClient,
    private val komgaSharedState: KomgaSharedState,
) {
    private val manageScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + SupervisorJob())
    private val broadcastScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var session: KomgaSSESession? = null

    init {
        komgaSharedState.authenticatedUser.onEach { newUser ->
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
            komgaSharedState.updateLibraries(libraryClient.getLibraries())
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