package io.github.snd_r.komga.book

import io.github.snd_r.komga.library.KomgaLibraryId
import java.time.LocalDate

data class KomgaBookQuery(
    val searchTerm: String? = null,
    val libraryIds: List<KomgaLibraryId>? = null,
    val mediaStatus: List<KomgaMediaStatus>? = null,
    val readStatus: List<KomgaReadStatus>? = null,
    val releasedAfter: LocalDate? = null,
    val tags: List<String>? = null,
)