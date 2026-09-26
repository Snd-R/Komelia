package snd.komelia.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komf.api.KomfServerSeriesId
import snd.komf.client.KomfMangaBakaClient
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.collection.KomgaCollectionCreateRequest
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.collection.KomgaCollectionQuery
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId

class RemoteCollectionsApi(
    private val collectionsClient: KomgaCollectionClient,
    private val komfMangaBakaClient: Flow<KomfMangaBakaClient?>,
) : KomgaCollectionsApi {
    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaCollection> = collectionsClient.getAll(search, libraryIds, pageRequest)

    override suspend fun getOne(id: KomgaCollectionId) = collectionsClient.getOne(id)

    override suspend fun addOne(request: KomgaCollectionCreateRequest) = collectionsClient.addOne(request)

    override suspend fun updateOne(
        id: KomgaCollectionId,
        request: KomgaCollectionUpdateRequest
    ) = collectionsClient.updateOne(id, request)

    override suspend fun deleteOne(id: KomgaCollectionId) = collectionsClient.deleteOne(id)

    override suspend fun getSeriesForCollection(
        id: KomgaCollectionId,
        query: KomgaCollectionQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaSeries> {
        val seriesPage = collectionsClient.getSeriesForCollection(id, query, pageRequest)
        val komfIds = seriesPage.content.map { KomfServerSeriesId(it.id.value) }
        val mangaBaka = komfMangaBakaClient.first()?.getAllLinked(komfIds).orEmpty()
        return seriesPage.toKomeliaPage(mangaBaka)
    }

    override suspend fun getDefaultThumbnail(collectionId: KomgaCollectionId) =
        collectionsClient.getDefaultThumbnail(collectionId)

    override suspend fun getThumbnail(collectionId: KomgaCollectionId, thumbnailId: KomgaThumbnailId) =
        collectionsClient.getThumbnail(collectionId, thumbnailId)

    override suspend fun getThumbnails(collectionId: KomgaCollectionId) =
        collectionsClient.getThumbnails(collectionId)

    override suspend fun uploadThumbnail(
        collectionId: KomgaCollectionId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ) = collectionsClient.uploadThumbnail(collectionId, file, filename, selected)

    override suspend fun selectThumbnail(
        collectionId: KomgaCollectionId,
        thumbnailId: KomgaThumbnailId
    ) = collectionsClient.selectThumbnail(collectionId, thumbnailId)

    override suspend fun deleteThumbnail(
        collectionId: KomgaCollectionId,
        thumbnailId: KomgaThumbnailId
    ) = collectionsClient.deleteThumbnail(collectionId, thumbnailId)
}