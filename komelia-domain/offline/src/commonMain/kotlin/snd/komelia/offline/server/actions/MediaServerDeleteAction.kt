package snd.komelia.offline.server.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.library.actions.LibraryDeleteAction
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.user.actions.UserDeleteAction
import snd.komelia.offline.user.repository.OfflineUserRepository

class MediaServerDeleteAction(
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val libraryRepository: OfflineLibraryRepository,
    private val userRepository: OfflineUserRepository,
    private val transactionTemplate: TransactionTemplate,
    private val libraryDeleteAction: LibraryDeleteAction,
    private val userDeleteAction: UserDeleteAction,
) : OfflineAction {
    suspend fun execute(serverId: OfflineMediaServerId) {
        return transactionTemplate.execute {
            mediaServerRepository.find(serverId) ?: return@execute

            val libraries = libraryRepository.findAllByMediaServer(serverId)
            for (library in libraries) {
                libraryDeleteAction.execute(library.id)
            }

            val users = userRepository.findAllByServer(serverId)
            for (user in users) {
                userDeleteAction.execute(user.id)
            }

            mediaServerRepository.delete(serverId)
        }
    }
}