package snd.komelia.komga.api

import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadataUpdateRequest
import snd.komga.client.book.KomgaBookPage
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.book.KomgaBookThumbnail
import snd.komga.client.book.R2Positions
import snd.komga.client.book.R2Progression
import snd.komga.client.book.WPPublication
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.search.BookConditionBuilder

interface KomgaBookApi {
    suspend fun getOne(bookId: KomgaBookId): KomeliaBook

    suspend fun getBookList(
        conditionBuilder: BookConditionBuilder,
        fullTextSearch: String? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaBook>

    suspend fun getBookList(
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaBook>

    suspend fun getLatestBooks(pageRequest: KomgaPageRequest? = null): Page<KomeliaBook>
    suspend fun getBooksOnDeck(
        libraryIds: List<KomgaLibraryId>? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomeliaBook>

    suspend fun getDuplicateBooks(pageRequest: KomgaPageRequest? = null): Page<KomeliaBook>
    suspend fun getBookSiblingPrevious(bookId: KomgaBookId): KomeliaBook?
    suspend fun getBookSiblingNext(bookId: KomgaBookId): KomeliaBook?
    suspend fun updateMetadata(bookId: KomgaBookId, request: KomgaBookMetadataUpdateRequest)
    suspend fun getBookPages(bookId: KomgaBookId): List<KomgaBookPage>
    suspend fun analyze(bookId: KomgaBookId)
    suspend fun refreshMetadata(bookId: KomgaBookId)
    suspend fun markReadProgress(bookId: KomgaBookId, request: KomgaBookReadProgressUpdateRequest)
    suspend fun deleteReadProgress(bookId: KomgaBookId)
    suspend fun deleteBook(bookId: KomgaBookId)
    suspend fun regenerateThumbnails(forBiggerResultOnly: Boolean)
    suspend fun getDefaultThumbnail(bookId: KomgaBookId): ByteArray?
    suspend fun getThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId): ByteArray
    suspend fun getThumbnails(bookId: KomgaBookId): List<KomgaBookThumbnail>
    suspend fun uploadThumbnail(
        bookId: KomgaBookId,
        file: ByteArray,
        filename: String = "",
        selected: Boolean = true
    ): KomgaBookThumbnail

    suspend fun selectBookThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId)
    suspend fun deleteBookThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId)
    suspend fun getAllReadListsByBook(bookId: KomgaBookId): List<KomgaReadList>
    suspend fun getPage(bookId: KomgaBookId, page: Int): ByteArray
    suspend fun getPageThumbnail(bookId: KomgaBookId, page: Int): ByteArray

    suspend fun getReadiumProgression(bookId: KomgaBookId): R2Progression?
    suspend fun updateReadiumProgression(bookId: KomgaBookId, progression: R2Progression)
    suspend fun getReadiumPositions(bookId: KomgaBookId): R2Positions

    suspend fun getWebPubManifest(bookId: KomgaBookId): WPPublication

    suspend fun getBookEpubResource(bookId: KomgaBookId, resourceName: String): ByteArray
}