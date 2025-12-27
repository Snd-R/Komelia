package snd.komelia.offline.media.model

import snd.komga.client.book.KomgaBookId

data class OfflineBookPage(
    val bookId: KomgaBookId,
    val fileName: String,
    val mediaType: String,
    val width: Int?,
    val height: Int?,
    val fileSize: Long?,
)
