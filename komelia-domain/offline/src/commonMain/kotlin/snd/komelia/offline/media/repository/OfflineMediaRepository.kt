package snd.komelia.offline.media.repository

import snd.komelia.offline.media.model.OfflineMedia
import snd.komga.client.book.KomgaBookId

interface OfflineMediaRepository {
    suspend fun save(media: OfflineMedia)
    suspend fun find(id: KomgaBookId): OfflineMedia?
    suspend fun findAll(ids: List<KomgaBookId>): List<OfflineMedia>
    suspend fun get(id: KomgaBookId): OfflineMedia
    suspend fun delete(id: KomgaBookId)
    suspend fun delete(bookIds: List<KomgaBookId>)
}