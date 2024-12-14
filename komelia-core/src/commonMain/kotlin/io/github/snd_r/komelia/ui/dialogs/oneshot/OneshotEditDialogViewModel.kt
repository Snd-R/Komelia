package io.github.snd_r.komelia.ui.dialogs.oneshot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.dialogs.PosterEditState
import io.github.snd_r.komelia.ui.dialogs.PosterTab
import io.github.snd_r.komelia.ui.dialogs.book.edit.AuthorsTab
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditMetadataState
import io.github.snd_r.komelia.ui.dialogs.book.edit.LinksTab
import io.github.snd_r.komelia.ui.dialogs.series.edit.SeriesEditMetadataState
import io.github.snd_r.komelia.ui.dialogs.series.edit.SharingTab
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId

class OneshotEditDialogViewModel(
    private val series: KomgaSeries?,
    private val book: KomgaBook?,
    private val seriesId: KomgaSeriesId,
    private val onDialogDismiss: () -> Unit,
    private val notifications: AppNotifications,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val referentialClient: KomgaReferentialClient,
    private val cardWidth: Flow<Dp>,
) {
    val loadState = MutableStateFlow<LoadState<OneshotEditVmState>>(Uninitialized)

    class OneshotEditVmState(
        val seriesMetadataState: SeriesEditMetadataState,
        val bookMetadataState: BookEditMetadataState,
        val posterState: PosterEditState,
    ) {
        private val generalTab = OneshotGeneralTab(seriesMetadataState, bookMetadataState)
        private val authorsTab = AuthorsTab(bookMetadataState)
        private val tagsTab = OneshotTagsTab(seriesMetadataState, bookMetadataState)
        private val linksTab = LinksTab(bookMetadataState)
        private val posterTab = PosterTab(posterState)
        private val sharingTab = SharingTab(seriesMetadataState)
        var currentTab by mutableStateOf<DialogTab>(generalTab)

        val tabs: List<DialogTab> = listOf(
            generalTab,
            authorsTab,
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
            val currentSeries = series ?: seriesClient.getOneSeries(seriesId)
            val currentBook = book
                ?: seriesClient.getAllBooksBySeries(seriesId).content.first()

            val posterState = PosterEditState(cardWidth)
            val allTags = referentialClient.getTags()
            val allGenres = referentialClient.getGenres()

            loadState.value = Success(
                OneshotEditVmState(
                    seriesMetadataState = SeriesEditMetadataState(currentSeries, allTags, allGenres, seriesClient),
                    bookMetadataState = BookEditMetadataState(currentBook, bookClient),
                    posterState = posterState,
                )
            )

            posterState.thumbnails = bookClient.getBookThumbnails(currentBook.id)
                .map { PosterEditState.KomgaThumbnail.BookThumbnail(it) }
        }.onFailure { loadState.value = Error(it) }
    }

    suspend fun saveChanges() {
        val state = getState()
        notifications.runCatchingToNotifications {
            state.seriesMetadataState.saveMetadataChanges()
            state.bookMetadataState.saveMetadataChanges()
            saveThumbnailChanges(state.bookMetadataState.book, state.posterState)
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges(
        book: KomgaBook,
        state: PosterEditState
    ) {
        state.userUploadedThumbnails.forEach { thumb ->
            bookClient.uploadBookThumbnail(
                bookId = book.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        state.thumbnails
            .firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> bookClient.selectBookThumbnail(book.id, thumb.id) }

        state.thumbnails
            .filter { it.markedDeleted }
            .forEach { thumb -> bookClient.deleteBookThumbnail(book.id, thumb.id) }
    }

    private fun getState(): OneshotEditVmState {
        return when (val state = loadState.value) {
            is Success -> state.value
            else -> error("successful state is required, current state $state")
        }
    }
}
