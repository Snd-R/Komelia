package snd.komelia.image.coil

import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import coil3.size.isOriginal
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId

abstract class CoilFetcher(
    private val decoder: CoilAwareDecoder,
    private val options: Options,
) : Fetcher {

    protected abstract suspend fun fetchBytes(): ByteArray?

    // decode right away to avoid copying bytearray into okio buffer
    override suspend fun fetch(): FetchResult? {
        val bytes = fetchBytes() ?: return null
        decoder.decodeBytes(bytes, options).use { image ->
            return ImageFetchResult(
                image = image.toCoilImage(),
                isSampled = !options.size.isOriginal,
                dataSource = DataSource.NETWORK
            )
        }
    }
}

class KomgaBookDefaultThumbnailFetcher(
    private val bookApi: KomgaBookApi,
    private val bookId: KomgaBookId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = bookApi.getDefaultThumbnail(bookId)
}

class KomgaBookThumbnailFetcher(
    private val bookApi: KomgaBookApi,
    private val bookId: KomgaBookId,
    private val thumbnailId: KomgaThumbnailId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = bookApi.getThumbnail(bookId, thumbnailId)
}

class KomgaSeriesDefaultThumbnailFetcher(
    private val seriesApi: KomgaSeriesApi,
    private val seriesId: KomgaSeriesId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = seriesApi.getDefaultThumbnail(seriesId)
}

class KomgaSeriesThumbnailFetcher(
    private val seriesApi: KomgaSeriesApi,
    private val seriesId: KomgaSeriesId,
    private val thumbnailId: KomgaThumbnailId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = seriesApi.getThumbnail(seriesId, thumbnailId)
}

class KomgaCollectionDefaultThumbnailFetcher(
    private val collectionApi: KomgaCollectionsApi,
    private val collectionId: KomgaCollectionId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = collectionApi.getDefaultThumbnail(collectionId)
}

class KomgaCollectionThumbnailFetcher(
    private val collectionApi: KomgaCollectionsApi,
    private val collectionId: KomgaCollectionId,
    private val thumbnailId: KomgaThumbnailId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = collectionApi.getThumbnail(collectionId, thumbnailId)
}

class KomgaReadListDefaultThumbnailFetcher(
    private val readListApi: KomgaReadListApi,
    private val readListId: KomgaReadListId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = readListApi.getDefaultThumbnail(readListId)
}

class KomgaReadListThumbnailFetcher(
    private val readListApi: KomgaReadListApi,
    private val readListId: KomgaReadListId,
    private val thumbnailId: KomgaThumbnailId,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = readListApi.getThumbnail(readListId, thumbnailId)
}

class KomgaBookPageThumbnailFetcher(
    private val bookApi: KomgaBookApi,
    private val bookId: KomgaBookId,
    private val pageNumber: Int,
    decoder: CoilAwareDecoder,
    options: Options,
) : CoilFetcher(decoder, options) {
    override suspend fun fetchBytes() = bookApi.getPageThumbnail(bookId, pageNumber)
}
