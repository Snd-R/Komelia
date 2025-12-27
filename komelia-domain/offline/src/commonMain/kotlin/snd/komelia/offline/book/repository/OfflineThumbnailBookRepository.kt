package snd.komelia.offline.book.repository

import snd.komelia.offline.book.model.OfflineThumbnailBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaThumbnailId

interface OfflineThumbnailBookRepository {
    suspend fun save(thumbnail: OfflineThumbnailBook)
    suspend fun find(id: KomgaThumbnailId): OfflineThumbnailBook?
    suspend fun findSelectedByBookId(bookId: KomgaBookId): OfflineThumbnailBook?
    suspend fun findAllByBookId(bookId: KomgaBookId): List<OfflineThumbnailBook>

    suspend fun findAllByBookIdAndType(
        bookId: KomgaBookId,
        type: Collection<OfflineThumbnailBook.Type>,
    ): List<OfflineThumbnailBook>

    suspend fun markSelected(thumbnail: OfflineThumbnailBook)

    suspend fun delete(id: KomgaThumbnailId)
    suspend fun deleteByBookIdAndType(id: KomgaBookId, type: OfflineThumbnailBook.Type)
    suspend fun deleteAllBy(id: KomgaBookId)

    suspend fun deleteByBookIds(bookIds: Collection<KomgaBookId>)
}