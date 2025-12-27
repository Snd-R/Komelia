package snd.komelia.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.JsonPrimitive
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.book.model.toOfflineBookMetadata
import snd.komelia.offline.book.model.toOfflineThumbnailBook
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komelia.offline.media.model.EpubTocEntry
import snd.komelia.offline.media.model.MediaExtension
import snd.komelia.offline.media.model.MediaExtensionEpub
import snd.komelia.offline.media.model.OfflineBookPage
import snd.komelia.offline.media.model.OfflineMedia
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgress
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logError
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logInfo
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookPage
import snd.komga.client.book.MediaProfile
import snd.komga.client.book.ReadProgress
import snd.komga.client.book.WPLink
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

class BookKomgaImportAction(
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataRepository: OfflineBookMetadataRepository,
    private val thumbnailBookRepository: OfflineThumbnailBookRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val mediaRepository: OfflineMediaRepository,
    private val logJournalRepository: LogJournalRepository,
    private val bookClient: KomgaBookClient,
    private val taskEmitter: OfflineTaskEmitter,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun execute(
        book: KomgaBook,
        offlinePath: PlatformFile,
        userId: KomgaUserId?,
        localFileModifiedDate: Instant,
    ) {
        val event = try {
            transactionTemplate.execute {
                val existing = bookRepository.find(book.id)
                if (existing != null && existing.fileDownloadPath != offlinePath) {
                    taskEmitter.deleteBookFiles(existing.fileDownloadPath)
                }

                val offlineBook = doImport(
                    book = book,
                    offlinePath = offlinePath,
                    userId = userId,
                    fileModifiedDate = localFileModifiedDate
                )
                logJournalRepository.logInfo { "Book updated '${book.metadata.title}'" }

                if (existing != null) KomgaEvent.BookChanged(
                    bookId = offlineBook.id,
                    seriesId = offlineBook.seriesId,
                    libraryId = offlineBook.libraryId
                ) else KomgaEvent.BookAdded(
                    bookId = offlineBook.id,
                    seriesId = offlineBook.seriesId,
                    libraryId = offlineBook.libraryId
                )
            }
        } catch (e: Exception) {
            logJournalRepository.logError(e) { "Book update error '${book.metadata.title}'" }
            throw e
        }

        komgaEvents.emit(event)
    }

    private suspend fun doImport(
        book: KomgaBook,
        offlinePath: PlatformFile,
        userId: KomgaUserId?,
        fileModifiedDate: Instant,
    ): OfflineBook {
        val offlineBook = book.toOfflineBook(offlinePath, fileModifiedDate)
        val offlineBookMetadata = book.metadata.toOfflineBookMetadata(book.id)
        val offlineMedia = getOfflineMedia(book)
        val offlineBookThumbnail = bookClient.getThumbnails(book.id)
            .firstOrNull { it.selected }
            ?.let { thumb ->
                val thumbnailBytes = bookClient.getThumbnail(thumb.bookId, thumb.id)
                thumb.toOfflineThumbnailBook(thumbnailBytes)
            }
        val offlineReadProgress = userId?.let { getReadProgress(book, it) }
        bookRepository.save(offlineBook)
        bookMetadataRepository.save(offlineBookMetadata)
        mediaRepository.save(offlineMedia)
        offlineBookThumbnail?.let { thumbnailBookRepository.save(it) }
        offlineReadProgress?.let { readProgressRepository.save(it) }

        taskEmitter.aggregateSeriesMetadata(book.seriesId)
        return offlineBook
    }

    private suspend fun getOfflineMedia(book: KomgaBook): OfflineMedia {
        val offlineBookPages = bookClient.getBookPages(book.id).map { it.toOfflineBookPage(book.id) }
        val mediaExtension = getMediaExtension(book)
        return OfflineMedia(
            bookId = book.id,
            status = book.media.status,
            mediaType = book.media.mediaType,
            mediaProfile = book.media.mediaProfile,
            comment = book.media.comment,
            epubDivinaCompatible = book.media.epubDivinaCompatible,
            epubIsKepub = book.media.epubIsKepub,
            pageCount = book.media.pagesCount,
            pages = offlineBookPages,
            extension = mediaExtension
        )
    }

    private suspend fun getMediaExtension(book: KomgaBook): MediaExtension? {
        if (book.media.mediaProfile != MediaProfile.EPUB) return null
        val positions = bookClient.getReadiumPositions(book.id).positions
        val manifest = bookClient.getWebPubManifest(book.id)
        val isFixedLayout = manifest.metadata.rendition["layout"]?.let { rendition ->
            when (rendition) {
                is JsonPrimitive -> rendition.content == "fixed"
                else -> false
            }
        } ?: false


        return MediaExtensionEpub(
            toc = manifest.toc.map { it.toTocEntry() },
            landmarks = manifest.landmarks.map { it.toTocEntry() },
            pageList = manifest.pageList.map { it.toTocEntry() },
            isFixedLayout = isFixedLayout,
            positions = positions,
            manifest = manifest
        )
    }

    private suspend fun getReadProgress(book: KomgaBook, userId: KomgaUserId): OfflineReadProgress? {
        return when (book.media.mediaProfile) {
            MediaProfile.DIVINA, MediaProfile.PDF -> book.readProgress?.toOfflineReadProgress(book.id, userId)
            MediaProfile.EPUB -> getEpubReadProgress(book, userId)
            null -> null
        }
    }

    private suspend fun getEpubReadProgress(book: KomgaBook, userId: KomgaUserId): OfflineReadProgress? {
        val readProgress = book.readProgress ?: return null
        val locator = bookClient.getReadiumProgression(book.id)?.locator ?: return null
        return OfflineReadProgress(
            bookId = book.id,
            userId = userId,
            page = readProgress.page,
            completed = readProgress.completed,
            readDate = readProgress.readDate,
            deviceId = readProgress.deviceId,
            deviceName = readProgress.deviceName,
            locator = locator,
            createdDate = readProgress.created,
            lastModifiedDate = readProgress.lastModified
        )
    }

    private fun WPLink.toTocEntry(): EpubTocEntry {
        return EpubTocEntry(
            title = this.title ?: "",
            href = this.href,
            children = this.children.map { it.toTocEntry() }
        )
    }

    private fun ReadProgress.toOfflineReadProgress(bookId: KomgaBookId, userId: KomgaUserId) =
        OfflineReadProgress(
            bookId = bookId,
            userId = userId,
            page = this.page,
            completed = this.completed,
            readDate = this.readDate,
            deviceId = this.deviceId,
            deviceName = this.deviceName,
            locator = null,
            createdDate = this.created,
            lastModifiedDate = this.lastModified
        )

    private fun KomgaBookPage.toOfflineBookPage(id: KomgaBookId): OfflineBookPage {
        return OfflineBookPage(
            bookId = id,
            fileName = this.fileName,
            mediaType = this.mediaType,
            width = this.width,
            height = this.height,
            fileSize = this.sizeBytes,
        )
    }

    private fun KomgaBook.toOfflineBook(
        downloadPath: PlatformFile,
        localFileModifiedDate: Instant,
    ): OfflineBook =
        OfflineBook(
            id = this.id,
            seriesId = this.seriesId,
            libraryId = this.libraryId,
            name = this.name,
            number = this.number,
            size = this.size,
            deleted = this.deleted,
            fileHash = this.fileHash,
            oneshot = this.oneshot,
            url = this.url,
            created = this.created,
            lastModified = this.lastModified,
            sizeBytes = this.sizeBytes,
            remoteFileLastModified = this.fileLastModified,
            localFileLastModified = localFileModifiedDate,
            remoteUnavailable = false,
            fileDownloadPath = downloadPath,
        )
}