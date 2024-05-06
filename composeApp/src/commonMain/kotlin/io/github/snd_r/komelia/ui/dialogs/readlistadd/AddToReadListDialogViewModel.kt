package io.github.snd_r.komelia.ui.dialogs.readlistadd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.readlist.KomgaReadList
import io.github.snd_r.komga.readlist.KomgaReadListClient
import io.github.snd_r.komga.readlist.KomgaReadListCreateRequest
import io.github.snd_r.komga.readlist.KomgaReadListUpdateRequest

class AddToReadListDialogViewModel(
    private val book: KomgaBook,
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
                KomgaReadListUpdateRequest(bookIds = PatchValue.Some(readList.bookIds + book.id))
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
                    bookIds = listOf(book.id)
                )
            )
            onDismissRequest()
        }
    }
}