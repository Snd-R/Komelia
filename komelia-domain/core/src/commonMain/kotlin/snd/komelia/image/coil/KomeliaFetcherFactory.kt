package snd.komelia.image.coil

import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.request.Options
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.komga.api.KomgaApi
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId
import kotlin.random.Random

class KomeliaFetcherFactory(
    private val komgaApi: StateFlow<KomgaApi>,
    private val decoder: CoilAwareDecoder,
) : Fetcher.Factory<Any> {

    override fun create(
        data: Any,
        options: Options,
        imageLoader: ImageLoader
    ): Fetcher? {
        return when (data) {
            is BookDefaultThumbnailRequest -> KomgaBookDefaultThumbnailFetcher(
                bookApi = komgaApi.value.bookApi,
                bookId = data.bookId,
                decoder = decoder,
                options = options
            )

            is BookThumbnailRequest -> KomgaBookThumbnailFetcher(
                bookApi = komgaApi.value.bookApi,
                bookId = data.bookId,
                thumbnailId = data.thumbnailId,
                decoder = decoder,
                options = options
            )

            is SeriesDefaultThumbnailRequest -> KomgaSeriesDefaultThumbnailFetcher(
                seriesApi = komgaApi.value.seriesApi,
                seriesId = data.seriesId,
                decoder = decoder,
                options = options
            )

            is SeriesThumbnailRequest -> KomgaSeriesThumbnailFetcher(
                seriesApi = komgaApi.value.seriesApi,
                seriesId = data.seriesId,
                thumbnailId = data.thumbnailId,
                decoder = decoder,
                options = options
            )

            is ReadListDefaultThumbnailRequest -> KomgaReadListDefaultThumbnailFetcher(
                readListApi = komgaApi.value.readListApi,
                readListId = data.readListId,
                decoder = decoder,
                options = options
            )

            is ReadListThumbnailRequest -> KomgaReadListThumbnailFetcher(
                readListApi = komgaApi.value.readListApi,
                readListId = data.readListId,
                thumbnailId = data.thumbnailId,
                decoder = decoder,
                options = options
            )

            is CollectionDefaultThumbnailRequest -> KomgaCollectionDefaultThumbnailFetcher(
                collectionApi = komgaApi.value.collectionsApi,
                collectionId = data.collectionId,
                decoder = decoder,
                options = options
            )

            is CollectionThumbnailRequest -> KomgaCollectionThumbnailFetcher(
                collectionApi = komgaApi.value.collectionsApi,
                collectionId = data.collectionId,
                thumbnailId = data.thumbnailId,
                decoder = decoder,
                options = options
            )

            is BookPageThumbnailRequest -> KomgaBookPageThumbnailFetcher(
                bookApi = komgaApi.value.bookApi,
                bookId = data.bookId,
                pageNumber = data.pageNumber,
                decoder = decoder,
                options = options
            )

            else -> null
        }
    }

}

data class BookDefaultThumbnailRequest(
    val bookId: KomgaBookId,
    val requestCache: Int = Random.nextInt()
)

data class BookThumbnailRequest(
    val bookId: KomgaBookId,
    val thumbnailId: KomgaThumbnailId
)

data class BookPageThumbnailRequest(
    val bookId: KomgaBookId,
    val pageNumber: Int
)

data class SeriesDefaultThumbnailRequest(
    val seriesId: KomgaSeriesId,
    val requestCache: Int = Random.nextInt()
)

data class SeriesThumbnailRequest(
    val seriesId: KomgaSeriesId,
    val thumbnailId: KomgaThumbnailId
)

data class CollectionDefaultThumbnailRequest(
    val collectionId: KomgaCollectionId,
    val requestCache: Int = Random.nextInt()
)

data class CollectionThumbnailRequest(
    val collectionId: KomgaCollectionId,
    val thumbnailId: KomgaThumbnailId
)

data class ReadListDefaultThumbnailRequest(
    val readListId: KomgaReadListId,
    val requestCache: Int = Random.nextInt()
)

data class ReadListThumbnailRequest(
    val readListId: KomgaReadListId,
    val thumbnailId: KomgaThumbnailId
)
