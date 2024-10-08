package io.github.snd_r.komelia.offline.client

import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeriesId

class OfflineReferentialClient: KomgaReferentialClient {
    override suspend fun getAgeRatings(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthors(
        search: String?,
        role: String?,
        libraryId: KomgaLibraryId?,
        collectionId: KomgaCollectionId?,
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthorsNames(search: String?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthorsRoles(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getBookTags(seriesId: KomgaSeriesId?, readListId: KomgaReadListId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getGenres(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getLanguages(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getPublishers(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getSeriesReleaseDates(
        libraryId: KomgaLibraryId?,
        collectionId: KomgaCollectionId?
    ): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getSeriesTags(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getSharingLabels(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getTags(libraryId: KomgaLibraryId?, collectionId: KomgaCollectionId?): List<String> {
        TODO("Not yet implemented")
    }
}