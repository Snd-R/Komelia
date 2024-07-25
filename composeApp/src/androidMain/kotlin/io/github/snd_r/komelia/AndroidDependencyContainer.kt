package io.github.snd_r.komelia

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.AndroidSharedLibrariesLoader
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.http.komeliaUserAgent
import io.github.snd_r.komelia.image.AndroidImageDecoder
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.image.coil.FileMapper
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.image.coil.VipsImageDecoder
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.AndroidReaderSettingsRepository
import io.github.snd_r.komelia.settings.AndroidSecretsRepository
import io.github.snd_r.komelia.settings.AndroidSettingsRepository
import io.github.snd_r.komelia.settings.AppSettingsSerializer
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.updates.AndroidAppUpdater
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.UpdateClient
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.FileSystem
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

class AndroidDependencyContainer(
    override val settingsRepository: SettingsRepository,
    override val readerSettingsRepository: ReaderSettingsRepository,
    override val secretsRepository: SecretsRepository,
    override val appUpdater: AppUpdater,
    override val availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    override val komgaClientFactory: KomgaClientFactory,
    override val readerImageLoader: ReaderImageLoader,
    override val imageLoader: ImageLoader,
    override val imageLoaderContext: PlatformContext
) : DependencyContainer {
    override val appNotifications: AppNotifications = AppNotifications()

    companion object {
        suspend fun createInstance(scope: CoroutineScope, context: Context): AndroidDependencyContainer {
            measureTime {
                try {
                    AndroidSharedLibrariesLoader.load()
                } catch (e: UnsatisfiedLinkError) {
                    logger.error(e) { "Couldn't load vips shared libraries. reader image loading will not work" }
                }
            }.also { logger.info { "completed vips libraries load in $it" } }

            val datastore = DataStoreFactory.create(
                serializer = AppSettingsSerializer,
                produceFile = { context.dataStoreFile("settings.pb") },
                corruptionHandler = null,
                migrations = listOf(),
            )

            val settingsRepository = AndroidSettingsRepository(datastore)
            val readerSettingsRepository = AndroidReaderSettingsRepository(datastore)
            val secretsRepository = AndroidSecretsRepository(datastore)

            val baseUrl = settingsRepository.getServerUrl().stateIn(scope)

            val okHttpWithoutCache = createOkHttpClient()
            val okHttpWithCache = okHttpWithoutCache.newBuilder()
                .cache(Cache(directory = context.cacheDir.resolve("okhttp"), maxSize = 50L * 1024L * 1024L))
                .build()
            val ktorWithCache = createKtorClient(okHttpWithCache)
            val ktorWithoutCache = createKtorClient(okHttpWithoutCache)

            val cookiesStorage = RememberMePersistingCookieStore(baseUrl, secretsRepository)
                .also { it.loadRememberMeCookie() }

            val komgaClientFactory = createKomgaClientFactory(
                baseUrl = baseUrl,
                ktorClient = ktorWithCache,
                cookiesStorage = cookiesStorage,
            )
            val appUpdater = createAppUpdater(ktorWithCache, ktorWithoutCache, context)
            val readerImageLoader = createReaderImageLoader(
                baseUrl = baseUrl,
                ktorClient = ktorWithoutCache,
                cookiesStorage = cookiesStorage,
            )

            val coil = createCoil(ktorWithoutCache, baseUrl, cookiesStorage, context)
            SingletonImageLoader.setSafe { coil }

            return AndroidDependencyContainer(
                settingsRepository = settingsRepository,
                readerSettingsRepository = readerSettingsRepository,
                secretsRepository = secretsRepository,
                appUpdater = appUpdater,
                availableDecoders = emptyFlow(),
                komgaClientFactory = komgaClientFactory,
                imageLoader = coil,
                imageLoaderContext = context,
                readerImageLoader = readerImageLoader
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
            okHttpClient: OkHttpClient,
        ): HttpClient {
            return HttpClient(OkHttp) {
                engine { preconfigured = okHttpClient }
                expectSuccess = true

                install(UserAgent) {
                    agent = komeliaUserAgent
                }
            }
        }

        private fun createKomgaClientFactory(
            baseUrl: StateFlow<String>,
            ktorClient: HttpClient,
            cookiesStorage: RememberMePersistingCookieStore,
        ): KomgaClientFactory {
            return KomgaClientFactory.Builder()
                .ktor(ktorClient)
                .baseUrl { baseUrl.value }
                .cookieStorage(cookiesStorage)
                .build()
        }

        private fun createReaderImageLoader(
            baseUrl: StateFlow<String>,
            ktorClient: HttpClient,
            cookiesStorage: RememberMePersistingCookieStore,
        ): ReaderImageLoader {
            val bookClient = KomgaClientFactory.Builder()
                .ktor(ktorClient)
                .baseUrl { baseUrl.value }
                .cookieStorage(cookiesStorage)
                .build()
                .bookClient()
            return ReaderImageLoader(
                bookClient,
                AndroidImageDecoder(),
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "komelia_reader_cache")
                    .build()
            )
        }

        private fun createCoil(
            ktorClient: HttpClient,
            url: StateFlow<String>,
            cookiesStorage: RememberMePersistingCookieStore,
            context: PlatformContext
        ): ImageLoader {
            val coilKtorClient = ktorClient.config {
                defaultRequest { url(url.value) }
                install(HttpCookies) { storage = cookiesStorage }
            }
            val diskCache = DiskCache.Builder()
                .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "coil3_disk_cache")
                .build()
            diskCache.clear()

            return ImageLoader.Builder(context)
                .components {
                    add(KomgaBookPageMapper(url))
                    add(KomgaSeriesMapper(url))
                    add(KomgaBookMapper(url))
                    add(KomgaCollectionMapper(url))
                    add(KomgaReadListMapper(url))
                    add(KomgaSeriesThumbnailMapper(url))
                    add(FileMapper())
                    add(VipsImageDecoder.Factory())
                    add(KtorNetworkFetcherFactory(httpClient = coilKtorClient))
                }.diskCache(diskCache)
                .build()
        }

        private fun createAppUpdater(
            ktor: HttpClient,
            ktorWithoutCache: HttpClient,
            context: Context
        ): AndroidAppUpdater {
            val githubClient = UpdateClient(
                ktor = ktor.config {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                },
                ktorWithoutCache = ktorWithoutCache.config {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                },
            )
            return AndroidAppUpdater(githubClient, context)
        }
    }
}