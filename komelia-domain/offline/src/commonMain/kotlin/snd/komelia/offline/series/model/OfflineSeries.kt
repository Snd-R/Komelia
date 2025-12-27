package snd.komelia.offline.series.model

import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Instant

data class OfflineSeries(
    val id: KomgaSeriesId,
    val libraryId: KomgaLibraryId,
    val name: String,
    val url: String,

    val oneshot: Boolean,

    val bookCount: Int,
    val deleted: Boolean,
    val created: Instant,
    val lastModified: Instant,
    val fileLastModified: Instant,
)
