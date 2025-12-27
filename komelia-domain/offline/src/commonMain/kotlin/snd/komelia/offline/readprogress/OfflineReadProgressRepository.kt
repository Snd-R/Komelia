package snd.komelia.offline.readprogress

import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

interface OfflineReadProgressRepository {
    suspend fun save(readProgress: OfflineReadProgress)
    suspend fun saveAll(readProgress: List<OfflineReadProgress>)
    suspend fun find(bookId: KomgaBookId, userId: KomgaUserId): OfflineReadProgress?

    suspend fun findAllByBookIdsAndUserId(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId,
    ): List<OfflineReadProgress>

    suspend fun findAllModifiedAfter(
        timestamp: Instant,
        userId: KomgaUserId,
        serverId: OfflineMediaServerId,
    ): List<OfflineReadProgress>

    suspend fun findAllByServer(
        userId: KomgaUserId,
        serverId: OfflineMediaServerId
    ): List<OfflineReadProgress>

    suspend fun deleteByUserId(userId: KomgaUserId, )

    suspend fun deleteByBookIdsAndUserId(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId,
    )

    suspend fun deleteBySeriesIds(seriesIds: List<KomgaSeriesId>)
    suspend fun deleteByBookIds(bookIds: List<KomgaBookId>)

    suspend fun delete(bookId: KomgaBookId, userId: KomgaUserId)
    suspend fun deleteAllBy(bookId: KomgaBookId)
}