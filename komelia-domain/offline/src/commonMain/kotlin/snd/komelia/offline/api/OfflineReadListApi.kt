package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.readlist.KomgaReadListQuery
import snd.komga.client.readlist.KomgaReadListThumbnail
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class OfflineReadListApi : KomgaReadListApi {
    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaReadList> {
        return Page.empty()
    }

    override suspend fun getOne(id: KomgaReadListId): KomgaReadList {
        TODO("Not yet implemented")
    }

    override suspend fun addOne(request: KomgaReadListCreateRequest): KomgaReadList {
        TODO("Not yet implemented")
    }

    override suspend fun updateOne(
        id: KomgaReadListId,
        request: KomgaReadListUpdateRequest
    ) {
    }

    override suspend fun deleteOne(id: KomgaReadListId) {
    }

    override suspend fun getBooksForReadList(
        id: KomgaReadListId,
        query: KomgaReadListQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> {
        return Page.empty()
    }

    override suspend fun getDefaultThumbnail(readListId: KomgaReadListId): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun getThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun getThumbnails(readListId: KomgaReadListId): List<KomgaReadListThumbnail> {
        return emptyList()
    }

    override suspend fun uploadThumbnail(
        readListId: KomgaReadListId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaReadListThumbnail {
        TODO("Not yet implemented")
    }

    override suspend fun selectThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ) {
    }

    override suspend fun deleteThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ) {
    }

    override suspend fun getBookSiblingNext(
        readListId: KomgaReadListId,
        bookId: KomgaBookId
    ): KomeliaBook {
        TODO("Not yet implemented")
    }

    override suspend fun getBookSiblingPrevious(
        readListId: KomgaReadListId,
        bookId: KomgaBookId
    ): KomeliaBook {
        TODO("Not yet implemented")
    }
}