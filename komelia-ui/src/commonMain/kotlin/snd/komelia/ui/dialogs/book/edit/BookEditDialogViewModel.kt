package snd.komelia.ui.dialogs.book.edit

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
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.dialogs.PosterEditState
import snd.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.BookThumbnail
import snd.komelia.ui.dialogs.PosterTab
import snd.komelia.ui.dialogs.tabs.DialogTab

class BookEditDialogViewModel(
    val book: KomeliaBook,
    val onDialogDismiss: () -> Unit,
    private val bookApi: KomgaBookApi,
    private val referentialApi: KomgaReferentialApi,
    private val notifications: AppNotifications,
    cardWidth: Flow<Dp>,
) {
    private val allTags = MutableStateFlow<List<String>>(emptyList())
    private val metadataState = BookEditMetadataState(book, allTags, bookApi)
    private val posterState = PosterEditState(cardWidth)

    private val generalTab = GeneralTab(metadataState)
    private val authorsTab = AuthorsTab(metadataState)
    private val tagsTab = TagsTab(metadataState)
    private val linksTab = LinksTab(metadataState)
    private val posterTab = PosterTab(posterState)
    var currentTab by mutableStateOf<DialogTab>(generalTab)

    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            posterState.thumbnails = bookApi.getThumbnails(book.id).map { BookThumbnail(it) }
            allTags.value = referentialApi.getTags()
        }
    }

    val tabs: List<DialogTab> = listOf(
        generalTab,
        authorsTab,
        tagsTab,
        linksTab,
        posterTab
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
            bookApi.uploadThumbnail(
                bookId = book.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails
            .firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> bookApi.selectBookThumbnail(book.id, thumb.id) }

        posterState.thumbnails
            .filter { it.markedDeleted }
            .forEach { thumb -> bookApi.deleteBookThumbnail(book.id, thumb.id) }
    }
}
