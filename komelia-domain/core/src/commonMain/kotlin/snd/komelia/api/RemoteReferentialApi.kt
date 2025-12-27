package snd.komelia.api

import snd.komelia.komga.api.KomgaReferentialApi
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeriesId

class RemoteReferentialApi(private val referentialClient: KomgaReferentialClient) : KomgaReferentialApi {
    override suspend fun getAuthors(
        search: String?,
        role: String?,
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?,
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        pageRequest: KomgaPageRequest?
    ) = referentialClient.getAuthors(
        search = search,
        role = role,
        libraryIds = libraryIds,
        collectionId = collectionId,
        seriesId = seriesId,
        readListId = readListId,
        pageRequest = pageRequest
    )

    override suspend fun getAuthorsNames(search: String?) = referentialClient.getAuthorsNames(search)

    override suspend fun getAuthorsRoles() = referentialClient.getAuthorsRoles()

    override suspend fun getGenres(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getGenres(libraryIds, collectionId)

    override suspend fun getSharingLabels(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getSharingLabels(libraryIds, collectionId)

    override suspend fun getTags(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getTags(libraryIds, collectionId)

    override suspend fun getBookTags(
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        libraryIds: List<KomgaLibraryId>
    ) = referentialClient.getBookTags(seriesId, readListId, libraryIds)

    override suspend fun getSeriesTags(
        libraryId: KomgaLibraryId?,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getSeriesTags(libraryId, collectionId)

    override suspend fun getLanguages(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getLanguages(libraryIds, collectionId)

    override suspend fun getPublishers(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getPublishers(libraryIds, collectionId)

    override suspend fun getAgeRatings(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getAgeRatings(libraryIds, collectionId)

    override suspend fun getSeriesReleaseDates(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ) = referentialClient.getSeriesReleaseDates(libraryIds, collectionId)
}