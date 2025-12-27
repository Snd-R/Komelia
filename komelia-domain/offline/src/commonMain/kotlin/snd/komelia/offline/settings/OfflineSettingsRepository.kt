package snd.komelia.offline.settings

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

interface OfflineSettingsRepository {
    fun getOfflineMode(): Flow<Boolean>
    suspend fun putOfflineMode(offline: Boolean)
    fun getUserId(): Flow<KomgaUserId>
    suspend fun putUserId(userId: KomgaUserId)

    fun getReadProgressSyncDate(): Flow<Instant?>
    suspend fun putReadProgressSyncDate(timestamp: Instant)

    fun getDataSyncDate(): Flow<Instant?>
    suspend fun putDataSyncDate(timestamp: Instant)


    fun getDownloadDirectory(): Flow<PlatformFile>
    suspend fun putDownloadDirectory(path: PlatformFile)
}