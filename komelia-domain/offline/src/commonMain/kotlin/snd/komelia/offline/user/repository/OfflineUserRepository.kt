package snd.komelia.offline.user.repository

import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.user.model.OfflineUser
import snd.komga.client.user.KomgaUserId

interface OfflineUserRepository {
    suspend fun save(user: OfflineUser)

    suspend fun get(id: KomgaUserId): OfflineUser
    suspend fun find(id: KomgaUserId): OfflineUser?
    suspend fun findAll(): List<OfflineUser>
    suspend fun findAllByServer(serverId: OfflineMediaServerId): List<OfflineUser>
    suspend fun delete(id: KomgaUserId)
}