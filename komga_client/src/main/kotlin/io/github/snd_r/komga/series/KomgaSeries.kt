@file:UseSerializers(ZonedDateTimeSerializer::class, LocalDateSerializer::class)

package io.github.snd_r.komga.series

import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.common.KomgaReadingDirection
import io.github.snd_r.komga.common.KomgaWebLink
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.serializers.KomgaReadingDirectionSerializer
import io.github.snd_r.komga.serializers.LocalDateSerializer
import io.github.snd_r.komga.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.ZonedDateTime

@Serializable
@JvmInline
value class KomgaSeriesId(val value: String) {
    override fun toString() = value
}

@Serializable
data class KomgaSeries(
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
    val booksMetadata: KomgaSeriesBookMetadata,
)

@Serializable
data class KomgaSeriesMetadata(
    val status: KomgaSeriesStatus,
    val statusLock: Boolean,
    val title: String,
    val alternateTitles: List<KomgaAlternativeTitle>,
    val alternateTitlesLock: Boolean,
    val titleLock: Boolean,
    val titleSort: String,
    val titleSortLock: Boolean,
    val summary: String,
    val summaryLock: Boolean,
    @Serializable(KomgaReadingDirectionSerializer::class)
    val readingDirection: KomgaReadingDirection?,
    val readingDirectionLock: Boolean,
    val publisher: String,
    val publisherLock: Boolean,
    val ageRating: Int?,
    val ageRatingLock: Boolean,
    val language: String,
    val languageLock: Boolean,
    val genres: List<String>,
    val genresLock: Boolean,
    val tags: List<String>,
    val tagsLock: Boolean,
    val totalBookCount: Int?,
    val totalBookCountLock: Boolean,
    val sharingLabels: List<String>,
    val sharingLabelsLock: Boolean,
    val links: List<KomgaWebLink>,
    val linksLock: Boolean,
)

@Serializable
data class KomgaSeriesBookMetadata(
    val authors: List<KomgaAuthor>,
    val tags: List<String>,
    val releaseDate: LocalDate?,
    val summary: String,
    val summaryNumber: String,

    val created: ZonedDateTime,
    val lastModified: ZonedDateTime,
)

@Serializable
data class KomgaAlternativeTitle(
    val label: String,
    val title: String,
)


@Serializable
data class KomgaSeriesThumbnail(
    val id: String,
    val seriesId: String,
    val type: String,
    val selected: Boolean,
)

@Serializable
enum class KomgaSeriesStatus {
    ENDED, ONGOING, ABANDONED, HIATUS
}

//enum class KomgaSeriesReadingDirection {
//    LEFT_TO_RIGHT,
//    RIGHT_TO_LEFT,
//    VERTICAL,
//    WEBTOON,
//}
enum class KomgaSeriesSort(val query: List<String>) {
    TITLE_ASC(listOf("metadata.titleSort", "asc")),
    TITLE_DESC(listOf("metadata.titleSort", "desc")),
}
