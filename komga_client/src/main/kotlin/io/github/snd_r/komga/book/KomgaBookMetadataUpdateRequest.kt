@file:UseSerializers(LocalDateSerializer::class)

package io.github.snd_r.komga.book

import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.common.KomgaWebLink
import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

@Serializable
data class KomgaBookMetadataUpdateRequest(
    val title: PatchValue<String> = PatchValue.Unset,
    val titleLock: PatchValue<Boolean> = PatchValue.Unset,
    val summary: PatchValue<String> = PatchValue.Unset,
    val summaryLock: PatchValue<Boolean> = PatchValue.Unset,
    val number: PatchValue<String> = PatchValue.Unset,
    val numberLock: PatchValue<Boolean> = PatchValue.Unset,
    val numberSort: PatchValue<Float> = PatchValue.Unset,
    val numberSortLock: PatchValue<Boolean> = PatchValue.Unset,
    val releaseDate: PatchValue<LocalDate> = PatchValue.Unset,
    val releaseDateLock: PatchValue<Boolean> = PatchValue.Unset,
    val authors: PatchValue<List<KomgaAuthor>> = PatchValue.Unset,
    val authorsLock: PatchValue<Boolean> = PatchValue.Unset,
    val tags: PatchValue<List<String>> = PatchValue.Unset,
    val tagsLock: PatchValue<Boolean> = PatchValue.Unset,
    val isbn: PatchValue<String> = PatchValue.Unset,
    val isbnLock: PatchValue<Boolean> = PatchValue.Unset,
    val links: PatchValue<List<KomgaWebLink>> = PatchValue.Unset,
    val linksLock: PatchValue<Boolean> = PatchValue.Unset,
)