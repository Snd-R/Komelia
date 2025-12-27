package snd.komelia.db

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.user.model.OfflineUser
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

@Serializable
data class OfflineSettings(
    val isOfflineModeEnabled: Boolean = false,
    val downloadDirectory: PlatformFile,
    val userId: KomgaUserId = OfflineUser.ROOT,
    val serverId: OfflineMediaServerId? = null,
    val readProgressSyncDate: Instant? = null,
    val dataSyncDate: Instant? = null,
)
