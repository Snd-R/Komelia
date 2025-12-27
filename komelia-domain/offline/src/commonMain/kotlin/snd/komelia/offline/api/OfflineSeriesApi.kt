package snd.komelia.offline.api

import kotlinx.coroutines.flow.StateFlow
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.offline.action.OfflineActions
import snd.komelia.offline.api.repository.OfflineSeriesDtoRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.readprogress.actions.ProgressCompleteForSeriesAction
import snd.komelia.offline.readprogress.actions.ProgressDeleteForSeriesAction
import snd.komelia.offline.series.actions.SeriesAddThumbnailAction
import snd.komelia.offline.series.actions.SeriesAnalyzeAction
import snd.komelia.offline.series.actions.SeriesDeleteAction
import snd.komelia.offline.series.actions.SeriesDeleteThumbnailAction
import snd.komelia.offline.series.actions.SeriesRefreshMetadataAction
import snd.komelia.offline.series.actions.SeriesSelectThumbnailAction
import snd.komelia.offline.series.actions.SeriesUpdateMetadataAction
import snd.komelia.offline.series.model.OfflineThumbnailSeries
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.SeriesCover
import snd.komga.client.search.SeriesConditionBuilder
import snd.komga.client.search.allOfSeries
import snd.komga.client.search.anyOfSeries
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.series.KomgaSeriesThumbnail
import snd.komga.client.user.KomgaUserId

class OfflineSeriesApi(
    private val actions: OfflineActions,
    private val seriesDtoRepository: OfflineSeriesDtoRepository,
    private val seriesThumbnailRepository: OfflineThumbnailSeriesRepository,
    private val seriesRepository: OfflineSeriesRepository,
    private val libraryRepository: OfflineLibraryRepository,
    private val bookRepository: OfflineBookRepository,
    private val thumbnailBookRepository: OfflineThumbnailBookRepository,
    private val offlineUserId: StateFlow<KomgaUserId>,
) : KomgaSeriesApi {
    private val userId
        get() = offlineUserId.value

    override suspend fun getOneSeries(seriesId: KomgaSeriesId): KomgaSeries {
        return seriesDtoRepository.get(seriesId, userId)
    }

    override suspend fun getSeriesList(
        conditionBuilder: SeriesConditionBuilder,
        fulltextSearch: String?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        return getSeriesList(KomgaSeriesSearch(conditionBuilder.toSeriesCondition(), fulltextSearch), pageRequest)
    }

    override suspend fun getSeriesList(
        search: KomgaSeriesSearch,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        return seriesDtoRepository.findAll(
            search = search,
            userId = userId,
            pageRequest = pageRequest ?: KomgaPageRequest()
        )
    }

    override suspend fun getNewSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        val sort = KomgaSort.KomgaSeriesSort.byCreatedDateDesc()
        val pageable = (pageRequest ?: KomgaPageRequest()).copy(sort = sort)

        val condition = allOfSeries {
            if (!libraryIds.isNullOrEmpty()) anyOfSeries { libraryIds.forEach { library { isEqualTo(it) } } }
            deleted?.let { if (it) isDeleted() else isNotDeleted() }
            oneshot?.let { if (it) isOneshot() else isNotOneshot() }
        }.toSeriesCondition()

        return seriesDtoRepository.findAll(
            search = KomgaSeriesSearch(condition),
            userId = userId,
            pageRequest = pageable
        )
    }

    override suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        val condition = allOfSeries {
            if (!libraryIds.isNullOrEmpty()) anyOfSeries { libraryIds.forEach { library { isEqualTo(it) } } }
            deleted?.let { if (it) isDeleted() else isNotDeleted() }
            oneshot?.let { if (it) isOneshot() else isNotOneshot() }
        }.toSeriesCondition()

        return seriesDtoRepository.findAllRecentlyUpdated(
            search = KomgaSeriesSearch(condition),
            userId = userId,
            pageRequest = pageRequest ?: KomgaPageRequest.DEFAULT
        )
    }

    override suspend fun analyze(seriesId: KomgaSeriesId) {
        actions.get<SeriesAnalyzeAction>()
            .run(seriesId)
    }

    override suspend fun refreshMetadata(seriesId: KomgaSeriesId) {
        actions.get<SeriesRefreshMetadataAction>()
            .run(seriesId)
    }

    override suspend fun markAsRead(seriesId: KomgaSeriesId) {
        actions.get<ProgressCompleteForSeriesAction>().execute(
            seriesId = seriesId,
            userId = userId
        )
    }

    override suspend fun markAsUnread(seriesId: KomgaSeriesId) {
        actions.get<ProgressDeleteForSeriesAction>().run(
            seriesId = seriesId,
            userId = userId
        )
    }

    override suspend fun delete(seriesId: KomgaSeriesId) {
        actions.get<SeriesDeleteAction>().execute(
            seriesId = seriesId,
        )
    }

    override suspend fun update(
        seriesId: KomgaSeriesId,
        request: KomgaSeriesMetadataUpdateRequest
    ) {
        actions.get<SeriesUpdateMetadataAction>()
            .run(seriesId, request)
    }

    override suspend fun getDefaultThumbnail(seriesId: KomgaSeriesId): ByteArray? {
        val selectedThumbnail = seriesThumbnailRepository.findSelectedBySeriesId(seriesId)?.thumbnail
        if (selectedThumbnail != null) return selectedThumbnail
        val series = seriesRepository.get(seriesId)
        val library = libraryRepository.get(series.libraryId)

        val bookId = when (library.seriesCover) {
            SeriesCover.FIRST -> bookRepository.findFirstIdInSeriesOrNull(seriesId)
            SeriesCover.FIRST_UNREAD_OR_FIRST ->
                bookRepository.findFirstUnreadIdInSeriesOrNull(seriesId, userId)
                    ?: bookRepository.findFirstIdInSeriesOrNull(seriesId)

            SeriesCover.FIRST_UNREAD_OR_LAST ->
                bookRepository.findFirstUnreadIdInSeriesOrNull(seriesId, userId)
                    ?: bookRepository.findLastIdInSeriesOrNull(seriesId)

            SeriesCover.LAST -> bookRepository.findLastIdInSeriesOrNull(seriesId)
        }

        if (bookId != null) return thumbnailBookRepository.findSelectedByBookId(bookId)?.thumbnail

        return null
    }

    override suspend fun getThumbnail(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun getThumbnails(seriesId: KomgaSeriesId): List<KomgaSeriesThumbnail> {
        return seriesThumbnailRepository.findAllBySeriesId(seriesId).map { it.toKomgaSeriesThumbnail() }
    }

    override suspend fun uploadThumbnail(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaSeriesThumbnail {
        val thumbnail = actions.get<SeriesAddThumbnailAction>()
            .run(seriesId, file, selected)

        return thumbnail.toKomgaSeriesThumbnail()
    }

    override suspend fun selectThumbnail(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) {
        actions.get<SeriesSelectThumbnailAction>()
            .run(seriesId, thumbnailId)
    }

    override suspend fun deleteThumbnail(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) {
        actions.get<SeriesDeleteThumbnailAction>()
            .run(seriesId, thumbnailId)
    }

    override suspend fun getAllCollectionsBySeries(seriesId: KomgaSeriesId): List<KomgaCollection> {
        return emptyList()
    }

    private fun OfflineThumbnailSeries.toKomgaSeriesThumbnail() = KomgaSeriesThumbnail(
        id = this.id,
        seriesId = this.seriesId,
        type = this.type.name,
        selected = this.selected,
        mediaType = this.mediaType,
        fileSize = this.fileSize,
        width = this.width,
        height = this.height
    )

}