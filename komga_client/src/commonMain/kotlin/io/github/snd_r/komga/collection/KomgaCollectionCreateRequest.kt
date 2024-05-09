package io.github.snd_r.komga.collection

import io.github.snd_r.komga.series.KomgaSeriesId
import kotlinx.serialization.Serializable

@Serializable
data class KomgaCollectionCreateRequest(
    val name: String,
    val ordered: Boolean,
    val seriesIds: List<KomgaSeriesId>
)