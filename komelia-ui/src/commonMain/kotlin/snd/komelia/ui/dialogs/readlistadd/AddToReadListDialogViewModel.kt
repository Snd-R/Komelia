package snd.komelia.ui.dialogs.readlistadd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class AddToReadListDialogViewModel(
    private val books: List<KomeliaBook>,
    private val onDismissRequest: () -> Unit,
    private val readListApi: KomgaReadListApi,
    private val appNotifications: AppNotifications,
) {

    var readLists by mutableStateOf<List<KomgaReadList>>(emptyList())
        private set

    suspend fun initialize() {
        readLists = readListApi.getAll(pageRequest = KomgaPageRequest(unpaged = true))
            .content.sortedByDescending { it.lastModifiedDate }
    }

    suspend fun addTo(readList: KomgaReadList) {
        appNotifications.runCatchingToNotifications {
            readListApi.updateOne(
                readList.id,
                KomgaReadListUpdateRequest(
                    bookIds = PatchValue.Some(
                        (readList.bookIds + books.map { it.id }).distinct()
                    )
                )
            )
            onDismissRequest()
        }
    }

    suspend fun createNew(name: String) {
        appNotifications.runCatchingToNotifications {
            readListApi.addOne(
                KomgaReadListCreateRequest(
                    name = name,
                    summary = "",
                    ordered = true,
                    bookIds = books.map { it.id }
                )
            )
            onDismissRequest()
        }
    }
}