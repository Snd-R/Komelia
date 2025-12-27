package snd.komelia.offline.book.actions

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komga.client.book.KomgaBookId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookChanged
import snd.komga.client.sse.KomgaEvent.BookDeleted

private val logger = KotlinLogging.logger { }

class BookDeleteAction(
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataRepository: OfflineBookMetadataRepository,
    private val thumbnailBookRepository: OfflineThumbnailBookRepository,
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val taskEmitter: OfflineTaskEmitter,
    private val isOffline: StateFlow<Boolean>
) : OfflineAction {

    suspend fun execute(bookId: KomgaBookId) {
        val book = transactionTemplate.execute {
            val book = bookRepository.get(bookId)
            readProgressRepository.deleteAllBy(bookId)

            mediaRepository.delete(bookId)
            thumbnailBookRepository.deleteAllBy(bookId)
            bookMetadataRepository.delete(bookId)

            bookRepository.delete(bookId)

            book
        }

        if (isOffline.value) {
            komgaEvents.emit(BookDeleted(book.id, book.seriesId, book.libraryId))
        } else {
            komgaEvents.emit(BookChanged(book.id, book.seriesId, book.libraryId))
        }
        taskEmitter.deleteBookFiles(book.fileDownloadPath)
    }
}