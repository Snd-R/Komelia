package snd.komelia.offline.user.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.model.toOfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komga.client.user.KomgaUser

class UserKomgaImportAction(
    private val userRepository: OfflineUserRepository,
    private val transactionTemplate: TransactionTemplate
) : OfflineAction {

    suspend fun execute(user: KomgaUser, serverId: OfflineMediaServerId): OfflineUser {
        return transactionTemplate.execute {
            val offlineUser = user.toOfflineUser(serverId)
            userRepository.save(offlineUser)
            offlineUser
        }
    }
}