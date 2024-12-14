package io.github.snd_r.komelia.ui.dialogs.series.editbulk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import snd.komga.client.common.patch
import snd.komga.client.common.patchLists
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest

private val logger = KotlinLogging.logger { }

class SeriesBulkEditDialogViewModel(
    val series: List<KomgaSeries>,
    val onDialogDismiss: () -> Unit,
    private val seriesClient: KomgaSeriesClient,
    private val referentialClient: KomgaReferentialClient,
    private val notifications: AppNotifications,
) {
    var status by mutableStateOf(
        distinctOrDefault(series, null) { it.metadata.status }
    )

    var statusLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.statusLock }
    )

    var language by mutableStateOf(
        distinctOrDefault(series, null) { it.metadata.language }
    )

    var languageLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.languageLock }
    )

    var readingDirection by mutableStateOf(
        distinctOrDefault(series, null) { it.metadata.readingDirection }
    )

    var readingDirectionLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.readingDirectionLock }
    )

    var publisher by mutableStateOf(
        distinctOrDefault(series, null) { it.metadata.publisher }
    )
    var publisherLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.publisherLock }
    )

    var ageRating by mutableStateOf(
        distinctOrDefault(series, null) { it.metadata.ageRating }
    )

    var ageRatingLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.ageRatingLock }
    )

    var genres by mutableStateOf<List<String>>(emptyList())
    var genresLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.genresLock }
    )

    var tags by mutableStateOf<List<String>>(emptyList())
    var tagsLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.tagsLock }
    )

    var sharingLabels by mutableStateOf<List<String>>(emptyList())
    var sharingLabelsLock by mutableStateOf(
        distinctOrDefault(series, false) { it.metadata.sharingLabelsLock }
    )

    var allTags: List<String> by mutableStateOf(emptyList())
        private set
    var allGenres: List<String> by mutableStateOf(emptyList())
        private set

    private val generalTab = GeneralTab(this)
    private val tagsTab = TagsTab(this)
    private val sharingTab = SharingTab(this)

    var currentTab by mutableStateOf<DialogTab>(generalTab)

    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            allTags = referentialClient.getTags()
            allGenres = referentialClient.getGenres()
        }.onFailure { logger.catching(it) }
    }

    fun tabs(): List<DialogTab> = listOf(generalTab, tagsTab, sharingTab)

    suspend fun saveChanges() {
        notifications.runCatchingToNotifications {
            series.forEach {
                val seriesMetadata = it.metadata
                val request = KomgaSeriesMetadataUpdateRequest(
                    status = patch(seriesMetadata.status, status),
                    statusLock = patch(seriesMetadata.statusLock, statusLock),
                    publisher = patch(seriesMetadata.publisher, publisher),
                    publisherLock = patch(seriesMetadata.publisherLock, publisherLock),
                    readingDirection = patch(seriesMetadata.readingDirection, readingDirection),
                    readingDirectionLock = patch(seriesMetadata.readingDirectionLock, readingDirectionLock),
                    ageRating = patch(seriesMetadata.ageRating, ageRating),
                    ageRatingLock = patch(seriesMetadata.ageRatingLock, ageRatingLock),
                    language = patch(seriesMetadata.language, language),
                    languageLock = patch(seriesMetadata.languageLock, languageLock),
                    genres = patchLists(seriesMetadata.genres, genres),
                    genresLock = patch(seriesMetadata.genresLock, genresLock),
                    tags = patchLists(seriesMetadata.tags, tags),
                    tagsLock = patch(seriesMetadata.tagsLock, tagsLock),
                    sharingLabels = patchLists(seriesMetadata.sharingLabels, sharingLabels),
                    sharingLabelsLock = patch(seriesMetadata.sharingLabelsLock, sharingLabelsLock),
                )

                seriesClient.updateSeries(it.id, request)
            }
            onDialogDismiss()
        }
    }

    private fun <T, R> distinctOrDefault(elements: List<T>, default: R, selector: (T) -> R): R {
        return if (elements.all { selector(it) == selector(elements[0]) }) selector(elements[0])
        else default
    }
}
