package snd.komelia.offline.user.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class UserDeleteAction(
    private val userRepository: OfflineUserRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val settingsRepository: OfflineSettingsRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun execute(userId: KomgaUserId) {
        return transactionTemplate.execute {
            userRepository.find(userId) ?: return@execute
            if (settingsRepository.getUserId().first() == userId) {
                settingsRepository.putUserId(OfflineUser.ROOT)
            }

            readProgressRepository.deleteByUserId(userId)
            userRepository.delete(userId)
            komgaEvents.emit(KomgaEvent.SessionExpired(userId))
        }
    }
}