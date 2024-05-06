package io.github.snd_r.komga.readlist

import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.serialization.Serializable

@Serializable
data class KomgaReadListCreateRequest(
    val name: String,
    val summary: String,
    val ordered: Boolean,
    val bookIds: List<KomgaBookId>
)