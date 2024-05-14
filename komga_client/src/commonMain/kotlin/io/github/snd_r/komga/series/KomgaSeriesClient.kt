package io.github.snd_r.komga.series

import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.collection.KomgaCollection
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
                query?.libraryIds?.let {
                    if (it.isNotEmpty()) append("library_id", it.joinToString(","))
                }
                query?.collectionIds?.let {
                    if (it.isNotEmpty()) append("collection_id", it.joinToString(","))
                }
                query?.status?.let {
                    if (it.isNotEmpty()) append("status", it.joinToString(","))
                }
                query?.readStatus?.let {
                    if (it.isNotEmpty()) append("read_status", it.joinToString(","))
                }
                query?.publishers?.let {
                    if (it.isNotEmpty()) append("publisher", it.joinToString(","))
                }
                query?.languages?.let {
                    if (it.isNotEmpty()) append("language", it.joinToString(","))
                }
                query?.genres?.let {
                    if (it.isNotEmpty()) append("genre", it.joinToString(","))
                }
                query?.tags?.let {
                    if (it.isNotEmpty()) append("tag", it.joinToString(","))
                }
                query?.ageRatings?.let {
                    if (it.isNotEmpty()) append("age_rating", it.joinToString(","))
                }
                query?.releaseYears?.let {
                    if (it.isNotEmpty()) append("release_year", it.joinToString(","))
                }
                query?.sharingLabels?.let {
                    if (it.isNotEmpty()) append("sharing_label", it.joinToString(","))
                }
                query?.authors?.let { authors ->
                    if (authors.isNotEmpty())
                        authors.forEach { author -> append("author", "${author.name},${author.role}") }
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
        libraryIds: List<KomgaLibraryId>? = null,
        oneshot: Boolean? = null,
        deleted: Boolean? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaSeries> {
        return ktor.get("api/v1/series/new") {
            url.parameters.apply {
                libraryIds?.let { append("library_id", it.joinToString(",")) }
                oneshot?.let { append("oneshot", oneshot.toString()) }
                deleted?.let { append("deleted", deleted.toString()) }
                pageRequest?.let { appendAll(it.toParams()) }
            }
        }.body()
    }

    suspend fun getUpdatedSeries(
        libraryIds: List<KomgaLibraryId>? = null,
        oneshot: Boolean? = null,
        deleted: Boolean? = null,
        pageRequest: KomgaPageRequest? = null,
    ): Page<KomgaSeries> {
        return ktor.get("api/v1/series/updated") {
            url.parameters.apply {
                libraryIds?.let { append("library_id", it.joinToString(",")) }
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

    suspend fun getAllCollectionsBySeries(seriesId: KomgaSeriesId): List<KomgaCollection> {
        return ktor.get("api/v1/series/$seriesId/collections").body()
    }
}