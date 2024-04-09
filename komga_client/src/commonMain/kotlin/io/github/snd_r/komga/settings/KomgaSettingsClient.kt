package io.github.snd_r.komga.settings

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KomgaSettingsClient internal constructor(private val ktor: HttpClient) {

    suspend fun getSettings(): KomgaSettings {
        return ktor.get("/api/v1/settings").body()
    }

    suspend fun updateSettings(request: KomgaSettingsUpdateRequest) {
        ktor.patch("/api/v1/settings") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

}