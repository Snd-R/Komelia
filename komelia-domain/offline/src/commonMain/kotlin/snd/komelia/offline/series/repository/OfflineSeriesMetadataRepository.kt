package snd.komelia.offline.series.repository

import snd.komelia.offline.series.model.OfflineSeriesMetadata
import snd.komga.client.series.KomgaSeriesId

interface OfflineSeriesMetadataRepository {
    suspend fun save(metadata: OfflineSeriesMetadata)
    suspend fun find(id: KomgaSeriesId): OfflineSeriesMetadata?
    suspend fun delete(id: KomgaSeriesId)
    suspend fun delete(seriesIds: List<KomgaSeriesId>)
}