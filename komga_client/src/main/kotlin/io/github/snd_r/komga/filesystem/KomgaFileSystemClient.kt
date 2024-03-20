package io.github.snd_r.komga.filesystem

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KomgaFileSystemClient(private val ktor: HttpClient) {

    suspend fun getDirectoryListing(request: DirectoryRequest): DirectoryListing {
        return ktor.post("api/v1/filesystem") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}