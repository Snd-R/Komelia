package snd.komelia.offline.book.actions

import snd.komelia.offline.action.OfflineAction
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadataUpdateRequest

class BookMetadataUpdateAction : OfflineAction {
    suspend fun run(
        bookId: KomgaBookId,
        request: KomgaBookMetadataUpdateRequest
    ) {
        TODO("Not yet implemented")
    }
}