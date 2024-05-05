package io.github.snd_r.komelia.ui.dialogs.collectionedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.getBytes
import io.github.snd_r.komelia.ui.dialogs.PosterEditState
import io.github.snd_r.komelia.ui.dialogs.PosterTab
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komga.collection.KomgaCollection
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.collection.KomgaCollectionUpdateRequest
import io.github.snd_r.komga.common.patch
import kotlinx.coroutines.flow.Flow

class CollectionEditDialogViewModel(
    val collection: KomgaCollection,
    val onDialogDismiss: () -> Unit,
    private val collectionClient: KomgaCollectionClient,
    private val notifications: AppNotifications,
    cardWidth: Flow<Dp>,
) {
    var name by mutableStateOf(collection.name)
    var manualOrdering by mutableStateOf(collection.ordered)
    private val posterState = PosterEditState(cardWidth)

    private val generalTab = GeneralTab(this)
    private val posterTab = PosterTab(posterState)
    var currentTab by mutableStateOf<DialogTab>(generalTab)


    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            posterState.thumbnails = collectionClient.getCollectionThumbnails(collection.id).map {
                PosterEditState.KomgaThumbnail.CollectionThumbnail(it)
            }
        }
    }

    fun tabs(): List<DialogTab> = listOf(generalTab, posterTab)

    suspend fun saveChanges() {
        notifications.runCatchingToNotifications {
            val request = KomgaCollectionUpdateRequest(
                name = patch(collection.name, name),
                ordered = patch(collection.ordered, manualOrdering),
            )
            collectionClient.updateOne(collection.id, request)

            saveThumbnailChanges()
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges() {
        posterState.userUploadedThumbnails.forEach { thumb ->
            collectionClient.uploadCollectionThumbnail(
                collectionId = collection.id,
                file = thumb.file.getBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails.firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> collectionClient.selectCollectionThumbnail(collection.id, thumb.id) }
        posterState.thumbnails.filter { it.markedDeleted }
            .forEach { thumb -> collectionClient.deleteCollectionThumbnail(collection.id, thumb.id) }
    }
}