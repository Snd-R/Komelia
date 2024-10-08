package snd.komelia

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.AndroidSharedLibrariesLoader
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.DependencyContainer
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.http.komeliaUserAgent
import io.github.snd_r.komelia.image.AndroidImageDecoder
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.image.coil.*
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.AndroidSecretsRepository
import io.github.snd_r.komelia.settings.AppSettingsSerializer
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.updates.AndroidAppUpdater
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.UpdateClient
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.FileSystem
import snd.komelia.db.Database
import snd.komelia.db.settings.*
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import snd.settings.CommonSettingsRepository
import snd.settings.ReaderSettingsRepository
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class AndroidDependencyContainer(
    override val settingsRepository: CommonSettingsRepository,
    override val readerSettingsRepository: ReaderSettingsRepository,
    override val secretsRepository: SecretsRepository,
    override val appUpdater: AppUpdater,
    override val imageDecoderDescriptor: Flow<PlatformDecoderDescriptor>,
    override val komgaClientFactory: KomgaClientFactory,
    override val readerImageLoader: ReaderImageLoader,
    override val imageLoader: ImageLoader,
    override val platformContext: PlatformContext,
    override val komfClientFactory: KomfClientFactory,
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
            )
            val secretsRepository = AndroidSecretsRepository(datastore)

            val database = Database(context.filesDir.resolve("komelia.sqlite").absolutePath)
            val settingsActor = createSettingsActor(database)
            val settingsRepository = SharedActorSettingsRepository(settingsActor)
            val readerSettingsRepository = SharedActorReaderSettingsRepository(settingsActor)

            val baseUrl = settingsRepository.getServerUrl().stateIn(scope)
            val komfUrl = settingsRepository.getKomfUrl().stateIn(scope)

            val okHttpWithoutCache = createOkHttpClient()
            val okHttpWithCache = okHttpWithoutCache.newBuilder()
                .cache(Cache(directory = context.cacheDir.resolve("okhttp"), maxSize = 50L * 1024L * 1024L))
                .build()
            val ktorWithCache = createKtorClient(okHttpWithCache)
            val ktorWithoutCache = createKtorClient(okHttpWithoutCache)

            val cookiesStorage = RememberMePersistingCookieStore(
                baseUrl.map { Url(it) }.stateIn(scope),
                secretsRepository
            )
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

            val komfClientFactory = KomfClientFactory.Builder()
                .baseUrl { komfUrl.value }
                .ktor(ktorWithCache)
                .build()

            return AndroidDependencyContainer(
                settingsRepository = settingsRepository,
                readerSettingsRepository = readerSettingsRepository,
                secretsRepository = secretsRepository,
                appUpdater = appUpdater,
                imageDecoderDescriptor = emptyFlow(),
                komgaClientFactory = komgaClientFactory,
                imageLoader = coil,
                platformContext = context,
                readerImageLoader = readerImageLoader,
                komfClientFactory = komfClientFactory,
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

        private fun createSettingsActor(database: Database): SettingsActor {
            val result = measureTimedValue {
                val repository = JooqSettingsRepository(database.dsl)
                SettingsActor(
                    settings = repository.get()
                        ?: AppSettings(
                            cardWidth = 150,
                            upscaleOption = "Default",
                            downscaleOption = "Default"
                        ),
                    saveSettings = repository::save
                )
            }
            logger.info { "loaded settings in ${result.duration}" }
            return result.value
        }
    }
}