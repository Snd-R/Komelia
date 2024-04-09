package io.github.snd_r.komga.readlist

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class KomgaReadListId(val value: String) {
    override fun toString() = value
}

@Serializable
data class KomgaReadList(
    val id: KomgaReadListId,
    val name: String,
    val summary: String,
    val ordered: Boolean,
    val bookIds: List<String>,
    val createdDate: Instant,
    val lastModifiedDate: Instant,
    val filtered: Boolean,
)
