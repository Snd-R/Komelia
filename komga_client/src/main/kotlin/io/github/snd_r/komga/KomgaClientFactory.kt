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
import io.github.snd_r.komga.sse.KomgaEventSource
import io.github.snd_r.komga.sse.KtorCookieJarWrapper
import io.github.snd_r.komga.sse.KtorKomgaEventSource
import io.github.snd_r.komga.sse.OkHttpKomgaEventSource
import io.github.snd_r.komga.task.KomgaTaskClient
import io.github.snd_r.komga.user.KomgaUserClient
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.sse.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class KomgaClientFactory private constructor(builder: Builder) {

    private val json = Json(builder.json) {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    private val baseUrl: () -> String = builder.baseUrl

    private val ktor: HttpClient = builder.ktor.config {
        val cookiesStorage = builder.cookieStorage
        if (cookiesStorage != null) {
            install(HttpCookies) { storage = cookiesStorage }
        }

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

    private val ktorSse: HttpClient = ktor.config { install(SSE) }

    private val okHttpClientSSEClient: OkHttpClient? = builder.okHttpClient?.newBuilder()
        ?.readTimeout(0, TimeUnit.SECONDS)
        ?.cookieJar(builder.cookieStorage?.let { KtorCookieJarWrapper(it) } ?: CookieJar.NO_COOKIES)
        ?.build()

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

    fun komgaEventSource(): KomgaEventSource {
        // TODO Use Ktor SSE implementation
        if (ktor.engine is OkHttpEngine) {
            requireNotNull(okHttpClientSSEClient)
            return OkHttpKomgaEventSource(okHttpClientSSEClient, json, baseUrl)
        }

        return KtorKomgaEventSource(ktorSse, json)
    }

    class Builder {
        internal var ktor: HttpClient = HttpClient()
        internal var baseUrl: () -> String = { "http://localhost:25600/" }
        internal var cookieStorage: CookiesStorage? = null
        internal var json: Json = Json

        internal var username: String? = null
        internal var password: String? = null

        //TODO remove when sse is switched to ktor
        // required for sse connection
        internal var okHttpClient: OkHttpClient? = null

        fun ktor(ktor: HttpClient) = apply {
            this.ktor = ktor
        }

        fun okHttp(okHttp: OkHttpClient) = apply {
            this.okHttpClient = okHttp
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
