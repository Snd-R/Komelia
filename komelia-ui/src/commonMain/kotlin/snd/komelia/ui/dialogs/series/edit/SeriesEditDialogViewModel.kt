package snd.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.ui.dialogs.PosterEditState
import snd.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.SeriesThumbnail
import snd.komelia.ui.dialogs.PosterTab
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komga.client.series.KomgaSeries

private val logger = KotlinLogging.logger { }

class SeriesEditDialogViewModel(
    val series: KomgaSeries,
    val onDialogDismiss: () -> Unit,
    private val seriesApi: KomgaSeriesApi,
    private val referentialApi: KomgaReferentialApi,
    private val notifications: AppNotifications,
    private val cardWidth: Flow<Dp>,
) {
    private val allTags = MutableStateFlow<List<String>>(emptyList())
    private val allGenres = MutableStateFlow<List<String>>(emptyList())
    private val posterState = PosterEditState(cardWidth)
    private val metadataState = SeriesEditMetadataState(series, allTags, allGenres, seriesApi)

    private val generalTab = GeneralTab(metadataState)
    private val alternativeTitlesTab = AlternativeTitlesTab(metadataState)
    private val tagsTab = TagsTab(metadataState)
    private val linksTab = LinksTab(metadataState)
    private val posterTab = PosterTab(posterState)
    private val sharingTab = SharingTab(metadataState)

    var currentTab by mutableStateOf<DialogTab>(generalTab)

    val tabs: List<DialogTab> = listOf(
        generalTab,
        alternativeTitlesTab,
        tagsTab,
        linksTab,
        posterTab,
        sharingTab
    )

    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            posterState.thumbnails = seriesApi.getThumbnails(series.id).map { SeriesThumbnail(it) }
            allTags.value = referentialApi.getTags()
            allGenres.value = referentialApi.getGenres()

        }.onFailure { logger.catching(it) }
    }

    suspend fun saveChanges() {
        notifications.runCatchingToNotifications {
            metadataState.saveMetadataChanges()
            saveThumbnailChanges()
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges() {
        posterState.userUploadedThumbnails.forEach { thumb ->
            seriesApi.uploadThumbnail(
                seriesId = series.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails.firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> seriesApi.selectThumbnail(series.id, thumb.id) }
        posterState.thumbnails.filter { it.markedDeleted }
            .forEach { thumb -> seriesApi.deleteThumbnail(series.id, thumb.id) }
    }
}
