package io.github.snd_r.komelia

import ch.qos.logback.classic.Level
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.VipsDecoder
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.image.VipsImageDecoder
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaBookThumbnailMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionThumbnailMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListThumbnailMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.settings.ActorMessage
import io.github.snd_r.komelia.settings.FileSystemSettingsActor
import io.github.snd_r.komelia.settings.FilesystemSettingsRepository
import io.github.snd_r.komelia.settings.KeyringSecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.cookies.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

actual suspend fun createViewModelFactory(
    scope: CoroutineScope,
): ViewModelFactory {
    setLogLevel()
    VipsDecoder.load()

    val settingsRepository = createSettingsRepository()
    val secretsRepository = KeyringSecretsRepository()

    val baseUrl = settingsRepository.getServerUrl().stateIn(scope)

    val okHttpClient = createOkHttpClient()
    val cookiesStorage = RememberMePersistingCookieStore(baseUrl, secretsRepository)
    val ktorClient = createKtorClient(baseUrl, okHttpClient, cookiesStorage)
    val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient, okHttpClient, cookiesStorage)

    val coil = createCoil(baseUrl, ktorClient)
    SingletonImageLoader.setSafe { coil }

    return ViewModelFactory(
        komgaClientFactory = komgaClientFactory,
//        komgaEventSource = komgaClientFactory.komgaEventSource(),
        settingsRepository = settingsRepository,
        secretsRepository = secretsRepository,
        imageLoader = coil,
        imageLoaderContext = PlatformContext.INSTANCE
    )
}

private fun createOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor { KotlinLogging.logger("http.logging").info { it } }
        .setLevel(HttpLoggingInterceptor.Level.BASIC)
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()
}

private fun createKtorClient(
    baseUrl: StateFlow<String>,
    okHttpClient: OkHttpClient,
    cookiesStorage: RememberMePersistingCookieStore,
): HttpClient {
    return HttpClient(OkHttp) {
        engine { preconfigured = okHttpClient }
        defaultRequest { url(baseUrl.value) }
        install(HttpCookies) { storage = cookiesStorage }

//        install(Logging) {
//            logger = Logger.DEFAULT
//            level = LogLevel.INFO
//        }
        expectSuccess = true
    }
}

private fun createKomgaClientFactory(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
    okHttpClient: OkHttpClient,
    cookiesStorage: RememberMePersistingCookieStore,
): KomgaClientFactory {

    val tempDir = Path(System.getProperty("java.io.tmpdir")).resolve("potato_http").createDirectories()
    val ktorKomgaClient = ktorClient.config {
        install(HttpCache) {
            privateStorage(FileStorage(tempDir.toFile()))
            publicStorage(FileStorage(tempDir.toFile()))
        }
    }

    return KomgaClientFactory.Builder()
        .ktor(ktorKomgaClient)
        .okHttp(okHttpClient)
        .baseUrl { baseUrl.value }
        .cookieStorage(cookiesStorage)
        .build()
}

private fun createCoil(url: StateFlow<String>, ktorClient: HttpClient): ImageLoader {

    return ImageLoader.Builder(PlatformContext.INSTANCE)
        .components {
            add(KomgaBookPageMapper(url))
            add(KomgaSeriesThumbnailMapper(url))
            add(KomgaBookThumbnailMapper(url))
            add(KomgaCollectionThumbnailMapper(url))
            add(KomgaReadListThumbnailMapper(url))
//            add(DesktopImageDecoder.Factory())
            add(VipsImageDecoder.Factory())
            add(KtorNetworkFetcherFactory(httpClient = ktorClient))
        }
        .memoryCache(
            MemoryCache.Builder()
                .maxSizeBytes(128 * 1024 * 1024) // 128 Mib
                .build()
        )
        .build()
}

private suspend fun createSettingsRepository(): SettingsRepository {
    val settingsProcessingActor = FileSystemSettingsActor()
    val ack = CompletableDeferred<Unit>()
    settingsProcessingActor.send(ActorMessage.Read(ack))
    ack.await()

    return FilesystemSettingsRepository(settingsProcessingActor)
}

private fun setLogLevel() {
    val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
    rootLogger.level = Level.INFO
    (LoggerFactory.getLogger("org.freedesktop") as ch.qos.logback.classic.Logger).level = Level.WARN

}