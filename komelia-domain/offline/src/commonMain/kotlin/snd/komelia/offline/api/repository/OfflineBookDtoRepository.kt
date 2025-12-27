package snd.komelia.offline.api.repository

import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.user.KomgaUserId

interface OfflineBookDtoRepository {
    suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest,
    ): Page<KomeliaBook>

    suspend fun findAll(
        userId: KomgaUserId,
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest,
    ): Page<KomeliaBook>

    suspend fun get(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): KomeliaBook

    suspend fun findByIdOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): KomeliaBook?

    suspend fun findPreviousInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): KomeliaBook?

    suspend fun findNextInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): KomeliaBook?

//    fun findPreviousInReadListOrNull(
//        readList: ReadList,
//        bookId: String,
//        userId: String,
//        filterOnLibraryIds: Collection<String>?,
////        restrictions: ContentRestrictions = ContentRestrictions(),
//    ): KomeliaBook?
//
//    fun findNextInReadListOrNull(
//        readList: ReadList,
//        bookId: String,
//        userId: String,
//        filterOnLibraryIds: Collection<String>?,
//        restrictions: ContentRestrictions = ContentRestrictions(),
//    ): BookDto?

    suspend fun findAllOnDeck(
        userId: KomgaUserId,
        filterOnLibraryIds: Collection<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest,
//        restrictions: ContentRestrictions = ContentRestrictions(),
    ): Page<KomeliaBook>

//    fun findAllDuplicates(
//        userId: KomgaUserId,
//        pageable: KomgaPageRequest,
//    ): Page<KomeliaBook>

}