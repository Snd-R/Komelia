package io.github.snd_r.komga

import io.github.snd_r.komga.actuator.KomgaActuatorClient
import io.github.snd_r.komga.announcements.KomgaAnnouncementsClient
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.collection.KomgaCollectionClient
import io.github.snd_r.komga.filesystem.KomgaFileSystemClient
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.readlist.KomgaReadListClient
import io.github.snd_r.komga.referential.KomgaReferentialClient
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.settings.KomgaSettingsClient
import io.github.snd_r.komga.sse.KomgaSSESession
import io.github.snd_r.komga.task.KomgaTaskClient
import io.github.snd_r.komga.user.KomgaUserClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class KomgaClientFactory private constructor(
    private val builder: Builder
) {

    private val json = Json(builder.json) {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    private val baseUrl: () -> String = builder.baseUrl
    private val ktor: HttpClient = configureKtor(builder.ktor ?: HttpClient())

    private fun configureKtor(client: HttpClient): HttpClient {
        return client.config {
//            val cookiesStorage = builder.cookieStorage
//            if (cookiesStorage != null) {
//                install(HttpCookies) { storage = cookiesStorage }
//            }

            install(HttpCookies)

            install(ContentNegotiation) { json(json) }

            defaultRequest { url(baseUrl()) }

            val username = builder.username
            val password = builder.password
            if (username != null && password != null) {
                install(Auth) {
                    basic {
                        credentials { BasicAuthCredentials(username = username, password = password) }
                        sendWithoutRequest { true }
                    }
                }
            }
            expectSuccess = true
        }
    }

    fun libraryClient() = KomgaLibraryClient(ktor)
    fun seriesClient() = KomgaSeriesClient(ktor)
    fun bookClient() = KomgaBookClient(ktor)
    fun userClient() = KomgaUserClient(ktor)
    fun fileSystemClient() = KomgaFileSystemClient(ktor)
    fun settingsClient() = KomgaSettingsClient(ktor)
    fun taskClient() = KomgaTaskClient(ktor)
    fun actuatorClient() = KomgaActuatorClient(ktor)
    fun announcementClient() = KomgaAnnouncementsClient(ktor)
    fun collectionClient() = KomgaCollectionClient(ktor)
    fun readListClient() = KomgaReadListClient(ktor)
    fun referentialClient() = KomgaReferentialClient(ktor)

    //FIXME ktor sse session implementation does NOT terminate sse connection on session context cancellation
    // using custom implementation as a workaround
    suspend fun sseSession(): KomgaSSESession {
//        val session = ktorSse.sseSession("sse/v1/events")
//        return KtorKomgaSSESession(json, session)

        val authCookie = ktor.cookies(Url(baseUrl()))
//            .get(Url(baseUrl()))
            .find { it.name == "SESSION" }
            ?.let { renderCookieHeader(it) }
            ?: ""
        return getSseSession(json, baseUrl(), authCookie)
    }

    class Builder {
        internal var ktor: HttpClient? = null
        internal var baseUrl: () -> String = { "http://localhost:25600/" }
        internal var cookieStorage: CookiesStorage? = null
        internal var json: Json = Json

        internal var username: String? = null
        internal var password: String? = null

        fun ktor(ktor: HttpClient) = apply {
            this.ktor = ktor
        }

        fun baseUrl(block: () -> String) = apply {
            this.baseUrl = block
        }

//        fun baseUrl(baseUrl: StateFlow<String>) = apply {
//            this.baseUrl = baseUrl
//        }

//        fun baseUrl(baseUrl: String) = apply {
//            this.baseUrl = { baseUrl }
//        }

        fun cookieStorage(cookiesStorage: CookiesStorage) = apply {
            this.cookieStorage = cookiesStorage
        }

        fun username(username: String) = apply {
            this.username = username
        }

        fun password(password: String) = apply {
            this.password = password
        }

        fun build(): KomgaClientFactory {
            return KomgaClientFactory(this)
        }

    }
}

internal expect suspend fun getSseSession(json: Json, baseUrl: String, authCookie: String): KomgaSSESession