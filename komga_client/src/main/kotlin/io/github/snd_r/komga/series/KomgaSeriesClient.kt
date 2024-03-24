package io.github.snd_r.komga.series

import io.github.snd_r.komga.book.KomgaBook
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

class KomgaSeriesClient(private val ktor: HttpClient) {


    suspend fun getOneSeries(seriesId: KomgaSeriesId): KomgaSeries {
        return ktor.get("api/v1/series/$seriesId").body()
    }

    suspend fun getAllSeries(
        query: KomgaSeriesQuery? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaSeries> {
        return ktor.get("api/v1/series") {
            url.parameters.apply {
                query?.searchTerm?.let { append("search", it) }
                query?.searchRegex?.let { append("search_regex", "${it.regex},${it.searchField}.}") }
                query?.libraryIds?.let { append("library_id", it.joinToString(",")) }
                query?.collectionIds?.let { append("collection_id", it.joinToString(",")) }
                query?.status?.let { append("status", it.joinToString(",")) }
                query?.readStatus?.let { append("read_status", it.joinToString(",")) }
                query?.publishers?.let { append("publisher", it.joinToString(",")) }
                query?.languages?.let { append("language", it.joinToString(",")) }
                query?.genres?.let { append("genre", it.joinToString(",")) }
                query?.tags?.let { append("tag", it.joinToString(",")) }
                query?.ageRatings?.let { append("age_rating", it.joinToString(",")) }
                query?.releaseYears?.let { append("release_year", it.joinToString(",")) }
                query?.sharingLabels?.let { append("sharing_label", it.joinToString(",")) }
                query?.authors?.let { authors ->
                    append("authors", authors.joinToString { "${it.name},${it.role}" })
                }
                query?.deleted?.let { append("deleted", it.toString()) }
                query?.complete?.let { append("complete", it.toString()) }
                query?.oneshot?.let { append("oneshot", it.toString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getBooks(
        seriesId: KomgaSeriesId,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaBook> {
        return ktor.get("api/v1/series/$seriesId/books") {
            url.parameters.apply { pageRequest?.let { appendAll(it.toParams()) } }
        }.body()
    }

    suspend fun getNewSeries(
        libraryId: KomgaLibraryId? = null,
        oneshot: Boolean? = null,
        deleted: Boolean? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaSeries> {
        return ktor.get("api/v1/series/new") {
            url.parameters.apply {
                libraryId?.let { append("library_id", libraryId.value) }
                oneshot?.let { append("oneshot", oneshot.toString()) }
                deleted?.let { append("deleted", deleted.toString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getUpdatedSeries(
        libraryId: KomgaLibraryId? = null,
        oneshot: Boolean? = null,
        deleted: Boolean? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaSeries> {
        return ktor.get("api/v1/series/updated") {
            url.parameters.apply {
                libraryId?.let { append("library_id", libraryId.value) }
                deleted?.let { append("deleted", deleted.toString()) }
                oneshot?.let { append("oneshot", oneshot.toString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun analyze(seriesId: KomgaSeriesId) {
        ktor.post("api/v1/series/$seriesId/analyze")
    }

    suspend fun refreshMetadata(seriesId: KomgaSeriesId) {
        ktor.post("api/v1/series/$seriesId/metadata/refresh")
    }

    suspend fun markAsRead(seriesId: KomgaSeriesId) {
        ktor.post("api/v1/series/$seriesId/read-progress")
    }

    suspend fun markAsUnread(seriesId: KomgaSeriesId) {
        ktor.delete("api/v1/series/$seriesId/read-progress")
    }

    suspend fun deleteSeries(seriesId: KomgaSeriesId) {
        ktor.delete("api/v1/series/$seriesId/file")
    }

    suspend fun updateSeries(seriesId: KomgaSeriesId, request: KomgaSeriesMetadataUpdateRequest) {
        ktor.patch("api/v1/series/$seriesId/metadata") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getSeriesThumbnails(seriesId: KomgaSeriesId): List<KomgaSeriesThumbnail> {
        return ktor.get("api/v1/series/$seriesId/thumbnails").body()
    }

    suspend fun uploadSeriesThumbnail(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        filename: String = "",
        selected: Boolean = true
    ): KomgaSeriesThumbnail {
        return ktor.post("api/v1/series/$seriesId/thumbnails") {
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

    suspend fun selectSeriesThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId) {
        ktor.put("api/v1/series/$seriesId/thumbnails/$thumbnailId/selected")
    }

    suspend fun deleteSeriesThumbnail(seriesId: KomgaSeriesId, thumbnailId: KomgaThumbnailId) {
        ktor.delete("api/v1/series/$seriesId/thumbnails/$thumbnailId")
    }
}