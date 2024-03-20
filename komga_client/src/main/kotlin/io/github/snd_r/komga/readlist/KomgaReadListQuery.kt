package io.github.snd_r.komga.readlist

import io.github.snd_r.komga.book.KomgaMediaStatus
import io.github.snd_r.komga.book.KomgaReadStatus
import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.library.KomgaLibraryId
import kotlinx.serialization.Serializable

@Serializable
data class KomgaReadListQuery(
    val libraryIds: List<KomgaLibraryId>? = null,
    val readStatus: List<KomgaReadStatus>? = null,
    val tags: List<String>? = null,
    val mediaStatus: List<KomgaMediaStatus>? = null,
    val deleted: Boolean? = null,
    val authors: List<KomgaAuthor>? = null,
)