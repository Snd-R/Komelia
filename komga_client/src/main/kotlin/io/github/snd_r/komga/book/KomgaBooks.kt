@file:UseSerializers(ZonedDateTimeSerializer::class, LocalDateSerializer::class)

package io.github.snd_r.komga.book

import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.common.KomgaWebLink
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.serializers.LocalDateSerializer
import io.github.snd_r.komga.serializers.ZonedDateTimeSerializer
import io.github.snd_r.komga.series.KomgaSeriesId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.ZonedDateTime


@Serializable
@JvmInline
value class KomgaBookId(val value: String) {
    override fun toString() = value
}

@Serializable
data class KomgaBook(
    val id: KomgaBookId,
    val seriesId: KomgaSeriesId,
    val seriesTitle: String,
    val libraryId: KomgaLibraryId,
    val name: String,
    val url: String,
    val number: Int,
    val created: ZonedDateTime,
    val lastModified: ZonedDateTime,
    val fileLastModified: ZonedDateTime,
    val sizeBytes: Long,
    val size: String,
    val media: Media,
    val metadata: KomgaBookMetadata,
    val readProgress: ReadProgress?,
    val deleted: Boolean,
    val fileHash: String,
)

@Serializable
data class KomgaBookMetadata(
    val title: String,
    val summary: String,
    val number: String,
    val numberSort: Float,
    val releaseDate: LocalDate?,
    val authors: List<KomgaAuthor>,
    val tags: List<String>,
    val isbn: String,
    val links: List<KomgaWebLink>,

    val titleLock: Boolean,
    val summaryLock: Boolean,
    val numberLock: Boolean,
    val numberSortLock: Boolean,
    val releaseDateLock: Boolean,
    val authorsLock: Boolean,
    val tagsLock: Boolean,
    val isbnLock: Boolean,
    val linksLock: Boolean,

    val created: ZonedDateTime,
    val lastModified: ZonedDateTime,
)

@Serializable
data class KomgaBookThumbnail(
    val id: String,
    val bookId: String,
    val type: String,
    val selected: Boolean,
)

@Serializable
data class Media(
    val status: KomgaMediaStatus,
    val mediaType: String,
    val pagesCount: Int,
    val comment: String,
)

@Serializable
data class ReadProgress(
    val page: Int,
    val completed: Boolean,
    val readDate: ZonedDateTime,
    val created: ZonedDateTime,
    val lastModified: ZonedDateTime,
)


enum class KomgaMediaStatus {
    READY,
    UNKNOWN,
    ERROR,
    UNSUPPORTED,
    OUTDATED
}

enum class KomgaReadStatus {
    UNREAD,
    IN_PROGRESS,
    READ
}

enum class CopyMode {
    MOVE,
    COPY,
    HARDLINK
}


enum class KomgaBookSort(val query: List<String>) {
    TITLE_ASC(listOf()),
    TITLE_DESC(listOf())
}