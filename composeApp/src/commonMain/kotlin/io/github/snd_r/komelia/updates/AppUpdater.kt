package io.github.snd_r.komelia.updates

import kotlinx.coroutines.flow.Flow

interface AppUpdater {

    suspend fun getReleases(): List<AppRelease>

    suspend fun updateToLatest(): Flow<DownloadProgress>?

    fun updateTo(release: AppRelease): Flow<DownloadProgress>?
}

data class DownloadProgress(
    val total: Long,
    val downloaded: Long,
)