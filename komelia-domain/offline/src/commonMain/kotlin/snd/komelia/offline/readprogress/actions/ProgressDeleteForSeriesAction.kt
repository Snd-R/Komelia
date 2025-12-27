package snd.komelia.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressDeleteForSeriesAction(
    private val readProgressRepository: OfflineReadProgressRepository,
    private val bookRepository: OfflineBookRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {
    suspend fun run(seriesId: KomgaSeriesId, userId: KomgaUserId) {
        val progresses = transactionTemplate.execute {
            val bookIds = bookRepository.findAllIdsBySeriesId(seriesId)
            val progresses = readProgressRepository.findAllByBookIdsAndUserId(bookIds, userId)
            readProgressRepository.deleteByBookIdsAndUserId(bookIds, userId)
            progresses
        }

        progresses.forEach { komgaEvents.emit(KomgaEvent.ReadProgressDeleted(it.bookId, it.userId)) }
        komgaEvents.emit(KomgaEvent.ReadProgressSeriesDeleted(seriesId, userId))
    }
}