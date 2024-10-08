package io.github.snd_r.komelia.offline.client

import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadataUpdateRequest
import snd.komga.client.series.KomgaSeriesQuery
import snd.komga.client.series.KomgaSeriesThumbnail

class OfflineSeriesClient : KomgaSeriesClient {
    override suspend fun analyze(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSeries(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSeriesThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllBooksBySeries(
        seriesId: KomgaSeriesId,
        mediaStatus: List<KomgaMediaStatus>?,
        readStatus: List<KomgaReadStatus>?,
        tag: List<String>?,
        authors: List<KomgaAuthor>?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllCollectionsBySeries(seriesId: KomgaSeriesId): List<KomgaCollection> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllSeries(query: KomgaSeriesQuery?, pageRequest: KomgaPageRequest?): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun getNewSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun getOneSeries(seriesId: KomgaSeriesId): KomgaSeries {
        TODO("Not yet implemented")
    }

    override suspend fun getSeriesDefaultThumbnail(seriesId: KomgaSeriesId): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun getSeriesThumbnails(seriesId: KomgaSeriesId): List<KomgaSeriesThumbnail> {
        TODO("Not yet implemented")
    }

    override suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>?,
        oneshot: Boolean?,
        deleted: Boolean?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun markAsRead(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun markAsUnread(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun refreshMetadata(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun selectSeriesThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun updateSeries(seriesId: KomgaSeriesId, request: KomgaSeriesMetadataUpdateRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadSeriesThumbnail(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaSeriesThumbnail {
        TODO("Not yet implemented")
    }
}