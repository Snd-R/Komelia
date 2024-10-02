package io.github.snd_r.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.PosterEditState
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.SeriesThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterTab
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import kotlinx.coroutines.flow.Flow
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient

class SeriesEditDialogViewModel(
    val series: KomgaSeries,
    val onDialogDismiss: () -> Unit,
    private val seriesClient: KomgaSeriesClient,
    private val notifications: AppNotifications,
    cardWidth: Flow<Dp>,
) {
    private val posterState = PosterEditState(cardWidth)
    private val metadataState = SeriesEditMetadataState(series, seriesClient)

    private val generalTab = GeneralTab(metadataState)
    private val alternativeTitlesTab = AlternativeTitlesTab(metadataState)
    private val tagsTab = TagsTab(metadataState)
    private val linksTab = LinksTab(metadataState)
    private val posterTab = PosterTab(posterState)
    private val sharingTab = SharingTab(metadataState)

    var currentTab by mutableStateOf<DialogTab>(generalTab)

    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            posterState.thumbnails =
                seriesClient.getSeriesThumbnails(series.id).map { SeriesThumbnail(it) }
        }
    }

    val tabs: List<DialogTab> = listOf(
        generalTab,
        alternativeTitlesTab,
        tagsTab,
        linksTab,
        posterTab,
        sharingTab
    )


    suspend fun saveChanges() {
        notifications.runCatchingToNotifications {
            metadataState.saveMetadataChanges()
            saveThumbnailChanges()
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges() {
        posterState.userUploadedThumbnails.forEach { thumb ->
            seriesClient.uploadSeriesThumbnail(
                seriesId = series.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails.firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> seriesClient.selectSeriesThumbnail(series.id, thumb.id) }
        posterState.thumbnails.filter { it.markedDeleted }
            .forEach { thumb -> seriesClient.deleteSeriesThumbnail(series.id, thumb.id) }
    }
}
