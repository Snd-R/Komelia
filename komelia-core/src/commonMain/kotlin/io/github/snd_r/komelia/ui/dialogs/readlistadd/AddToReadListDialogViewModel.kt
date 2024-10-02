package io.github.snd_r.komelia.ui.dialogs.readlistadd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import snd.komga.client.book.KomgaBook
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class AddToReadListDialogViewModel(
    private val books: List<KomgaBook>,
    private val onDismissRequest: () -> Unit,
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
) {

    var readLists by mutableStateOf<List<KomgaReadList>>(emptyList())
        private set

    suspend fun initialize() {
        readLists = readListClient.getAll().content.sortedByDescending { it.lastModifiedDate }
    }

    suspend fun addTo(readList: KomgaReadList) {
        appNotifications.runCatchingToNotifications {
            readListClient.updateOne(
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
            readListClient.addOne(
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