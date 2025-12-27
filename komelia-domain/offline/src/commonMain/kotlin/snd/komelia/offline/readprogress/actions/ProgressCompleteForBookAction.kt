package snd.komelia.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgress
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressCompleteForBookAction(
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ) {
        transactionTemplate.execute {
            val media = mediaRepository.get(bookId)
            val progress = OfflineReadProgress(
                bookId,
                userId,
                media.pageCount,
                true
            )
            readProgressRepository.save(progress)
        }
        komgaEvents.emit(KomgaEvent.ReadProgressChanged(bookId, userId))
    }
}