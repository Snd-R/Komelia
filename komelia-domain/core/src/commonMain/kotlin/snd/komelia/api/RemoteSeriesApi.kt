package snd.komelia.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.mangabaka.KomfMangaBakaLinkedSeries
import snd.komf.client.KomfMangaBakaClient
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.search.SeriesConditionBuilder
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest
import snd.komga.client.series.KomgaSeriesSearch

class RemoteSeriesApi(
    private val seriesClient: KomgaSeriesClient,
    private val komfMangaBakaClient: Flow<KomfMangaBakaClient?>,
) : KomgaSeriesApi {
    override suspend fun getOneSeries(seriesId: KomgaSeriesId): KomeliaSeries {
        val series = seriesClient.getOneSeries(seriesId)
        val mangaBaka = komfMangaBakaClient.first()?.getLinked(KomfServerSeriesId(series.id.value))
        return KomeliaSeries(series, mangaBaka?.mangaBaka)
    }

    override suspend fun getSeriesList(
        conditionBuilder: SeriesConditionBuilder,
        fulltextSearch: String?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> {
        val seriesPage = seriesClient.getSeriesList(conditionBuilder, fulltextSearch, pageRequest)
        return seriesPage.toKomeliaPage()
    }

    override suspend fun getSeriesList(
        search: KomgaSeriesSearch,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> {
        val seriesPage = seriesClient.getSeriesList(search, pageRequest)
        return seriesPage.toKomeliaPage()
    }

    override suspend fun getNewSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> {
        val seriesPage = seriesClient.getNewSeries(
            libraryIds = libraryIds,
            oneshot = oneshot,
            deleted = deleted,
            pageRequest = pageRequest
        )
        return seriesPage.toKomeliaPage()
    }

    override suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> {
        val seriesPage = seriesClient.getUpdatedSeries(
            libraryIds = libraryIds,
            oneshot = oneshot,
            deleted = deleted,
            pageRequest = pageRequest
        )
        return seriesPage.toKomeliaPage()
    }

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

    internal suspend fun Page<KomgaSeries>.toKomeliaPage(): Page<KomeliaSeries> {
        val komfIds = this.content.map { KomfServerSeriesId(it.id.value) }
        val mangaBaka = komfMangaBakaClient.first()?.getAllLinked(komfIds).orEmpty()
        return this.toKomeliaPage(mangaBaka)
    }
}

internal fun Page<KomgaSeries>.toKomeliaPage(mangaBaka: List<KomfMangaBakaLinkedSeries>): Page<KomeliaSeries> {
    val mangaBaka = mangaBaka.associateBy { it.komgaId.value }
    return Page(
        content = this.content.map { KomeliaSeries(it, mangaBaka[it.id.value]?.mangaBaka) },
        pageable = this.pageable,
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        last = this.last,
        number = this.number,
        sort = this.sort,
        first = this.first,
        numberOfElements = this.numberOfElements,
        size = this.size,
        empty = this.empty
    )

}
