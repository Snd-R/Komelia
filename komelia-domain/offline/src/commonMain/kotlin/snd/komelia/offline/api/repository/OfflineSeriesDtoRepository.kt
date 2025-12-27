package snd.komelia.offline.api.repository

import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.user.KomgaUserId

interface OfflineSeriesDtoRepository {
    suspend fun get(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId,
    ): KomgaSeries

    suspend fun find(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId,
    ): KomgaSeries?

    suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaSeries>

    suspend fun findAll(
        search: KomgaSeriesSearch,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaSeries>

    suspend fun findAllRecentlyUpdated(
        search: KomgaSeriesSearch,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries>
}