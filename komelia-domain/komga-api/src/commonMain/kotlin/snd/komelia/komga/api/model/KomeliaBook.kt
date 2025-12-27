package snd.komelia.komga.api.model

import kotlinx.serialization.Serializable
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadata
import snd.komga.client.book.Media
import snd.komga.client.book.ReadProgress
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Instant

@Serializable
data class KomeliaBook(
    // komga book fields
    val id: KomgaBookId,
    val seriesId: KomgaSeriesId,
    val seriesTitle: String,
    val libraryId: KomgaLibraryId,
    val name: String,
    val url: String,
    val number: Int,
    val created: Instant,
    val lastModified: Instant,
    val fileLastModified: Instant,
    val sizeBytes: Long,
    val size: String,
    val media: Media,
    val metadata: KomgaBookMetadata,
    val readProgress: ReadProgress?,
    val deleted: Boolean,
    val fileHash: String,
    val oneshot: Boolean,

    // custom fields
    val downloaded: Boolean,
    val localFileLastModified: Instant?,
    val remoteFileUnavailable: Boolean
) {
    val isLocalFileOutdated = localFileLastModified?.let { it.epochSeconds != fileLastModified.epochSeconds } ?: false

    constructor(
        book: KomgaBook,
        downloaded: Boolean = false,
        localFileLastModified: Instant?,
        remoteFileUnavailable: Boolean
    ) : this(
        id = book.id,
        seriesId = book.seriesId,
        seriesTitle = book.seriesTitle,
        libraryId = book.libraryId,
        name = book.name,
        url = book.url,
        number = book.number,
        created = book.created,
        lastModified = book.lastModified,
        fileLastModified = book.fileLastModified,
        sizeBytes = book.sizeBytes,
        size = book.size,
        media = book.media,
        metadata = book.metadata,
        readProgress = book.readProgress,
        deleted = book.deleted,
        fileHash = book.fileHash,
        oneshot = book.oneshot,

        downloaded = downloaded,
        localFileLastModified = localFileLastModified,
        remoteFileUnavailable = remoteFileUnavailable
    )
}
