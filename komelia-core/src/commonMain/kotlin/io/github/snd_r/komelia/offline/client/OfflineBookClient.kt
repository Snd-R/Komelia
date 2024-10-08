package io.github.snd_r.komelia.offline.client

import io.ktor.client.statement.*
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadataUpdateRequest
import snd.komga.client.book.KomgaBookPage
import snd.komga.client.book.KomgaBookQuery
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest
import snd.komga.client.book.KomgaBookThumbnail
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList

class OfflineBookClient: KomgaBookClient {
    override suspend fun analyze(bookId: KomgaBookId) {
    }

    override suspend fun deleteBook(bookId: KomgaBookId) {
    }

    override suspend fun deleteBookThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId) {
    }

    override suspend fun deleteReadProgress(bookId: KomgaBookId) {
    }

    override suspend fun getAllBooks(query: KomgaBookQuery?, pageRequest: KomgaPageRequest?): Page<KomgaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllReadListsByBook(bookId: KomgaBookId): List<KomgaReadList> {
        TODO("Not yet implemented")
    }

    override suspend fun getBook(bookId: KomgaBookId): KomgaBook {
        TODO("Not yet implemented")
    }

    override suspend fun getBookPage(bookId: KomgaBookId, page: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun getBookPages(bookId: KomgaBookId): List<KomgaBookPage> {
        TODO("Not yet implemented")
    }

    override suspend fun getBookSiblingNext(bookId: KomgaBookId): KomgaBook {
        TODO("Not yet implemented")
    }

    override suspend fun getBookSiblingPrevious(bookId: KomgaBookId): KomgaBook {
        TODO("Not yet implemented")
    }

    override suspend fun getBookThumbnail(bookId: KomgaBookId): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun getBookThumbnails(bookId: KomgaBookId): List<KomgaBookThumbnail> {
        TODO("Not yet implemented")
    }

    override suspend fun getBooksOnDeck(
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun getDuplicateBooks(pageRequest: KomgaPageRequest?): Page<KomgaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun getLatestBooks(pageRequest: KomgaPageRequest?): Page<KomgaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun markReadProgress(bookId: KomgaBookId, request: KomgaBookReadProgressUpdateRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun refreshMetadata(bookId: KomgaBookId) {
    }

    override suspend fun regenerateThumbnails(forBiggerResultOnly: Boolean) {
    }

    override suspend fun selectBookThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId) {
    }

    override suspend fun <T> streamBookPage(
        bookId: KomgaBookId,
        page: Int,
        block: suspend (response: HttpResponse) -> T
    ): T {
        TODO("Not yet implemented")
    }

    override suspend fun updateMetadata(bookId: KomgaBookId, request: KomgaBookMetadataUpdateRequest) {
    }

    override suspend fun uploadBookThumbnail(
        bookId: KomgaBookId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaBookThumbnail {
        TODO("Not yet implemented")
    }
}