package io.github.snd_r.komelia

import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.LibraryEvent
import io.github.snd_r.komga.sse.KomgaEvent.TaskQueueStatus
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailBookAdded
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailBookDeleted
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailSeriesAdded
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailSeriesDeleted
import io.github.snd_r.komga.sse.KomgaEventSource
import io.github.snd_r.komga.user.KomgaUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class ManagedKomgaEvents(
    authenticatedUser: StateFlow<KomgaUser?>,
    private val eventSource: KomgaEventSource,
    private val memoryCache: MemoryCache?,
    private val diskCache: DiskCache?,

    private val libraryClient: KomgaLibraryClient,
    private val librariesFlow: MutableStateFlow<List<KomgaLibrary>>
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        authenticatedUser.onEach { newUser ->
            if (newUser == null) eventSource.disconnect()
            else eventSource.connect()
        }.launchIn(scope)

        startBroadcast()
    }

    private val _events = MutableSharedFlow<KomgaEvent>()
    val events: SharedFlow<KomgaEvent> = _events

    private fun startBroadcast() {
        eventSource.incoming.onEach { event ->
            logger.info { event }

            when (event) {
                is TaskQueueStatus -> {}
                is ThumbnailBookAdded -> removeBookThumbnailCache(event.bookId)
                is ThumbnailBookDeleted -> removeBookThumbnailCache(event.bookId)

                is ThumbnailSeriesAdded -> removeSeriesThumbnailCache(event.seriesId)
                is ThumbnailSeriesDeleted -> removeSeriesThumbnailCache(event.seriesId)

                is LibraryEvent -> updateLibraries()

                else -> {}
            }

            _events.emit(event)
        }.launchIn(scope)
    }

    private fun updateLibraries() {
        scope.launch {
            librariesFlow.value = libraryClient.getLibraries()
        }
    }

    private fun removeSeriesThumbnailCache(seriesId: KomgaSeriesId) {
        memoryCache?.remove(MemoryCache.Key(seriesId.value))
        diskCache?.remove((seriesId.value))
    }

    private fun removeBookThumbnailCache(bookId: KomgaBookId) {
        memoryCache?.remove(MemoryCache.Key(bookId.value))
        diskCache?.remove((bookId.value))
    }
}