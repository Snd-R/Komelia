package snd.komelia.offline.server.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.server.model.OfflineMediaServer
import snd.komelia.offline.server.repository.OfflineMediaServerRepository

class MediaServerSaveAction(
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val transactionTemplate: TransactionTemplate
) : OfflineAction {
    suspend fun execute(serverUrl: String): OfflineMediaServer {
        return transactionTemplate.execute {
            val existing = mediaServerRepository.findByUrl(serverUrl)
            if (existing != null) return@execute existing

            val mediaServer = OfflineMediaServer(url = serverUrl)
            mediaServerRepository.save(mediaServer)
            return@execute mediaServer
        }
    }
}