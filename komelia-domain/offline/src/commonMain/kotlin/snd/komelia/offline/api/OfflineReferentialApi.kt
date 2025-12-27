package snd.komelia.offline.api

import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.offline.api.repository.OfflineReferentialRepository
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId

class OfflineReferentialApi(
    private val referentialRepository: OfflineReferentialRepository
) : KomgaReferentialApi {
    override suspend fun getAuthors(
        search: String?,
        role: String?,
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?,
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaAuthor> {
        val pageRequest = pageRequest ?: KomgaPageRequest()
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllAuthorsByNameAndLibraries(
                search,
                role,
                libraryIds,
                pageRequest
            )

            collectionId != null -> referentialRepository.findAllAuthorsByNameAndCollection(
                search,
                role,
                collectionId,
                pageRequest
            )

            seriesId != null -> referentialRepository.findAllAuthorsByNameAndSeries(
                search,
                role,
                seriesId,
                pageRequest
            )

            readListId != null -> referentialRepository.findAllAuthorsByNameAndReadList(
                search,
                role,
                readListId,
                pageRequest
            )

            else -> referentialRepository.findAllAuthorsByName(
                search,
                role,
                pageRequest
            )
        }
    }

    override suspend fun getAuthorsNames(search: String?): List<String> {
        return referentialRepository.findAllAuthorsNamesByName(search ?: "")
    }

    override suspend fun getAuthorsRoles(): List<String> {
        return referentialRepository.findAllAuthorsRoles()
    }

    override suspend fun getGenres(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllGenresByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllGenresByCollection(collectionId)
            else -> referentialRepository.findAllGenres()
        }
    }

    override suspend fun getSharingLabels(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllSharingLabelsByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllSharingLabelsByCollection(collectionId)
            else -> referentialRepository.findAllSharingLabels()
        }
    }

    override suspend fun getTags(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllSeriesAndBookTagsByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllSeriesAndBookTagsByCollection(collectionId)
            else -> referentialRepository.findAllSeriesAndBookTags()
        }
    }

    override suspend fun getBookTags(
        seriesId: KomgaSeriesId?,
        readListId: KomgaReadListId?,
        libraryIds: List<KomgaLibraryId>
    ): List<String> {
        return when {
            seriesId != null -> referentialRepository.findAllBookTagsBySeries(seriesId)
            readListId != null -> referentialRepository.findAllBookTagsByReadList(readListId)
            libraryIds.isNotEmpty() -> referentialRepository.findAllBookTags()
            else -> referentialRepository.findAllBookTags()
        }
    }

    override suspend fun getSeriesTags(
        libraryId: KomgaLibraryId?,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryId != null -> referentialRepository.findAllSeriesTagsByLibrary(libraryId)
            collectionId != null -> referentialRepository.findAllSeriesTagsByCollection(collectionId)
            else -> referentialRepository.findAllSeriesTags()
        }
    }

    override suspend fun getLanguages(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllLanguagesByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllLanguagesByCollection(collectionId)
            else -> referentialRepository.findAllLanguages()
        }
    }

    override suspend fun getPublishers(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllPublishersByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllPublishersByCollection(collectionId)
            else -> referentialRepository.findAllPublishers()
        }
    }

    override suspend fun getAgeRatings(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllAgeRatingsByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllAgeRatingsByCollection(collectionId)
            else -> referentialRepository.findAllAgeRatings()
        }.map { it?.toString() ?: "None" }
    }

    override suspend fun getSeriesReleaseDates(
        libraryIds: List<KomgaLibraryId>,
        collectionId: KomgaCollectionId?
    ): List<String> {
        return when {
            libraryIds.isNotEmpty() -> referentialRepository.findAllSeriesReleaseDatesByLibraries(libraryIds)
            collectionId != null -> referentialRepository.findAllSeriesReleaseDatesByCollection(collectionId)
            else -> referentialRepository.findAllSeriesReleaseDates()
        }.map { it.year.toString() }
    }
}