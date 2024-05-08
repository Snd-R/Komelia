package io.github.snd_r.komga.collection

import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.series.KomgaSeriesId
import kotlinx.serialization.Serializable

@Serializable
data class KomgaCollectionUpdateRequest(
    val name: PatchValue<String> = PatchValue.Unset,
    val ordered: PatchValue<Boolean> = PatchValue.Unset,
    val seriesIds: PatchValue<Set<KomgaSeriesId>> = PatchValue.Unset,
)