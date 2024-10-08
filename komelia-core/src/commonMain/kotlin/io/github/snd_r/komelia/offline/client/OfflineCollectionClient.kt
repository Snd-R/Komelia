package io.github.snd_r.komelia.offline.client

import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.collection.KomgaCollectionCreateRequest
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.collection.KomgaCollectionQuery
import snd.komga.client.collection.KomgaCollectionThumbnail
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries

class OfflineCollectionClient: KomgaCollectionClient {

    override suspend fun addOne(request: KomgaCollectionCreateRequest): KomgaCollection {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCollectionThumbnail(collectionId: KomgaCollectionId, thumbnailId: KomgaThumbnailId) {
    }

    override suspend fun deleteOne(id: KomgaCollectionId) {
    }

    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaCollection> {
        TODO("Not yet implemented")
    }

    override suspend fun getCollectionThumbnails(collectionId: KomgaCollectionId): List<KomgaCollectionThumbnail> {
        TODO("Not yet implemented")
    }

    override suspend fun getOne(id: KomgaCollectionId): KomgaCollection {
        TODO("Not yet implemented")
    }

    override suspend fun getSeriesForCollection(
        id: KomgaCollectionId,
        query: KomgaCollectionQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun selectCollectionThumbnail(collectionId: KomgaCollectionId, thumbnailId: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun updateOne(id: KomgaCollectionId, request: KomgaCollectionUpdateRequest) {
    }

    override suspend fun uploadCollectionThumbnail(
        collectionId: KomgaCollectionId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaCollectionThumbnail {
        TODO("Not yet implemented")
    }
}