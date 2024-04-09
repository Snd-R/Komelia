package io.github.snd_r.komga.library

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KomgaLibraryClient(private val ktor: HttpClient) {

    suspend fun getLibraries(): List<KomgaLibrary> {
        return ktor.get("api/v1/libraries").body()
    }

    suspend fun getLibrary(libraryId: KomgaLibraryId): KomgaLibrary {
        return ktor.get("api/v1/libraries/$libraryId").body()
    }

    suspend fun adOne(request: KomgaLibraryCreateRequest): KomgaLibrary {
        return ktor.post("api/v1/libraries") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun patchOne(libraryId: KomgaLibraryId, request: KomgaLibraryUpdateRequest) {
        ktor.patch("api/v1/libraries/$libraryId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteOne(libraryId: KomgaLibraryId) {
        ktor.delete("api/v1/libraries/$libraryId")
    }

    suspend fun scan(libraryId: KomgaLibraryId, deep: Boolean = false) {
        ktor.post("api/v1/libraries/$libraryId/scan") {
            parameter("deep", deep)
        }
    }

    suspend fun analyze(libraryId: KomgaLibraryId) {
        ktor.post("api/v1/libraries/$libraryId/analyze")
    }

    suspend fun refreshMetadata(libraryId: KomgaLibraryId) {
        ktor.post("api/v1/libraries/$libraryId/metadata/refresh")
    }

    suspend fun emptyTrash(libraryId: KomgaLibraryId) {
        ktor.post("api/v1/libraries/$libraryId/empty-trash")
    }
}