package snd.komelia.ui.dialogs.collectionedit

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.ui.LoadState
import snd.komelia.ui.dialogs.PosterEditState
import snd.komelia.ui.dialogs.PosterTab
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.patch

class CollectionEditDialogViewModel(
    val collection: KomgaCollection,
    val onDialogDismiss: () -> Unit,
    private val collectionApi: KomgaCollectionsApi,
    private val notifications: AppNotifications,
    cardWidth: Flow<Dp>,
) {
    private var state by mutableStateOf<LoadState<Unit>>(LoadState.Uninitialized)
    var name by mutableStateOf(collection.name)
    private var collections by mutableStateOf<List<KomgaCollection>>(emptyList())
    val nameValidationError by derivedStateOf {
        when {
            name.isBlank() -> "Required"
            name != collection.name && collections.any { it.name == this.name } -> "A collection with this name already exists"
            else -> null
        }
    }

    var manualOrdering by mutableStateOf(collection.ordered)
    private val posterState = PosterEditState(cardWidth)

    private val generalTab = GeneralTab(this)
    private val posterTab = PosterTab(posterState)
    var currentTab by mutableStateOf<DialogTab>(generalTab)

    suspend fun initialize() {
        if (state != LoadState.Uninitialized) return

        notifications.runCatchingToNotifications {
            this.collections = collectionApi.getAll(pageRequest = KomgaPageRequest(unpaged = true)).content

            posterState.thumbnails = collectionApi.getThumbnails(collection.id).map {
                PosterEditState.KomgaThumbnail.CollectionThumbnail(it)
            }
        }
        state = LoadState.Success(Unit)
    }

    fun tabs(): List<DialogTab> = listOf(generalTab, posterTab)

    fun canSave() = state != LoadState.Uninitialized && nameValidationError == null

    suspend fun saveChanges() {
        if (!canSave()) return

        notifications.runCatchingToNotifications {
            val request = KomgaCollectionUpdateRequest(
                name = patch(collection.name, name),
                ordered = patch(collection.ordered, manualOrdering),
            )
            collectionApi.updateOne(collection.id, request)

            saveThumbnailChanges()
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges() {
        posterState.userUploadedThumbnails.forEach { thumb ->
            collectionApi.uploadThumbnail(
                collectionId = collection.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails.firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> collectionApi.selectThumbnail(collection.id, thumb.id) }
        posterState.thumbnails.filter { it.markedDeleted }
            .forEach { thumb -> collectionApi.deleteThumbnail(collection.id, thumb.id) }
    }
}