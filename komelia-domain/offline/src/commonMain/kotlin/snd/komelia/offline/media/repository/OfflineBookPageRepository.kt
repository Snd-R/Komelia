package snd.komelia.offline.media.repository

import snd.komelia.offline.media.model.OfflineBookPage
import snd.komga.client.book.KomgaBookId

interface OfflineBookPageRepository {
    suspend fun save(media: OfflineBookPage)
    suspend fun save(media: List<OfflineBookPage>)
    suspend fun find(id: KomgaBookId): OfflineBookPage?
    suspend fun findAll(id: KomgaBookId): List<OfflineBookPage>
    suspend fun delete(id: KomgaBookId)
}