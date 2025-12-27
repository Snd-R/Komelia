package snd.komelia.offline.mediacontainer

import io.github.oshai.kotlinlogging.KotlinLogging
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.media.model.OfflineMedia
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.MediaProfile

private val logger = KotlinLogging.logger { }

class BookContentExtractors(
    divinaExtractors: List<DivinaExtractor>,
    private val epubExtractor: EpubExtractor
) {

    val divinaExtractors = divinaExtractors
        .flatMap { e -> e.mediaTypes().map { it to e } }
        .toMap()

    fun getBookPage(
        book: OfflineBook,
        media: OfflineMedia,
        page: Int
    ): ByteArray {

        if (media.status != KomgaMediaStatus.READY) {
            logger.warn { "Book media is not ready, cannot get pages" }
            throw IllegalStateException("Media is not ready")
        }

        if (page > media.pageCount || page <= 0) {
            logger.error { "Page number #$page is out of bounds. Book has ${media.pageCount} pages" }
            throw IndexOutOfBoundsException("Page $page does not exist")
        }

        return when (media.mediaProfile) {
            MediaProfile.DIVINA -> getDivinaExtractorOrThrow(media)
                .getEntryBytes(book.fileDownloadPath, media.pages[page - 1].fileName)

            MediaProfile.EPUB -> {
                if (media.epubDivinaCompatible) {
                    epubExtractor.getEntryBytes(book.fileDownloadPath, media.pages[page - 1].fileName)
                } else throw IllegalStateException("Epub profile does not support getting page content")
            }

            MediaProfile.PDF -> {
                TODO()
            }

            null -> throw IllegalStateException("Media is not ready")
        }
    }

    fun getFileContent(book: OfflineBook, media: OfflineMedia, filename: String): ByteArray {
        return when (media.mediaProfile) {
            MediaProfile.DIVINA -> getDivinaExtractorOrThrow(media)
                .getEntryBytes(book.fileDownloadPath, filename)

            MediaProfile.EPUB -> epubExtractor
                .getEntryBytes(book.fileDownloadPath, filename)

            MediaProfile.PDF, null -> throw IllegalStateException("Extractor does not support extraction of files")
        }
    }

    private fun getDivinaExtractorOrThrow(media: OfflineMedia): DivinaExtractor {
        val type = checkNotNull(media.mediaType) { "Book media type is null" }
        return checkNotNull(divinaExtractors[type]) { "Unsupported book file format $type" }
    }
}