package snd.komelia.offline.book.actions

import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.model.OfflineThumbnailBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaThumbnailId

class BookThumbnailDeleteAction : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        thumbnailId: KomgaThumbnailId
    ): OfflineThumbnailBook {
        TODO("Not yet implemented")
    }
}