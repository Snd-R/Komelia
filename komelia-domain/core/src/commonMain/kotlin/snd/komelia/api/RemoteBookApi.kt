package snd.komelia.api

import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
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

class RemoteBookApi(
    private val bookClient: KomgaBookClient,
    private val offlineBookRepository: OfflineBookRepository,
) : KomgaBookApi {
    override suspend fun getOne(bookId: KomgaBookId): KomeliaBook {
        val book = bookClient.getOne(bookId)
        return getKomeliaBook(book)
    }

    override suspend fun getBookList(
        conditionBuilder: BookConditionBuilder,
        fullTextSearch: String?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> {
        val booksPage = bookClient.getBookList(conditionBuilder, fullTextSearch, pageRequest)
        return getKomeliaBookPage(booksPage)
    }

    override suspend fun getBookList(
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> {
        val booksPage = bookClient.getBookList(search, pageRequest)
        return getKomeliaBookPage(booksPage)
    }

    override suspend fun getLatestBooks(pageRequest: KomgaPageRequest?): Page<KomeliaBook> {
        val booksPage = bookClient.getLatestBooks(pageRequest)
        return getKomeliaBookPage(booksPage)
    }

    override suspend fun getBooksOnDeck(
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomeliaBook> {
        val booksPage = bookClient.getLatestBooks(pageRequest)
        return getKomeliaBookPage(booksPage)
    }

    override suspend fun getDuplicateBooks(pageRequest: KomgaPageRequest?): Page<KomeliaBook> {
        val booksPage = bookClient.getDuplicateBooks(pageRequest)
        return getKomeliaBookPage(booksPage)
    }

    override suspend fun getBookSiblingPrevious(bookId: KomgaBookId): KomeliaBook? {
        val book = bookClient.getBookSiblingPrevious(bookId)
        return book?.let { getKomeliaBook(it) }

    }

    override suspend fun getBookSiblingNext(bookId: KomgaBookId): KomeliaBook? {
        val book = bookClient.getBookSiblingNext(bookId)
        return book?.let { getKomeliaBook(it) }
    }

    override suspend fun updateMetadata(
        bookId: KomgaBookId,
        request: KomgaBookMetadataUpdateRequest
    ) {
        bookClient.updateMetadata(bookId, request)
    }

    override suspend fun getBookPages(bookId: KomgaBookId): List<KomgaBookPage> {
        return bookClient.getBookPages(bookId)
    }

    override suspend fun analyze(bookId: KomgaBookId) {
        bookClient.analyze(bookId)
    }

    override suspend fun refreshMetadata(bookId: KomgaBookId) {
        bookClient.refreshMetadata(bookId)
    }

    override suspend fun markReadProgress(
        bookId: KomgaBookId,
        request: KomgaBookReadProgressUpdateRequest
    ) {
        bookClient.markReadProgress(bookId, request)
    }

    override suspend fun deleteReadProgress(bookId: KomgaBookId) {
        bookClient.deleteReadProgress(bookId)
    }

    override suspend fun deleteBook(bookId: KomgaBookId) {
        bookClient.deleteBook(bookId)
    }

    override suspend fun regenerateThumbnails(forBiggerResultOnly: Boolean) {
        bookClient.regenerateThumbnails(forBiggerResultOnly)
    }

    override suspend fun getDefaultThumbnail(bookId: KomgaBookId): ByteArray? {
        return bookClient.getDefaultThumbnail(bookId)
    }

    override suspend fun getThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId) =
        bookClient.getThumbnail(bookId, thumbnailId)

    override suspend fun getThumbnails(bookId: KomgaBookId): List<KomgaBookThumbnail> {
        return bookClient.getThumbnails(bookId)
    }

    override suspend fun uploadThumbnail(
        bookId: KomgaBookId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaBookThumbnail {
        return bookClient.uploadThumbnail(
            bookId = bookId,
            file = file,
            filename = filename,
            selected = selected
        )
    }

    override suspend fun selectBookThumbnail(
        bookId: KomgaBookId,
        thumbnailId: KomgaThumbnailId
    ) {
        bookClient.selectThumbnail(bookId, thumbnailId)
    }

    override suspend fun deleteBookThumbnail(
        bookId: KomgaBookId,
        thumbnailId: KomgaThumbnailId
    ) {
        bookClient.deleteThumbnail(bookId, thumbnailId)
    }

    override suspend fun getAllReadListsByBook(bookId: KomgaBookId): List<KomgaReadList> {
        return bookClient.getAllReadListsByBook(bookId)
    }

    override suspend fun getPage(bookId: KomgaBookId, page: Int): ByteArray {
        return bookClient.getPage(bookId, page)
    }

    override suspend fun getPageThumbnail(
        bookId: KomgaBookId,
        page: Int
    ): ByteArray {
        return bookClient.getPageThumbnail(bookId, page)
    }

    override suspend fun getReadiumProgression(bookId: KomgaBookId): R2Progression? {
        return bookClient.getReadiumProgression(bookId)
    }

    override suspend fun updateReadiumProgression(
        bookId: KomgaBookId,
        progression: R2Progression
    ) {
        bookClient.updateReadiumProgression(bookId, progression)
    }

    override suspend fun getReadiumPositions(bookId: KomgaBookId): R2Positions {
        return bookClient.getReadiumPositions(bookId)
    }

    override suspend fun getWebPubManifest(bookId: KomgaBookId): WPPublication {
        return bookClient.getWebPubManifest(bookId)
    }

    override suspend fun getBookEpubResource(
        bookId: KomgaBookId,
        resourceName: String
    ): ByteArray {
        return bookClient.getBookEpubResource(bookId, resourceName)
    }

    private suspend fun getKomeliaBook(book: KomgaBook): KomeliaBook {
        val offlineBook = offlineBookRepository.find(book.id)
        return KomeliaBook(
            book = book,
            downloaded = offlineBook != null,
            localFileLastModified = offlineBook?.localFileLastModified,
            remoteFileUnavailable = offlineBook?.remoteUnavailable ?: false
        )
    }

    private suspend fun getKomeliaBookPage(bookPage: Page<KomgaBook>): Page<KomeliaBook> {
        val ids = bookPage.content.map { it.id }
        val offlineBooks = offlineBookRepository.findIn(ids).associateBy { it.id }
        val komeliaBooks = bookPage.content.map {
            val offlineBook = offlineBooks[it.id]
            KomeliaBook(
                book = it,
                downloaded = offlineBook != null,
                localFileLastModified = offlineBook?.localFileLastModified,
                remoteFileUnavailable = offlineBook?.remoteUnavailable ?: false
            )
        }
        return bookPage.toKomeliaBookPage(komeliaBooks)
    }
}

fun Page<KomgaBook>.toKomeliaBookPage(books: List<KomeliaBook>): Page<KomeliaBook> {
    return Page(
        content = books,
        pageable = this.pageable,
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        last = this.last,
        number = this.number,
        sort = this.sort,
        first = this.first,
        numberOfElements = this.numberOfElements,
        size = this.size,
        empty = this.empty
    )
}
