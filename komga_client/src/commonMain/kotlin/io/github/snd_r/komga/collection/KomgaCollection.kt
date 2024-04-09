package io.github.snd_r.komga.collection

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class KomgaCollectionId(val value: String) {
    override fun toString() = value
}

@Serializable
data class KomgaCollection(
    val id: KomgaCollectionId,
    val name: String,
    val ordered: Boolean,
    val seriesIds: List<String>,
    val createdDate: Instant,
    val lastModifiedDate: Instant,
    val filtered: Boolean,
)
