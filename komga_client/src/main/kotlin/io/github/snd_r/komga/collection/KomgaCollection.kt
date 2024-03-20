package io.github.snd_r.komga.collection

import io.github.snd_r.komga.serializers.InstantIsoStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

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
    @Serializable(InstantIsoStringSerializer::class)
    val createdDate: Instant,
    @Serializable(InstantIsoStringSerializer::class)
    val lastModifiedDate: Instant,
    val filtered: Boolean,
)
