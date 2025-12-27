package snd.komelia.komga.api

import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId

interface KomgaReferentialApi {
    suspend fun getAuthors(
        search: String? = null,
        role: String? = null,
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null,
        seriesId: KomgaSeriesId? = null,
        readListId: KomgaReadListId? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaAuthor>

    suspend fun getAuthorsNames(search: String? = null): List<String>

    suspend fun getAuthorsRoles(): List<String>

    suspend fun getGenres(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getSharingLabels(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getTags(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getBookTags(
        seriesId: KomgaSeriesId? = null,
        readListId: KomgaReadListId? = null,
        libraryIds: List<KomgaLibraryId> = emptyList()
    ): List<String>

    suspend fun getSeriesTags(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getLanguages(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getPublishers(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getAgeRatings(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>

    suspend fun getSeriesReleaseDates(
        libraryIds: List<KomgaLibraryId> = emptyList(),
        collectionId: KomgaCollectionId? = null
    ): List<String>
}