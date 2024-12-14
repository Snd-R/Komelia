package io.github.snd_r.komelia.ui.dialogs.series.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.dialogs.PosterEditState
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.SeriesThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterTab
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient

class SeriesEditDialogViewModel(
    val series: KomgaSeries,
    val onDialogDismiss: () -> Unit,
    private val seriesClient: KomgaSeriesClient,
    private val referentialClient: KomgaReferentialClient,
    private val notifications: AppNotifications,
    private val cardWidth: Flow<Dp>,
) {
    val loadState = MutableStateFlow<LoadState<SeriesEditVmState>>(Uninitialized)

    class SeriesEditVmState(
        val posterState: PosterEditState,
        val metadataState: SeriesEditMetadataState,
    ) {
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
    }

    suspend fun initialize() {
        if (loadState.value != Uninitialized) return
        loadState.value = Loading

        notifications.runCatchingToNotifications {
            val posterState = PosterEditState(cardWidth)
            posterState.thumbnails = seriesClient.getSeriesThumbnails(series.id).map { SeriesThumbnail(it) }
            val allTags = referentialClient.getTags()
            val allGenres = referentialClient.getGenres()
            val metadataState = SeriesEditMetadataState(
                series = series,
                allTags = allTags,
                allGenres = allGenres,
                seriesClient = seriesClient
            )

            loadState.value = Success(
                SeriesEditVmState(
                    posterState = posterState,
                    metadataState = metadataState
                )
            )
        }
    }

    suspend fun saveChanges() {
        val state = getState()
        notifications.runCatchingToNotifications {
            state.metadataState.saveMetadataChanges()
            saveThumbnailChanges()
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges() {
        val posterState = getState().posterState
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

    private fun getState(): SeriesEditVmState {
        return when (val state = loadState.value) {
            is Success -> state.value
            else -> error("successful state is required, current state $state")
        }
    }
}
