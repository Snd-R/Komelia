package snd.komelia.api

import snd.komelia.komga.api.KomgaSeriesApi
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.search.SeriesConditionBuilder
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest
import snd.komga.client.series.KomgaSeriesSearch

class RemoteSeriesApi(private val seriesClient: KomgaSeriesClient) : KomgaSeriesApi {
    override suspend fun getOneSeries(seriesId: KomgaSeriesId) = seriesClient.getOneSeries(seriesId)

    override suspend fun getSeriesList(
        conditionBuilder: SeriesConditionBuilder,
        fulltextSearch: String?,
        pageRequest: KomgaPageRequest?
    ) = seriesClient.getSeriesList(conditionBuilder, fulltextSearch, pageRequest)

    override suspend fun getSeriesList(
        search: KomgaSeriesSearch,
        pageRequest: KomgaPageRequest?
    ) = seriesClient.getSeriesList(search, pageRequest)

    override suspend fun getNewSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ) = seriesClient.getNewSeries(
        libraryIds = libraryIds,
        oneshot = oneshot,
        deleted = deleted,
        pageRequest = pageRequest
    )

    override suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ) = seriesClient.getUpdatedSeries(
        libraryIds = libraryIds,
        oneshot = oneshot,
        deleted = deleted,
        pageRequest = pageRequest
    )

    override suspend fun analyze(seriesId: KomgaSeriesId) = seriesClient.analyze(seriesId)

    override suspend fun refreshMetadata(seriesId: KomgaSeriesId) = seriesClient.refreshMetadata(seriesId)

    override suspend fun markAsRead(seriesId: KomgaSeriesId) = seriesClient.markAsRead(seriesId)

    override suspend fun markAsUnread(seriesId: KomgaSeriesId) = seriesClient.markAsUnread(seriesId)

    override suspend fun delete(seriesId: KomgaSeriesId) = seriesClient.delete(seriesId)

    override suspend fun update(
        seriesId: KomgaSeriesId,
        request: KomgaSeriesMetadataUpdateRequest
    ) = seriesClient.update(seriesId, request)

    override suspend fun getDefaultThumbnail(seriesId: KomgaSeriesId) =
        seriesClient.getDefaultThumbnail(seriesId)

    override suspend fun getThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId) =
        seriesClient.getThumbnail(seriesId, thumbnailId)

    override suspend fun getThumbnails(seriesId: KomgaSeriesId) = seriesClient.getThumbnails(seriesId)

    override suspend fun uploadThumbnail(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ) = seriesClient.uploadThumbnail(
        seriesId = seriesId,
        file = file,
        filename = filename,
        selected = selected
    )

    override suspend fun selectThumbnail(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) = seriesClient.selectThumbnail(seriesId, thumbnailId)

    override suspend fun deleteThumbnail(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) = seriesClient.deleteThumbnail(seriesId, thumbnailId)

    override suspend fun getAllCollectionsBySeries(seriesId: KomgaSeriesId) =
        seriesClient.getAllCollectionsBySeries(seriesId)
}