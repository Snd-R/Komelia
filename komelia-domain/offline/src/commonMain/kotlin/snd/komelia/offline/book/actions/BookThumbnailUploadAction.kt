package snd.komelia.offline.book.actions

import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.model.OfflineThumbnailBook
import snd.komga.client.book.KomgaBookId

class BookThumbnailUploadAction : OfflineAction {
    suspend fun run(
        bookId: KomgaBookId,
        file: ByteArray,
        selected: Boolean
    ): OfflineThumbnailBook {
        TODO("Not yet implemented")
    }
}