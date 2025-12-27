package snd.komelia.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressDeleteForBookAction(
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ) {
        val deleted = transactionTemplate.execute {
            val existing = readProgressRepository.find(bookId, userId)
            readProgressRepository.delete(bookId, userId)
            existing != null
        }

        if (deleted) {
            komgaEvents.emit(KomgaEvent.ReadProgressDeleted(bookId, userId))
        }
    }
}