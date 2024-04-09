package io.github.snd_r.komga.book

import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.common.KomgaThumbnailId
import io.github.snd_r.komga.common.Page
import io.github.snd_r.komga.common.toParams
import io.github.snd_r.komga.library.KomgaLibraryId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*


class KomgaBookClient(private val ktor: HttpClient) {

    suspend fun getBook(bookId: KomgaBookId): KomgaBook {
        return ktor.get("api/v1/books/$bookId").body()
    }

    suspend fun getAllBooks(
        query: KomgaBookQuery? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaBook> {
        val params = ParametersBuilder().apply {
            query?.searchTerm?.let { append("search", it) }
            query?.libraryIds?.let { if (it.isNotEmpty()) append("library_id", it.joinToString()) }
            query?.mediaStatus?.let { append("media_status", it.joinToString()) }
            query?.readStatus?.let { append("read_status", it.joinToString()) }
            query?.releasedAfter?.let { append("released_after", it.toString()) }
            query?.tags?.let { append("tag", it.joinToString()) }
            pageRequest?.let { appendAll(it.toParams()) }
        }.build()

        return ktor.get {
            url {
                path("api/v1/books")
                parameters.appendAll(params)
            }
        }.body()

    }

    suspend fun getLatestBooks(pageRequest: KomgaPageRequest? = null): Page<KomgaBook> {
        return ktor.get {
            url {
                path("api/v1/books/latest")
                pageRequest?.let { parameters.appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getBooksOnDeck(
        libraryIds: List<KomgaLibraryId>? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaBook> {
        return ktor.get {
            url {
                path("api/v1/books/ondeck")
                pageRequest?.let { parameters.appendAll(it.toParams()) }
                libraryIds?.let { parameters.append("library_id", it.joinToString()) }
            }
        }.body()
    }

    suspend fun getDuplicateBooks(pageRequest: KomgaPageRequest? = null): Page<KomgaBook> {
        return ktor.get {
            url {
                path("api/v1/books/duplicates")
                pageRequest?.let { parameters.appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getBookSiblingPrevious(bookId: KomgaBookId): KomgaBook {
        return ktor.get("api/v1/books/$bookId/previous").body()
    }

    suspend fun getBookSiblingNext(bookId: KomgaBookId): KomgaBook {
        return ktor.get("api/v1/books/$bookId/next").body()
    }

    suspend fun updateMetadata(bookId: KomgaBookId, request: KomgaBookMetadataUpdateRequest) {
        ktor.patch("api/v1/books/$bookId/metadata") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getBookPages(bookId: KomgaBookId): List<KomgaBookPage> {
        return ktor.get("api/v1/books/$bookId/pages").body()
    }

    suspend fun analyze(bookId: KomgaBookId) {
        ktor.post("api/v1/books/$bookId/analyze")
    }

    suspend fun refreshMetadata(bookId: KomgaBookId) {
        ktor.post("api/v1/books/$bookId/metadata/refresh")
    }

    suspend fun markReadProgress(bookId: KomgaBookId, request: KomgaBookReadProgressUpdateRequest) {
        ktor.patch("api/v1/books/$bookId/read-progress") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteReadProgress(bookId: KomgaBookId) {
        ktor.delete("api/v1/books/$bookId/read-progress")
    }

    suspend fun deleteBook(bookId: KomgaBookId) {
        ktor.delete("api/v1/books/$bookId/file")
    }

    suspend fun regenerateThumbnails(forBiggerResultOnly: Boolean) {
        ktor.put("/api/v1/books/thumbnails") {
            parameter("for_bigger_result_only", forBiggerResultOnly)
        }
    }

    suspend fun getBookThumbnails(bookId: KomgaBookId): List<KomgaBookThumbnail> {
        return ktor.get("api/v1/books/$bookId/thumbnails").body()
    }

    suspend fun uploadBookThumbnail(
        bookId: KomgaBookId,
        file: ByteArray,
        filename: String = "",
        selected: Boolean = true
    ): KomgaBookThumbnail {
        return ktor.post("api/v1/books/$bookId/thumbnails") {
            contentType(ContentType.MultiPart.FormData)
            setBody(
                MultiPartFormDataContent(formData {
                    append("file", file, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    })
                    append("selected", selected)
                })
            )
        }.body()
    }

    suspend fun selectBookThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId) {
        ktor.put("api/v1/books/$bookId/thumbnails/$thumbnailId/selected")
    }

    suspend fun deleteBookThumbnail(bookId: KomgaBookId, thumbnailId: KomgaThumbnailId) {
        ktor.delete("api/v1/books/$bookId/thumbnails/$thumbnailId")
    }
}