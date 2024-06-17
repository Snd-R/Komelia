package io.github.snd_r.komelia.updates

import kotlinx.coroutines.flow.Flow

interface AppUpdater {

    suspend fun getReleases(): List<AppRelease>

    suspend fun updateToLatest(): Flow<UpdateProgress>?

    fun updateTo(release: AppRelease): Flow<UpdateProgress>?
}

data class UpdateProgress(
    val total: Long,
    val completed: Long,
    val description: String? = null,
)