package io.github.snd_r.komga.common

import io.github.snd_r.komga.serializers.UpdateValueSerializer
import kotlinx.serialization.Serializable

@Serializable(UpdateValueSerializer::class)
sealed class PatchValue<out T> {
    data object Unset : PatchValue<Nothing>()
    data object None : PatchValue<Nothing>()
    class Some<T>(val value: T) : PatchValue<T>()

}

fun <T> patch(original: T?, patch: T?): PatchValue<T> {
    return when {
        original == patch -> PatchValue.Unset
        patch == null -> PatchValue.None
        else -> PatchValue.Some(patch)
    }
}

fun <T> patchLists(original: List<T>, patch: List<T>): PatchValue<List<T>> {
    return when {
        original is ArrayList -> patch(original, patch)
        patch is ArrayList && patch == original -> PatchValue.Unset
        else -> PatchValue.Some(patch)
    }
}
