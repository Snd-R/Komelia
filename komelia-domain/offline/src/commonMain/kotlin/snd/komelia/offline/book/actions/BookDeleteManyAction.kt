package snd.komelia.offline.book.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookDeleted

class BookDeleteManyAction(
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataRepository: OfflineBookMetadataRepository,
    private val thumbnailBookRepository: OfflineThumbnailBookRepository,
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val taskEmitter: OfflineTaskEmitter,
) : OfflineAction {

    suspend fun execute(books: List<OfflineBook>) {
        transactionTemplate.execute {
            val bookIds = books.map { it.id }

            readProgressRepository.deleteByBookIds(bookIds)
            mediaRepository.delete(bookIds)
            thumbnailBookRepository.deleteByBookIds(bookIds)
            bookMetadataRepository.delete(bookIds)
            bookRepository.delete(bookIds)
        }

        books.forEach { book ->
            komgaEvents.emit(BookDeleted(book.id, book.seriesId, book.libraryId))
            taskEmitter.deleteBookFiles(book.fileDownloadPath)
        }
    }
}