package io.github.snd_r.komga.collection

import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.common.Page
import io.github.snd_r.komga.common.toParams
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.series.KomgaSeries
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KomgaCollectionClient internal constructor(private val ktor: HttpClient) {

    suspend fun getAll(
        search: String? = null,
        libraryIds: List<KomgaLibraryId>? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaCollection> {
        return ktor.get("api/v1/collections") {
            url.parameters.apply {
                search?.let { append("search", it) }
                libraryIds?.let { ids -> if (ids.isNotEmpty()) append("library_id", ids.joinToString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getOne(id: KomgaCollectionId): KomgaCollection {
        return ktor.get("api/v1/collections/$id").body()
    }

    suspend fun addOne(request: KomgaCollectionCreateRequest): KomgaCollection {
        return ktor.post("api/v1/collections") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateOne(id: KomgaCollectionId, request: KomgaCollectionUpdateRequest) {
        ktor.patch("api/v1/collections/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteOne(id: KomgaCollectionId) {
        ktor.delete("api/v1/collections/$id")
    }

    suspend fun getSeriesForCollection(
        id: KomgaCollectionId,
        query: KomgaCollectionQuery? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaSeries> {
        return ktor.get("api/v1/collections/$id/series") {
            url.parameters.apply {
                query?.libraryIds?.let { append("library_id", it.joinToString()) }
                query?.readStatus?.let { append("read_status", it.joinToString()) }
                query?.status?.let { append("status", it.joinToString()) }
                query?.languages?.let { append("language", it.joinToString()) }
                query?.publishers?.let { append("publisher", it.joinToString()) }
                query?.tags?.let { append("tag", it.joinToString()) }
                query?.genres?.let { append("genre", it.joinToString()) }
                query?.ageRatings?.let { append("age_rating", it.joinToString()) }
                query?.releaseYears?.let { append("release_year", it.joinToString()) }
                query?.authors?.let { authors ->
                    append("authors", authors.joinToString { "${it.name},${it.role}" })
                }
                query?.complete?.let { append("complete", it.toString()) }
                query?.deleted?.let { append("deleted", it.toString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()

    }
}