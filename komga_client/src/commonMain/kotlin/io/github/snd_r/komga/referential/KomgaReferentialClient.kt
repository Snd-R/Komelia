package io.github.snd_r.komga.referential

import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.common.Page
import io.github.snd_r.komga.common.toParams
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.readlist.KomgaReadListId
import io.github.snd_r.komga.series.KomgaSeriesId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class KomgaReferentialClient internal constructor(private val ktor: HttpClient) {
    suspend fun getAuthors(
        search: String? = null,
        role: String? = null,
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null,
        seriesId: KomgaSeriesId? = null,
        readListId: KomgaReadListId? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaAuthor> {
        return ktor.get("api/v2/authors") {
            url.parameters.apply {
                search?.let { append("search", it) }
                role?.let { append("role", it) }
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
                seriesId?.let { append("series_id", it.value) }
                readListId?.let { append("readlist_id", it.value) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getAuthorsNames(search: String? = null): List<String> {
        return ktor.get("api/v1/authors/names") {
            url.parameters.apply {
                search?.let { append("search", it) }
            }
        }.body()
    }

    suspend fun getAuthorsRoles(): List<String> {
        return ktor.get("api/v1/authors/roles").body()
    }

    suspend fun getGenres(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/genres") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getSharingLabels(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/sharing-labels") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getTags(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/tags") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getBookTags(
        seriesId: KomgaSeriesId? = null,
        readListId: KomgaReadListId? = null
    ): List<String> {
        return ktor.get("api/v1/tags/book") {
            url.parameters.apply {
                seriesId?.let { append("series_id", it.value) }
                readListId?.let { append("readlist_id", it.value) }
            }
        }.body()
    }

    suspend fun getSeriesTags(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/tags/series") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getLanguages(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/languages") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getPublishers(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/publishers") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getAgeRatings(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/age-ratings") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }

    suspend fun getSeriesReleaseDates(
        libraryId: KomgaLibraryId? = null,
        collectionId: KomgaCollectionId? = null
    ): List<String> {
        return ktor.get("api/v1/series/release-dates") {
            url.parameters.apply {
                libraryId?.let { append("library_id", it.value) }
                collectionId?.let { append("collection_id", it.value) }
            }
        }.body()
    }
}
