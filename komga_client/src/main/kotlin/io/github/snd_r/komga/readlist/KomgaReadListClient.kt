package io.github.snd_r.komga.readlist

import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.collection.KomgaCollectionCreateRequest
import io.github.snd_r.komga.collection.KomgaCollectionUpdateRequest
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.common.Page
import io.github.snd_r.komga.common.toParams
import io.github.snd_r.komga.library.KomgaLibraryId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KomgaReadListClient internal constructor(private val ktor: HttpClient) {
    suspend fun getAll(
        search: String? = null,
        libraryIds: List<KomgaLibraryId>? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaReadList> {
        return ktor.get("api/v1/readlists") {
            url.parameters.apply {
                search?.let { append("search", it) }
                libraryIds?.let { ids -> if (ids.isNotEmpty()) append("library_id", ids.joinToString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getOne(id: KomgaReadListId): KomgaReadList {
        return ktor.get("api/v1/readlists/$id").body()
    }

    suspend fun addOne(request: KomgaCollectionCreateRequest): KomgaReadList {
        return ktor.post("api/v1/readlists") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateOne(id: KomgaReadListId, request: KomgaCollectionUpdateRequest) {
        ktor.patch("api/v1/readlists/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteOne(id: KomgaReadListId) {
        ktor.delete("api/v1/readlists/$id")
    }

    suspend fun getBooksForReadList(
        id: KomgaReadListId,
        query: KomgaReadListQuery? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaBook> {
        return ktor.get("api/v1/readlists/$id/books") {
            url.parameters.apply {
                query?.libraryIds?.let { append("library_id", it.joinToString()) }
                query?.readStatus?.let { append("read_status", it.joinToString()) }
                query?.tags?.let { append("tag", it.joinToString()) }
                query?.mediaStatus?.let { append("media_status", it.joinToString()) }
                query?.authors?.let { authors ->
                    append("authors", authors.joinToString { "${it.name},${it.role}" })
                }
                query?.deleted?.let { append("deleted", it.toString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }
}