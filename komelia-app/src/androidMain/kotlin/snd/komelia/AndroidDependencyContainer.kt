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
import io.github.snd_r.komelia.image.CropBordersStep
import io.github.snd_r.komelia.image.ImageProcessingPipeline
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.FileSystem
import snd.komelia.db.Database
import snd.komelia.db.settings.AppSettings
import snd.komelia.db.settings.JooqSettingsRepository
import snd.komelia.db.settings.SettingsActor
import snd.komelia.db.settings.SharedActorReaderSettingsRepository
import snd.komelia.db.settings.SharedActorSettingsRepository
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
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
        suspend fun createInstance(initScope: CoroutineScope, context: Context): AndroidDependencyContainer {
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

            val baseUrl = settingsRepository.getServerUrl().stateIn(initScope)
            val komfUrl = settingsRepository.getKomfUrl().stateIn(initScope)

            val okHttpWithoutCache = createOkHttpClient()
            val okHttpWithCache = okHttpWithoutCache.newBuilder()
                .cache(Cache(directory = context.cacheDir.resolve("okhttp"), maxSize = 50L * 1024L * 1024L))
                .build()
            val ktorWithCache = createKtorClient(okHttpWithCache)
            val ktorWithoutCache = createKtorClient(okHttpWithoutCache)

            val cookiesStorage = RememberMePersistingCookieStore(
                baseUrl.map { Url(it) }.stateIn(initScope),
                secretsRepository
            )
                .also { it.loadRememberMeCookie() }

            val komgaClientFactory = createKomgaClientFactory(
                baseUrl = baseUrl,
                ktorClient = ktorWithCache,
                cookiesStorage = cookiesStorage,
            )
            val appUpdater = createAppUpdater(ktorWithCache, ktorWithoutCache, context)

            val imagePipeline = createImagePipeline(
                cropBorders = readerSettingsRepository.getCropBorders().stateIn(initScope)
            )
            val stretchImages = readerSettingsRepository.getStretchToFit().stateIn(initScope)
            val readerImageLoader = createReaderImageLoader(
                baseUrl = baseUrl,
                ktorClient = ktorWithoutCache,
                cookiesStorage = cookiesStorage,
                stretchImages = stretchImages,
                pipeline = imagePipeline,
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
            stretchImages: StateFlow<Boolean>,
            pipeline: ImageProcessingPipeline
        ): ReaderImageLoader {
            val bookClient = KomgaClientFactory.Builder()
                .ktor(ktorClient)
                .baseUrl { baseUrl.value }
                .cookieStorage(cookiesStorage)
                .build()
                .bookClient()
            return ReaderImageLoader(
                bookClient = bookClient,
                decoder = AndroidImageDecoder(
                    stretchImages = stretchImages,
                    processingPipeline = pipeline
                ),
                diskCache = DiskCache.Builder()
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


        private fun createImagePipeline(
            cropBorders: StateFlow<Boolean>,
        ): ImageProcessingPipeline {
            val pipeline = ImageProcessingPipeline()
            pipeline.addStep(CropBordersStep(cropBorders))
            return pipeline
        }
    }
}