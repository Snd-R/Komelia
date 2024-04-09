package io.github.snd_r.komga.series

import io.github.snd_r.komga.book.KomgaReadStatus
import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.library.KomgaLibraryId

data class KomgaSeriesQuery(
    val searchTerm: String? = null,
    val searchRegex: SearchRegex? = null,
    val libraryIds: List<KomgaLibraryId>? = null,
    val collectionIds: List<KomgaCollectionId>? = null,
    val status: List<KomgaSeriesStatus>? = null,
    val readStatus: List<KomgaReadStatus>? = null,
    val publishers: List<String>? = null,
    val languages: List<String>? = null,
    val genres: List<String>? = null,
    val tags: List<String>? = null,
    val ageRatings: List<String>? = null,
    val releaseYears: List<String>? = null,
    val sharingLabels: List<String>? = null,
    val authors: List<KomgaAuthor>? = null,
    val deleted: Boolean? = null,
    val complete: Boolean? = null,
    val oneshot: Boolean? = null,
)

data class SearchRegex(
    val regex: String,
    val searchField: SearchField,
)

enum class SearchField {
//    NAME,
    TITLE,
    TITLE_SORT
}
