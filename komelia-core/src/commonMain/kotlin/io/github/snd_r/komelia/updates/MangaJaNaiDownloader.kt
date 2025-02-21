package io.github.snd_r.komelia.updates

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface MangaJaNaiDownloader {
    val downloadCompletionEventFlow: SharedFlow<Unit>
    fun download(): Flow<UpdateProgress>
}