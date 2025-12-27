package snd.komelia.ui.dialogs.oneshot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.dialogs.PosterEditState
import snd.komelia.ui.dialogs.PosterTab
import snd.komelia.ui.dialogs.book.edit.AuthorsTab
import snd.komelia.ui.dialogs.book.edit.BookEditMetadataState
import snd.komelia.ui.dialogs.book.edit.LinksTab
import snd.komelia.ui.dialogs.series.edit.SeriesEditMetadataState
import snd.komelia.ui.dialogs.series.edit.SharingTab
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komga.client.search.allOfBooks
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId

class OneshotEditDialogViewModel(
    private val series: KomgaSeries?,
    private val book: KomeliaBook?,
    private val seriesId: KomgaSeriesId,
    private val onDialogDismiss: () -> Unit,
    private val notifications: AppNotifications,
    private val bookApi: KomgaBookApi,
    private val seriesApi: KomgaSeriesApi,
    private val referentialApi: KomgaReferentialApi,
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
            val currentSeries = series ?: seriesApi.getOneSeries(seriesId)
            val currentBook = book
                ?: bookApi.getBookList(
                    allOfBooks { seriesId { isEqualTo(seriesId) } }
                ).content.first()

            val posterState = PosterEditState(cardWidth)
            val allTags = referentialApi.getTags()
            val allGenres = referentialApi.getGenres()

            loadState.value = Success(
                OneshotEditVmState(
                    seriesMetadataState = SeriesEditMetadataState(
                        series = currentSeries,
                        allTags = MutableStateFlow(allTags),
                        allGenres = MutableStateFlow(allGenres),
                        seriesApi = seriesApi
                    ),
                    bookMetadataState = BookEditMetadataState(currentBook, MutableStateFlow(allTags), bookApi),
                    posterState = posterState,
                )
            )

            posterState.thumbnails = bookApi.getThumbnails(currentBook.id)
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
        book: KomeliaBook,
        state: PosterEditState
    ) {
        state.userUploadedThumbnails.forEach { thumb ->
            bookApi.uploadThumbnail(
                bookId = book.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        state.thumbnails
            .firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> bookApi.selectBookThumbnail(book.id, thumb.id) }

        state.thumbnails
            .filter { it.markedDeleted }
            .forEach { thumb -> bookApi.deleteBookThumbnail(book.id, thumb.id) }
    }

    private fun getState(): OneshotEditVmState {
        return when (val state = loadState.value) {
            is Success -> state.value
            else -> error("successful state is required, current state $state")
        }
    }
}
