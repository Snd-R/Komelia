package snd.komelia.offline.api.repository

import kotlinx.datetime.LocalDate
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId

interface OfflineReferentialRepository {

    suspend fun findAllAuthorsByName(
        search: String,
    ): List<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndLibrary(
        search: String,
        libraryId: KomgaLibraryId,
    ): List<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndCollection(
        search: String,
        collectionId: KomgaCollectionId,
    ): List<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndSeries(
        search: String,
        seriesId: KomgaSeriesId,
    ): List<KomgaAuthor>

    suspend fun findAllAuthorsNamesByName(
        search: String,
    ): List<String>

    suspend fun findAllAuthorsRoles(): List<String>

    suspend fun findAllAuthorsByName(
        search: String?,
        role: String?,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndLibraries(
        search: String?,
        role: String?,
        libraryIds: List<KomgaLibraryId>,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndCollection(
        search: String?,
        role: String?,
        collectionId: KomgaCollectionId,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndSeries(
        search: String?,
        role: String?,
        seriesId: KomgaSeriesId,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaAuthor>

    suspend fun findAllAuthorsByNameAndReadList(
        search: String?,
        role: String?,
        readListId: KomgaReadListId,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaAuthor>

    suspend fun findAllGenres(): List<String>

    suspend fun findAllGenresByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<String>

    suspend fun findAllGenresByCollection(
        collectionId: KomgaCollectionId,
    ): List<String>

    suspend fun findAllSeriesAndBookTags(): List<String>

    suspend fun findAllSeriesAndBookTagsByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<String>

    suspend fun findAllSeriesAndBookTagsByCollection(
        collectionId: KomgaCollectionId,
    ): List<String>

    suspend fun findAllSeriesTags(): List<String>

    suspend fun findAllSeriesTagsByLibrary(
        libraryId: KomgaLibraryId,
    ): List<String>

    suspend fun findAllSeriesTagsByCollection(
        collectionId: KomgaCollectionId,
    ): List<String>

    suspend fun findAllBookTags(): List<String>

    suspend fun findAllBookTagsBySeries(
        seriesId: KomgaSeriesId,
    ): List<String>

    suspend fun findAllBookTagsByReadList(
        readListId: KomgaReadListId,
    ): List<String>

    suspend fun findAllLanguages(): List<String>

    suspend fun findAllLanguagesByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<String>

    suspend fun findAllLanguagesByCollection(
        collectionId: KomgaCollectionId,
    ): List<String>

    suspend fun findAllPublishers(): List<String>

    suspend fun findAllPublishers(
        pageable: KomgaPageRequest,
    ): Page<String>

    suspend fun findAllPublishersByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<String>

    suspend fun findAllPublishersByCollection(
        collectionId: KomgaCollectionId,
    ): List<String>

    suspend fun findAllAgeRatings(): List<Int?>

    suspend fun findAllAgeRatingsByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<Int?>

    suspend fun findAllAgeRatingsByCollection(
        collectionId: KomgaCollectionId,
    ): List<Int?>

    suspend fun findAllSeriesReleaseDates(): List<LocalDate>

    suspend fun findAllSeriesReleaseDatesByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<LocalDate>

    suspend fun findAllSeriesReleaseDatesByCollection(
        collectionId: KomgaCollectionId,
    ): List<LocalDate>

    suspend fun findAllSharingLabels(): List<String>

    suspend fun findAllSharingLabelsByLibraries(
        libraryIds: List<KomgaLibraryId>,
    ): List<String>

    suspend fun findAllSharingLabelsByCollection(
        collectionId: KomgaCollectionId,
    ): List<String>
}