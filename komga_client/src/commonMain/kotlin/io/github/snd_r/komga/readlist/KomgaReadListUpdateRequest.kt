package io.github.snd_r.komga.readlist

import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.common.PatchValue
import kotlinx.serialization.Serializable

@Serializable
data class KomgaReadListUpdateRequest(
    val name: PatchValue<String> = PatchValue.Unset,
    val summary: PatchValue<String> = PatchValue.Unset,
    val ordered: PatchValue<Boolean> = PatchValue.Unset,
    val bookIds: PatchValue<List<KomgaBookId>> = PatchValue.Unset,
)