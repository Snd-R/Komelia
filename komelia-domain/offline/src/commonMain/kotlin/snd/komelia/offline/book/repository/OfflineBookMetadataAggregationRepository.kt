package snd.komelia.offline.book.repository

import snd.komelia.offline.series.model.OfflineBookMetadataAggregation
import snd.komga.client.series.KomgaSeriesId

interface OfflineBookMetadataAggregationRepository {
    suspend fun save(metadata: OfflineBookMetadataAggregation)
    suspend fun find(seriesId: KomgaSeriesId): OfflineBookMetadataAggregation?
    suspend fun get(seriesId: KomgaSeriesId): OfflineBookMetadataAggregation
    suspend fun delete(seriesId: KomgaSeriesId)
    suspend fun delete(seriesIds: List<KomgaSeriesId>)
}