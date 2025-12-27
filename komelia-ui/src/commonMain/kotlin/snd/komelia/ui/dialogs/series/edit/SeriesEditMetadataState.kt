package snd.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.common.patch
import snd.komga.client.common.patchLists
import snd.komga.client.series.KomgaAlternativeTitle
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest

class SeriesEditMetadataState(
    val series: KomgaSeries,
    val allTags: StateFlow<List<String>>,
    val allGenres: StateFlow<List<String>>,
    private val seriesApi: KomgaSeriesApi,
) {
    var title by mutableStateOf(series.metadata.title)
    var titleLock by mutableStateOf(series.metadata.titleLock)

    var titleSort by mutableStateOf(series.metadata.titleSort)
    var titleSortLock by mutableStateOf(series.metadata.titleSortLock)

    var status by mutableStateOf(series.metadata.status)
    var statusLock by mutableStateOf(series.metadata.statusLock)

    var summary by mutableStateOf(series.metadata.summary)
    var summaryLock by mutableStateOf(series.metadata.summaryLock)

    var readingDirection by mutableStateOf(series.metadata.readingDirection)
    var readingDirectionLock by mutableStateOf(series.metadata.readingDirectionLock)

    var publisher by mutableStateOf(series.metadata.publisher)
    var publisherLock by mutableStateOf(series.metadata.publisherLock)

    var ageRating by mutableStateOf(series.metadata.ageRating)
    var ageRatingLock by mutableStateOf(series.metadata.ageRatingLock)

    var language by mutableStateOf(series.metadata.language)
    var languageLock by mutableStateOf(series.metadata.languageLock)

    var genres by mutableStateOf(series.metadata.genres)
    var genresLock by mutableStateOf(series.metadata.genresLock)

    var tags by mutableStateOf(series.metadata.tags)
    var tagsLock by mutableStateOf(series.metadata.tagsLock)

    var totalBookCount by mutableStateOf(series.metadata.totalBookCount)
    var totalBookCountLock by mutableStateOf(series.metadata.totalBookCountLock)

    var sharingLabels by mutableStateOf(series.metadata.sharingLabels.toList())
    var sharingLabelsLock by mutableStateOf(series.metadata.sharingLabelsLock)

    var links = mutableStateListOf<KomgaWebLink>()
        .also { it.addAll(series.metadata.links) }
    var linksLock by mutableStateOf(series.metadata.linksLock)

    var alternateTitles = mutableStateListOf<KomgaAlternativeTitle>()
        .also { it.addAll(series.metadata.alternateTitles) }
    var alternateTitlesLock by mutableStateOf(series.metadata.alternateTitlesLock)


    suspend fun saveMetadataChanges() {
        val seriesMetadata = series.metadata
        val request = KomgaSeriesMetadataUpdateRequest(
            status = patch(seriesMetadata.status, status),
            statusLock = patch(seriesMetadata.statusLock, statusLock),
            title = patch(seriesMetadata.title, title),
            titleLock = patch(seriesMetadata.titleLock, titleLock),
            titleSort = patch(seriesMetadata.titleSort, titleSort),
            titleSortLock = patch(seriesMetadata.titleSortLock, titleSortLock),
            summary = patch(seriesMetadata.summary, summary),
            summaryLock = patch(seriesMetadata.summaryLock, summaryLock),
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
            totalBookCount = patch(seriesMetadata.totalBookCount, totalBookCount),
            totalBookCountLock = patch(seriesMetadata.totalBookCountLock, totalBookCountLock),
            sharingLabels = patchLists(seriesMetadata.sharingLabels, sharingLabels),
            sharingLabelsLock = patch(seriesMetadata.sharingLabelsLock, sharingLabelsLock),
            links = patchLists(seriesMetadata.links, links),
            linksLock = patch(seriesMetadata.linksLock, linksLock),
            alternateTitles = patchLists(seriesMetadata.alternateTitles, alternateTitles),
            alternateTitlesLock = patch(seriesMetadata.alternateTitlesLock, alternateTitlesLock),
        )

        seriesApi.update(series.id, request)
    }

}
