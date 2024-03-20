package io.github.snd_r.komga.series

import io.github.snd_r.komga.common.KomgaReadingDirection
import io.github.snd_r.komga.common.KomgaWebLink
import io.github.snd_r.komga.common.PatchValue
import kotlinx.serialization.Serializable

@Serializable
data class KomgaSeriesMetadataUpdateRequest(
    val status: PatchValue<KomgaSeriesStatus> = PatchValue.Unset,
    val statusLock: PatchValue<Boolean> = PatchValue.Unset,
    val title: PatchValue<String> = PatchValue.Unset,
    val titleLock: PatchValue<Boolean> = PatchValue.Unset,
    val titleSort: PatchValue<String> = PatchValue.Unset,
    val titleSortLock: PatchValue<Boolean> = PatchValue.Unset,
    val summary: PatchValue<String> = PatchValue.Unset,
    val summaryLock: PatchValue<Boolean> = PatchValue.Unset,
    val publisher: PatchValue<String> = PatchValue.Unset,
    val publisherLock: PatchValue<Boolean> = PatchValue.Unset,
    val readingDirection: PatchValue<KomgaReadingDirection> = PatchValue.Unset,
    val readingDirectionLock: PatchValue<Boolean> = PatchValue.Unset,
    val ageRating: PatchValue<Int> = PatchValue.Unset,
    val ageRatingLock: PatchValue<Boolean> = PatchValue.Unset,
    val language: PatchValue<String> = PatchValue.Unset,
    val languageLock: PatchValue<Boolean> = PatchValue.Unset,
    val genres: PatchValue<List<String>> = PatchValue.Unset,
    val genresLock: PatchValue<Boolean> = PatchValue.Unset,
    val tags: PatchValue<List<String>> = PatchValue.Unset,
    val tagsLock: PatchValue<Boolean> = PatchValue.Unset,
    val totalBookCount: PatchValue<Int> = PatchValue.Unset,
    val totalBookCountLock: PatchValue<Boolean> = PatchValue.Unset,
    val sharingLabels: PatchValue<List<String>> = PatchValue.Unset,
    val sharingLabelsLock: PatchValue<Boolean> = PatchValue.Unset,
    val links: PatchValue<List<KomgaWebLink>> = PatchValue.Unset,
    val linksLock: PatchValue<Boolean> = PatchValue.Unset,
    val alternateTitles: PatchValue<List<KomgaAlternativeTitle>> = PatchValue.Unset,
    val alternateTitlesLock: PatchValue<Boolean> = PatchValue.Unset
)