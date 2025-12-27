package snd.komelia.ui.settings.offline.downloads

import coil3.PlatformContext
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadCompleted
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadError
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadProgress
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komga.client.book.KomgaBookId

class OfflineDownloadsState(
    downloadEvents: SharedFlow<DownloadEvent>,
    platformContext: PlatformContext,
    private val taskEmitter: OfflineTaskEmitter,
    private val settingsRepository: OfflineSettingsRepository,
    private val coroutineScope: CoroutineScope,
) {
    private val internalDownloadDir = getDefaultInternalDownloadsDir(platformContext)
    private val downloadsMap = MutableStateFlow<Map<KomgaBookId, DownloadEvent>>(emptyMap())
    val downloads = downloadsMap.map { it.values }
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    val storageLocation = settingsRepository.getDownloadDirectory()
        .stateIn(coroutineScope, SharingStarted.Eagerly,null)

    init {
        downloadEvents.onEach { event ->
            when (event) {
                is BookDownloadProgress, is BookDownloadCompleted -> updateDownloads(event)
                is BookDownloadError -> handleErrorEvent(event)
            }
        }.launchIn(coroutineScope)
    }

    private fun handleErrorEvent(event: BookDownloadError) {
        if (event.book == null) {
            val previousEvent = downloadsMap.value[event.bookId]
            val newEvent =
                if (previousEvent is BookDownloadProgress) event.copy(book = previousEvent.book)
                else event
            updateDownloads(newEvent)
        } else {
            updateDownloads(event)
        }
    }

    fun onStorageLocationChange(directory: PlatformFile) {
        coroutineScope.launch { settingsRepository.putDownloadDirectory(directory) }
    }

    fun onStorageLocationReset() {
        coroutineScope.launch { settingsRepository.putDownloadDirectory(internalDownloadDir.platformFile) }
    }

    private fun updateDownloads(event: DownloadEvent) {
        downloadsMap.update {
            val mutable = it.toMutableMap()
            mutable[event.bookId] = event
            mutable
        }
    }

    fun onDownloadCancel(bookId: KomgaBookId) {
        coroutineScope.launch { taskEmitter.cancelBookDownload(bookId) }
    }
}

internal data class DefaultDownloadStorageLocation(
    val platformFile: PlatformFile,
    val label: String,
)

internal expect fun getDefaultInternalDownloadsDir(platformContent: PlatformContext): DefaultDownloadStorageLocation