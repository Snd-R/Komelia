package snd.komelia.ui.dialogs.readlistedit

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.ui.LoadState
import snd.komelia.ui.dialogs.PosterEditState
import snd.komelia.ui.dialogs.PosterTab
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.patch
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class ReadListEditDialogViewModel(
    val readList: KomgaReadList,
    val onDialogDismiss: () -> Unit,
    private val readListApi: KomgaReadListApi,
    private val notifications: AppNotifications,
    cardWidth: Flow<Dp>,
) {
    private var state by mutableStateOf<LoadState<Unit>>(LoadState.Uninitialized)
    var name by mutableStateOf(readList.name)
    var summary by mutableStateOf(readList.summary)
    private var readLists by mutableStateOf<List<KomgaReadList>>(emptyList())
    val nameValidationError by derivedStateOf {
        when {
            name.isBlank() -> "Required"
            name != readList.name && readLists.any { it.name == this.name } -> "A read list with this name already exists"
            else -> null
        }
    }

    var manualOrdering by mutableStateOf(readList.ordered)
    private val posterState = PosterEditState(cardWidth)

    private val generalTab = GeneralTab(this)
    private val posterTab = PosterTab(posterState)
    var currentTab by mutableStateOf<DialogTab>(generalTab)

    suspend fun initialize() {
        if (state != LoadState.Uninitialized) return

        notifications.runCatchingToNotifications {
            this.readLists = readListApi.getAll(pageRequest = KomgaPageRequest(unpaged = true)).content

            posterState.thumbnails = readListApi.getThumbnails(readList.id).map {
                PosterEditState.KomgaThumbnail.ReadListThumbnail(it)
            }
        }
        state = LoadState.Success(Unit)
    }

    fun tabs(): List<DialogTab> = listOf(generalTab, posterTab)

    fun canSave() = state != LoadState.Uninitialized && nameValidationError == null

    suspend fun saveChanges() {
        if (!canSave()) return

        notifications.runCatchingToNotifications {
            val request = KomgaReadListUpdateRequest(
                name = patch(readList.name, name),
                summary = patch(readList.summary, summary),
                ordered = patch(readList.ordered, manualOrdering),
            )
            readListApi.updateOne(readList.id, request)

            saveThumbnailChanges()
            onDialogDismiss()
        }
    }

    private suspend fun saveThumbnailChanges() {
        posterState.userUploadedThumbnails.forEach { thumb ->
            readListApi.uploadThumbnail(
                readListId = readList.id,
                file = thumb.file.readBytes(),
                selected = thumb.selected
            )
        }

        posterState.thumbnails.firstOrNull { it.markedSelected && it.markedSelected != it.selected }
            ?.let { thumb -> readListApi.selectThumbnail(readList.id, thumb.id) }
        posterState.thumbnails.filter { it.markedDeleted }
            .forEach { thumb -> readListApi.deleteThumbnail(readList.id, thumb.id) }
    }
}