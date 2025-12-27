package snd.komelia.offline.book.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komga.client.book.KomgaBookId

class BookMarkRemoteDeletedAction(
    private val bookRepository: OfflineBookRepository,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(bookId: KomgaBookId): OfflineBook {
        return transactionTemplate.execute {
            val book = bookRepository.get(bookId).markRemoteUnavailable()
            bookRepository.save(book)
            book
        }
    }
}