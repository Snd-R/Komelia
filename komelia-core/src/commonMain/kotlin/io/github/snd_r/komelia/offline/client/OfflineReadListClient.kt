package io.github.snd_r.komelia.offline.client

import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.readlist.KomgaReadListQuery
import snd.komga.client.readlist.KomgaReadListThumbnail
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class OfflineReadListClient : KomgaReadListClient {
    override suspend fun addOne(request: KomgaReadListCreateRequest): KomgaReadList {
        TODO("Not yet implemented")
    }

    override suspend fun deleteOne(id: KomgaReadListId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReadListThumbnail(readListId: KomgaReadListId, thumbnailId: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaReadList> {
        TODO("Not yet implemented")
    }

    override suspend fun getBookSiblingNext(readListId: KomgaReadListId, bookId: KomgaBookId): KomgaBook {
        TODO("Not yet implemented")
    }

    override suspend fun getBookSiblingPrevious(readListId: KomgaReadListId, bookId: KomgaBookId): KomgaBook {
        TODO("Not yet implemented")
    }

    override suspend fun getBooksForReadList(
        id: KomgaReadListId,
        query: KomgaReadListQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun getOne(id: KomgaReadListId): KomgaReadList {
        TODO("Not yet implemented")
    }

    override suspend fun getReadListThumbnails(readListId: KomgaReadListId): List<KomgaReadListThumbnail> {
        TODO("Not yet implemented")
    }

    override suspend fun selectReadListThumbnail(readListId: KomgaReadListId, thumbnailId: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun updateOne(id: KomgaReadListId, request: KomgaReadListUpdateRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadReadListThumbnail(
        readListId: KomgaReadListId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaReadListThumbnail {
        TODO("Not yet implemented")
    }
}