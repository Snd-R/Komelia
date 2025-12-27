package snd.komelia.offline.sync.model

import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId

sealed interface DownloadEvent {
    val bookId: KomgaBookId

    data class BookDownloadProgress(
        val book: KomgaBook,
        val total: Long,
        val completed: Long,
    ) : DownloadEvent {
        override val bookId: KomgaBookId = book.id

    }

    data class BookDownloadError(
        override val bookId: KomgaBookId,
        val error: Throwable,
        val book: KomgaBook?=null,
    ) : DownloadEvent

    data class BookDownloadCompleted(
        val book: KomgaBook,
    ) : DownloadEvent {
        override val bookId: KomgaBookId = book.id
    }
}
