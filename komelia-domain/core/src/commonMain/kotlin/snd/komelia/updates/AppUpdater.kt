package snd.komelia.updates

import kotlinx.coroutines.flow.Flow

interface AppUpdater {

    suspend fun getReleases(): List<AppRelease>

    suspend fun updateToLatest(): Flow<UpdateProgress>?

    fun updateTo(release: AppRelease): Flow<UpdateProgress>?
}
