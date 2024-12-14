package io.github.snd_r.komelia.ui.dialogs.book.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.PosterEditState
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.BookThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterTab
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.referential.KomgaReferentialClient

class BookEditDialogViewModel(
    val book: KomgaBook,
    val onDialogDismiss: () -> Unit,
    private val bookClient: KomgaBookClient,
    private val referentialClient: KomgaReferentialClient,
    private val notifications: AppNotifications,
    cardWidth: Flow<Dp>,
) {
    private val allTags = MutableStateFlow<List<String>>(emptyList())
    private val metadataState = BookEditMetadataState(book, allTags, bookClient)
    private val posterState = PosterEditState(cardWidth)

    private val generalTab = GeneralTab(metadataState)
    private val authorsTab = AuthorsTab(metadataState)
    private val tagsTab = TagsTab(metadataState)
    private val linksTab = LinksTab(metadataState)
    private val posterTab = PosterTab(posterState)
    var currentTab by mutableStateOf<DialogTab>(generalTab)

    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            posterState.thumbnails = bookClient.getBookThumbnails(book.id).map { BookThumbnail(it) }
            allTags.value = referentialClient.getTags()
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
            bookClient.uploadBookThumbnail(
                bookId = book.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails
            .firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> bookClient.selectBookThumbnail(book.id, thumb.id) }

        posterState.thumbnails
            .filter { it.markedDeleted }
            .forEach { thumb -> bookClient.deleteBookThumbnail(book.id, thumb.id) }
    }
}
