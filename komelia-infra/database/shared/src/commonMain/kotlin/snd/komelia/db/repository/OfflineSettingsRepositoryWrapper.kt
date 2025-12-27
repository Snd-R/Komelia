package snd.komelia.db.repository

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import snd.komelia.db.OfflineSettings
import snd.komelia.db.SettingsStateWrapper
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

class OfflineSettingsRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<OfflineSettings>,
) : OfflineSettingsRepository {

    override fun getOfflineMode(): Flow<Boolean> {
        return wrapper.mapState { it.isOfflineModeEnabled }
    }

    override suspend fun putOfflineMode(offline: Boolean) {
        wrapper.transform { it.copy(isOfflineModeEnabled = offline) }
    }

    override fun getUserId(): Flow<KomgaUserId> {
        return wrapper.mapState { it.userId }
    }

    override suspend fun putUserId(userId: KomgaUserId) {
        wrapper.transform { it.copy(userId = userId) }
    }

    override fun getReadProgressSyncDate(): Flow<Instant?> {
        return wrapper.mapState { it.readProgressSyncDate }
    }

    override suspend fun putReadProgressSyncDate(timestamp: Instant) {
        wrapper.transform { it.copy(readProgressSyncDate = timestamp) }
    }

    override fun getDataSyncDate(): Flow<Instant?> {
        return wrapper.mapState { it.dataSyncDate }
    }

    override suspend fun putDataSyncDate(timestamp: Instant) {
        wrapper.transform { it.copy(dataSyncDate = timestamp) }
    }

    override fun getDownloadDirectory(): Flow<PlatformFile> {
        return wrapper.mapState { it.downloadDirectory }
    }

    override suspend fun putDownloadDirectory(path: PlatformFile) {
        wrapper.transform { it.copy(downloadDirectory = path) }
    }
}