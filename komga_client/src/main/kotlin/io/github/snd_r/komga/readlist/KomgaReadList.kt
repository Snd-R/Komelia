package io.github.snd_r.komga.readlist

import io.github.snd_r.komga.serializers.InstantIsoStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

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
    @Serializable(InstantIsoStringSerializer::class)
    val createdDate: Instant,
    @Serializable(InstantIsoStringSerializer::class)
    val lastModifiedDate: Instant,
    val filtered: Boolean,
)
