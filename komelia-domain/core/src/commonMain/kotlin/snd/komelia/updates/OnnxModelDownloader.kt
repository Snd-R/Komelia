package snd.komelia.updates

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface OnnxModelDownloader {
    val downloadCompletionEvents: SharedFlow<CompletionEvent>
    fun mangaJaNaiDownload(): Flow<UpdateProgress>
    fun panelDownload(): Flow<UpdateProgress>

    sealed interface CompletionEvent {
        data object MangaJaNaiDownloaded : CompletionEvent
        data object PanelModelDownloaded : CompletionEvent
    }
}