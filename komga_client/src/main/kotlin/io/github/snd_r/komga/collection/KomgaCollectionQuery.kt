package io.github.snd_r.komga.collection

import io.github.snd_r.komga.book.KomgaReadStatus
import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.series.KomgaSeriesStatus
import kotlinx.serialization.Serializable

@Serializable
data class KomgaCollectionQuery(
    val libraryIds: List<KomgaLibraryId>? =null,
    val status: List<KomgaSeriesStatus>? = null,
    val readStatus: List<KomgaReadStatus>? = null,
    val publishers: List<String>? = null,
    val languages: List<String>? = null,
    val genres: List<String>? = null,
    val tags: List<String>? = null,
    val ageRatings: List<String>? = null,
    val releaseYears: List<String>? = null,
    val authors: List<KomgaAuthor>? = null,
    val deleted: Boolean? = null,
    val complete: Boolean? = null,
)