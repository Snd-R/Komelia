package snd.komelia.offline.series.repository

import snd.komelia.offline.series.model.OfflineThumbnailSeries
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId

interface OfflineThumbnailSeriesRepository {
    suspend fun save(thumbnail: OfflineThumbnailSeries)

    suspend fun find(thumbnailId: KomgaThumbnailId): OfflineThumbnailSeries?

    suspend fun findSelectedBySeriesId(seriesId: KomgaSeriesId): OfflineThumbnailSeries?

    suspend fun findAllBySeriesId(seriesId: KomgaSeriesId): Collection<OfflineThumbnailSeries>

    suspend fun findAllBySeriesIdAndType(
        seriesId: KomgaSeriesId,
        type: OfflineThumbnailSeries.Type,
    ): List<OfflineThumbnailSeries>


    suspend fun markSelected(thumbnail: OfflineThumbnailSeries)

    suspend fun delete(thumbnailSeriesId: KomgaThumbnailId)
    suspend fun deleteBySeriesId(seriesId: KomgaSeriesId)

    suspend fun deleteBySeriesIds(seriesIds: List<KomgaSeriesId>)
}