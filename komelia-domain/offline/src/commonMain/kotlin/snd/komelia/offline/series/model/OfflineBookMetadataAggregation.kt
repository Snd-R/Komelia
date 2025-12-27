package snd.komelia.offline.series.model

import kotlinx.datetime.LocalDate
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Clock
import kotlin.time.Instant

data class OfflineBookMetadataAggregation(
    val seriesId: KomgaSeriesId,
    val releaseDate: LocalDate? = null,
    val summary: String = "",
    val summaryNumber: String = "",
    val authors: List<KomgaAuthor> = emptyList(),
    val tags: Set<String> = emptySet(),
    val createdDate: Instant = Clock.System.now(),
    val lastModifiedDate: Instant = createdDate,
)