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
import io.github.snd_r.komelia.image.DesktopDecoder
import io.github.snd_r.komelia.image.coil.FileMapper
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.settings.ActorMessage
import io.github.snd_r.komelia.settings.FileSystemSettingsActor
import io.github.snd_r.komelia.settings.FilesystemReaderSettingsRepository
import io.github.snd_r.komelia.settings.FilesystemSettingsRepository
import io.github.snd_r.komelia.settings.KeyringSecretsRepository
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.cookies.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}
private val stateFlowScope = CoroutineScope(Dispatchers.Default)
actual suspend fun createViewModelFactory(context: PlatformContext): ViewModelFactory {
    val initResult = measureTimedValue {
        setLogLevel()

        measureTime {
            try {
                VipsDecoder.load()
            } catch (e: UnsatisfiedLinkError) {
                logger.error(e) { "Couldn't load libvips. Vips decoder will not work" }
            }
        }.also {
            logger.info { "loaded vips in $it" }
        }


        val settingsActor = createSettingsActor()
        val settingsRepository = FilesystemSettingsRepository(settingsActor)
        val readerSettingsRepository = FilesystemReaderSettingsRepository(settingsActor)

        val secretsRepository = measureTimedValue {
            KeyringSecretsRepository()
        }.also {
            logger.info { "initialized keyring in ${it.duration}" }
        }.value

        val baseUrl = settingsRepository.getServerUrl().stateIn(stateFlowScope)
        val decoderType = settingsRepository.getDecoderType().stateIn(stateFlowScope)

        val okHttpClient = createOkHttpClient()
        val cookiesStorage = RememberMePersistingCookieStore(baseUrl, secretsRepository)

        measureTime {
            cookiesStorage.loadRememberMeCookie()
        }.also {
            logger.info { "loaded remember-me cookie from keyring in $it" }
        }

        val ktorClient = createKtorClient(baseUrl, okHttpClient, cookiesStorage)
        val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient, cookiesStorage)

        val coil = createCoil(baseUrl, ktorClient, decoderType)
        SingletonImageLoader.setSafe { coil }

        ViewModelFactory(
            komgaClientFactory = komgaClientFactory,
            settingsRepository = settingsRepository,
            readerSettingsRepository = readerSettingsRepository,
            secretsRepository = secretsRepository,
            imageLoader = coil,
            imageLoaderContext = context,
        )
    }

    logger.info { "completed initialization in ${initResult.duration}" }
    return initResult.value
}

private fun createOkHttpClient(): OkHttpClient {
    return measureTimedValue {
        val loggingInterceptor = HttpLoggingInterceptor { KotlinLogging.logger("http.logging").info { it } }
            .setLevel(HttpLoggingInterceptor.Level.BASIC)
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }.also { logger.info { "created OkHttp client in ${it.duration}" } }
        .value
}

private fun createKtorClient(
    baseUrl: StateFlow<String>,
    okHttpClient: OkHttpClient,
    cookiesStorage: RememberMePersistingCookieStore,
): HttpClient {
    return measureTimedValue {
        HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            defaultRequest { url(baseUrl.value) }
            install(HttpCookies) { storage = cookiesStorage }
            expectSuccess = true
        }
    }.also { logger.info { "initialized Ktor in ${it.duration}" } }
        .value
}

private fun createKomgaClientFactory(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
    cookiesStorage: RememberMePersistingCookieStore,
): KomgaClientFactory {
    return measureTimedValue {

        val tempDir = Path(System.getProperty("java.io.tmpdir")).resolve("potato_http").createDirectories()
        val ktorKomgaClient = ktorClient.config {
            install(HttpCache) {
                privateStorage(FileStorage(tempDir.toFile()))
                publicStorage(FileStorage(tempDir.toFile()))
            }
        }

        KomgaClientFactory.Builder()
            .ktor(ktorKomgaClient)
            .baseUrl { baseUrl.value }
            .cookieStorage(cookiesStorage)
            .build()
    }.also { logger.info { "created Komga client factory in ${it.duration}" } }
        .value
}

private fun createCoil(
    url: StateFlow<String>,
    ktorClient: HttpClient,
    decoderState: StateFlow<SamplerType>
): ImageLoader {

    return measureTimedValue {
        ImageLoader.Builder(PlatformContext.INSTANCE)
            .components {
                add(KomgaBookPageMapper(url))
                add(KomgaSeriesMapper(url))
                add(KomgaBookMapper(url))
                add(KomgaCollectionMapper(url))
                add(KomgaReadListMapper(url))
                add(KomgaSeriesThumbnailMapper(url))
                add(FileMapper())
                add(DesktopDecoder.Factory(decoderState))
                add(KtorNetworkFetcherFactory(httpClient = ktorClient))
            }
            .memoryCache(
                MemoryCache.Builder()
                    .maxSizeBytes(128 * 1024 * 1024) // 128 Mib
                    .build()
            )
            .build()
    }.also { logger.info { "initialized Coil in ${it.duration}" } }.value
}

private suspend fun createSettingsActor(): FileSystemSettingsActor {
    val result = measureTimedValue {
        val settingsProcessingActor = FileSystemSettingsActor()
        val ack = CompletableDeferred<Unit>()
        settingsProcessingActor.send(ActorMessage.Read(ack))
        ack.await()

        settingsProcessingActor
    }
    logger.info { "loaded settings in ${result.duration}" }
    return result.value
}

private fun setLogLevel() {
    val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
    rootLogger.level = Level.INFO
    (LoggerFactory.getLogger("org.freedesktop") as ch.qos.logback.classic.Logger).level = Level.WARN

}