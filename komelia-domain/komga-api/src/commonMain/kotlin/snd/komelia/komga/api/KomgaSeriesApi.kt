package snd.komelia.komga.api

import snd.komelia.komga.api.model.KomeliaSeries
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.search.SeriesConditionBuilder
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.series.KomgaSeriesThumbnail


interface KomgaSeriesApi {
    suspend fun getOneSeries(seriesId: KomgaSeriesId): KomeliaSeries

    suspend fun getSeriesList(
        conditionBuilder: SeriesConditionBuilder,
        fulltextSearch: String?,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaSeries>

    suspend fun getSeriesList(
        search: KomgaSeriesSearch,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaSeries>

    suspend fun getNewSeries(
        libraryIds: List<KomgaLibraryId>? = null,
        oneshot: Boolean? = null,
        deleted: Boolean? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaSeries>

    suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>? = null,
        oneshot: Boolean? = null,
        deleted: Boolean? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaSeries>

    suspend fun analyze(seriesId: KomgaSeriesId)
    suspend fun refreshMetadata(seriesId: KomgaSeriesId)
    suspend fun markAsRead(seriesId: KomgaSeriesId)
    suspend fun markAsUnread(seriesId: KomgaSeriesId)
    suspend fun delete(seriesId: KomgaSeriesId)
    suspend fun update(seriesId: KomgaSeriesId, request: KomgaSeriesMetadataUpdateRequest)
    suspend fun getDefaultThumbnail(seriesId: KomgaSeriesId): ByteArray?
    suspend fun getThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId): ByteArray
    suspend fun getThumbnails(seriesId: KomgaSeriesId): List<KomgaSeriesThumbnail>
    suspend fun uploadThumbnail(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        filename: String = "",
        selected: Boolean = true
    ): KomgaSeriesThumbnail

    suspend fun selectThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId)
    suspend fun deleteThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId)
    suspend fun getAllCollectionsBySeries(seriesId: KomgaSeriesId): List<KomgaCollection>
}