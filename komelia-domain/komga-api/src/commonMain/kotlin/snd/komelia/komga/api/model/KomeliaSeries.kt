package snd.komelia.komga.api.model

import kotlinx.serialization.Serializable
import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesBookMetadata
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadata
import kotlin.time.Instant

@Serializable
data class KomeliaSeries(
    val id: KomgaSeriesId,
    val libraryId: KomgaLibraryId,
    val name: String,
    val url: String,
    val booksCount: Int,
    val booksReadCount: Int,
    val booksUnreadCount: Int,
    val booksInProgressCount: Int,
    val metadata: KomgaSeriesMetadata,
    val deleted: Boolean,
    val oneshot: Boolean,
    val booksMetadata: KomgaSeriesBookMetadata,
    val created: Instant,
    val lastModified: Instant,
    val fileLastModified: Instant,

    val mangaBakaMetadata: KomfMangaBakaSeries?,
) {
    constructor(series: KomgaSeries, mangaBakaMetadata: KomfMangaBakaSeries?) :
            this(
                id = series.id,
                libraryId = series.libraryId,
                name = series.name,
                url = series.url,
                booksCount = series.booksCount,
                booksReadCount = series.booksReadCount,
                booksUnreadCount = series.booksUnreadCount,
                booksInProgressCount = series.booksInProgressCount,
                metadata = series.metadata,
                deleted = series.deleted,
                oneshot = series.oneshot,
                booksMetadata = series.booksMetadata,
                created = series.created,
                lastModified = series.lastModified,
                fileLastModified = series.fileLastModified,

                mangaBakaMetadata = mangaBakaMetadata,
            )
}
