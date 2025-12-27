package snd.komelia.offline.series.model

import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.series.KomgaAlternativeTitle
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesStatus

data class OfflineSeriesMetadata(
    val seriesId: KomgaSeriesId,
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
