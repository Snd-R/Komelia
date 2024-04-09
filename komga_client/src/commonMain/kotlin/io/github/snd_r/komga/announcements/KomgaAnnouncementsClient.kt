package io.github.snd_r.komga.announcements

import io.github.snd_r.komga.announcements.KomgaJsonFeed.KomgaAnnouncementId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KomgaAnnouncementsClient internal constructor(private val ktor: HttpClient) {

    suspend fun getAnnouncements(): KomgaJsonFeed {
        return ktor.get("api/v1/announcements").body()
    }

    suspend fun markAnnouncementsRead(announcements: List<KomgaAnnouncementId>) {
        ktor.put("api/v1/announcements") {
            contentType(ContentType.Application.Json)
            setBody(announcements)
        }
    }
}