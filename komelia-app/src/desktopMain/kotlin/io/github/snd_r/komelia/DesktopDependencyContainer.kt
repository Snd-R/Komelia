package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.OnnxRuntimeSharedLibraries
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsSharedLibraries
import io.github.snd_r.komelia.AppDirectories.coilCachePath
import io.github.snd_r.komelia.AppDirectories.mangaJaNaiModelFiles
import io.github.snd_r.komelia.AppDirectories.readerCachePath
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.http.komeliaUserAgent
import io.github.snd_r.komelia.image.DesktopImageDecoder
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.image.coil.DesktopDecoder
import io.github.snd_r.komelia.image.coil.FileMapper
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.mangaJaNai
import io.github.snd_r.komelia.platform.upsamplingFilters
import io.github.snd_r.komelia.platform.vipsDownscaleLanczos
import io.github.snd_r.komelia.secrets.AppKeyring
import io.github.snd_r.komelia.settings.ActorMessage
import io.github.snd_r.komelia.settings.DesktopReaderSettingsRepository
import io.github.snd_r.komelia.settings.DesktopSettingsRepository
import io.github.snd_r.komelia.settings.FileSystemSettingsActor
import io.github.snd_r.komelia.settings.KeyringSecretsRepository
import io.github.snd_r.komelia.ui.error.NonRestartableException
import io.github.snd_r.komelia.ui.settings.decoder.DecoderSettingsViewModel
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.DesktopAppUpdater
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import io.github.snd_r.komelia.updates.UpdateClient
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Path.Companion.toOkioPath
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class DesktopDependencyContainer private constructor(
    override val komgaClientFactory: KomgaClientFactory,
    override val appUpdater: AppUpdater,
    override val settingsRepository: DesktopSettingsRepository,
    override val readerSettingsRepository: DesktopReaderSettingsRepository,
    override val secretsRepository: KeyringSecretsRepository,
    override val imageLoader: ImageLoader,
    override val availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    override val readerImageLoader: ReaderImageLoader,
    override val appNotifications: AppNotifications,
    override val komfClientFactory: KomfClientFactory,
    val onnxRuntimeInstaller: OnnxRuntimeInstaller,
    val mangaJaNaiDownloader: MangaJaNaiDownloader
) : DependencyContainer {
    override val imageLoaderContext: PlatformContext = PlatformContext.INSTANCE

    companion object {
        suspend fun createInstance(systemScope: CoroutineScope): DesktopDependencyContainer {
            VipsSharedLibraries.loadError?.let {
                throw NonRestartableException("Failed to load libvips shared libraries. ${it.message}", it)
            }
            if (!VipsSharedLibraries.isAvailable)
                throw NonRestartableException("libvips shared libraries were not loaded. libvips is required for image decoding")
            VipsBitmapFactory.load()

            val settingsActor = createSettingsActor()
            val settingsRepository = DesktopSettingsRepository(settingsActor)
            val readerSettingsRepository = DesktopReaderSettingsRepository(settingsActor)

            val onnxUpscaler = measureTimedValue {
                try {
                    OnnxRuntimeSharedLibraries.load()
                    ManagedOnnxUpscaler(settingsRepository).also { it.initialize() }
                } catch (e: UnsatisfiedLinkError) {
                    logger.error(e) { "Couldn't load ONNX Runtime. ONNX upscaling will not work" }
                    null
                }
            }.also { logger.info { "completed ONNX Runtime load in ${it.duration}" } }

            val secretsRepository = createSecretsRepository()

            val baseUrl = settingsRepository.getServerUrl().stateIn(systemScope)
            val komfUrl = settingsRepository.getKomfUrl().stateIn(systemScope)
            val decoderType = settingsRepository.getDecoderSettings().stateIn(systemScope)

            val okHttpWithoutCache = createOkHttpClient()
            val okHttpWithCache = okHttpWithoutCache.newBuilder()
                .cache(
                    Cache(
                        directory = AppDirectories.okHttpCachePath.createDirectories().toFile(),
                        maxSize = 50L * 1024L * 1024L // 50 MiB
                    )
                ).build()
            val cookiesStorage = RememberMePersistingCookieStore(
                baseUrl.map { Url(it) }.stateIn(systemScope),
                secretsRepository
            )

            measureTime { cookiesStorage.loadRememberMeCookie() }
                .also { logger.info { "loaded remember-me cookie from keyring in $it" } }

            val ktorWithCache = createKtorClient(okHttpWithCache)
            val ktorWithoutCache = createKtorClient(okHttpWithoutCache)

            val komgaClientFactory = createKomgaClientFactory(
                baseUrl = baseUrl,
                ktorClient = ktorWithCache,
                cookiesStorage = cookiesStorage,
            )

            val notifications = AppNotifications()
            val updateClient = UpdateClient(
                ktor = ktorWithCache.config {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                },
                ktorWithoutCache = ktorWithoutCache.config {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }
            )
            val appUpdater = DesktopAppUpdater(updateClient)
            val onnxRuntimeInstaller = OnnxRuntimeInstaller(updateClient)
            val mangaJaNaiDownloader = MangaJaNaiDownloader(updateClient, notifications)

            val coil = createCoil(
                ktorClient = ktorWithoutCache,
                url = baseUrl,
                cookiesStorage = cookiesStorage,
                decoderState = decoderType,
                tempDir = coilCachePath.createDirectories()
            )
            SingletonImageLoader.setSafe { coil }

            val readerImageLoader = createReaderImageLoader(
                baseUrl = baseUrl,
                ktorClient = ktorWithoutCache,
                cookiesStorage = cookiesStorage,
                upscaleOption = decoderType.map { it.upscaleOption }.stateIn(systemScope),
                upscaler = onnxUpscaler.value
            )

            val availableDecoders = createAvailableDecodersFlow(
                settingsRepository,
                mangaJaNaiDownloader,
                systemScope
            )
            val komfClientFactory = KomfClientFactory.Builder()
                .baseUrl { komfUrl.value }
                .ktor(ktorWithCache)
                .build()

            return DesktopDependencyContainer(
                komgaClientFactory = komgaClientFactory,
                appUpdater = appUpdater,
                settingsRepository = settingsRepository,
                readerSettingsRepository = readerSettingsRepository,
                secretsRepository = secretsRepository,
                imageLoader = coil,
                availableDecoders = availableDecoders,
                readerImageLoader = readerImageLoader,
                appNotifications = notifications,
                komfClientFactory = komfClientFactory,
                onnxRuntimeInstaller = onnxRuntimeInstaller,
                mangaJaNaiDownloader = mangaJaNaiDownloader
            )
        }

        private fun createOkHttpClient(): OkHttpClient {
            return measureTimedValue {
                val logger = KotlinLogging.logger("http.logging")
                val loggingInterceptor = HttpLoggingInterceptor { logger.info { it } }
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
            okHttpClient: OkHttpClient,
        ): HttpClient {
            return measureTimedValue {
                HttpClient(OkHttp) {
                    engine { preconfigured = okHttpClient }
                    expectSuccess = true

                    install(UserAgent) {
                        agent = komeliaUserAgent
                    }

                }
            }.also { logger.info { "initialized Ktor in ${it.duration}" } }
                .value
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
            upscaleOption: StateFlow<UpscaleOption>,
            upscaler: ManagedOnnxUpscaler?
        ): ReaderImageLoader {
            val bookClient = KomgaClientFactory.Builder()
                .ktor(ktorClient)
                .baseUrl { baseUrl.value }
                .cookieStorage(cookiesStorage)
                .build()
                .bookClient()

            val decoder = DesktopImageDecoder(
                upscaleOptionFlow = upscaleOption,
                onnxUpscaler = upscaler
            )

            return ReaderImageLoader(
                bookClient,
                decoder,
                DiskCache.Builder()
                    .directory(readerCachePath.createDirectories().toOkioPath())
                    .build()
            )
        }

        private fun createCoil(
            ktorClient: HttpClient,
            url: StateFlow<String>,
            cookiesStorage: RememberMePersistingCookieStore,
            decoderState: StateFlow<PlatformDecoderSettings>,
            tempDir: Path,
        ): ImageLoader {
            val coilKtorClient = ktorClient.config {
                defaultRequest { url(url.value) }
                install(HttpCookies) { storage = cookiesStorage }
            }
            val diskCache = DiskCache.Builder()
                .directory(tempDir.toOkioPath())
                .build()
            diskCache.clear()

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
                        add(KtorNetworkFetcherFactory(httpClient = coilKtorClient))
                    }
                    .memoryCache(
                        MemoryCache.Builder()
                            .maxSizeBytes(128 * 1024 * 1024) // 128 Mib
                            .build()
                    )
                    .diskCache { diskCache }
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

        private fun createSecretsRepository(): KeyringSecretsRepository {
            return measureTimedValue { KeyringSecretsRepository(AppKeyring()) }
                .also { logger.info { "initialized keyring in ${it.duration}" } }
                .value
        }

        private suspend fun createAvailableDecodersFlow(
            settingsRepository: DesktopSettingsRepository,
            mangaJaNaiDownloader: MangaJaNaiDownloader,
            scope: CoroutineScope
        ): Flow<List<PlatformDecoderDescriptor>> {
            val decoderFlow: StateFlow<List<PlatformDecoderDescriptor>> =
                if (VipsSharedLibraries.isAvailable && OnnxRuntimeSharedLibraries.isAvailable) {
                    settingsRepository.getOnnxModelsPath()
                        .combine(mangaJaNaiDownloader.downloadCompletionEventFlow) { path, _ -> path }
                        .map { listOf(createVipsOnnxDescriptor(Path.of(it))) }
                        .stateIn(scope)
                } else {
                    MutableStateFlow(
                        listOf(
                            PlatformDecoderDescriptor(
                                platformType = PlatformDecoderType.VIPS,
                                upscaleOptions = upsamplingFilters,
                                downscaleOptions = listOf(vipsDownscaleLanczos),
                            )
                        )
                    )
                }

            decoderFlow.onEach { decoders ->
                val current = settingsRepository.getDecoderSettings().first()
                val currentDescriptor = decoders.firstOrNull { it.platformType == current.platformType }
                if (currentDescriptor == null) {
                    val newDecoder = decoders.first()
                    settingsRepository.putDecoderSettings(
                        PlatformDecoderSettings(
                            platformType = newDecoder.platformType,
                            upscaleOption = newDecoder.upscaleOptions.first(),
                            downscaleOption = newDecoder.downscaleOptions.first()
                        )
                    )
                } else if (!currentDescriptor.upscaleOptions.contains(current.upscaleOption)) {
                    settingsRepository.putDecoderSettings(
                        current.copy(upscaleOption = currentDescriptor.upscaleOptions.first())
                    )
                }
            }.launchIn(scope)

            return decoderFlow
        }

        private fun createVipsOnnxDescriptor(modelsPath: Path): PlatformDecoderDescriptor {
            val mangaJaNaiIsAvailable = AppDirectories.mangaJaNaiInstallPath.exists() &&
                    AppDirectories.mangaJaNaiInstallPath.listDirectoryEntries()
                        .map { it.fileName.toString() }
                        .containsAll(mangaJaNaiModelFiles)
            val defaultOptions = if (mangaJaNaiIsAvailable) upsamplingFilters + mangaJaNai else upsamplingFilters

            try {
                val models = Files.list(modelsPath)
                    .filter { !it.isDirectory() }
                    .map { it.fileName.toString() }
                    .filter { it.endsWith(".onnx") }
                    .sorted()
                    .toList()
                return PlatformDecoderDescriptor(
                    platformType = PlatformDecoderType.VIPS_ONNX,
                    upscaleOptions = defaultOptions + models.map { UpscaleOption(it) },
                    downscaleOptions = listOf(vipsDownscaleLanczos),
                )
            } catch (e: java.nio.file.NoSuchFileException) {
                return PlatformDecoderDescriptor(
                    platformType = PlatformDecoderType.VIPS_ONNX,
                    upscaleOptions = defaultOptions,
                    downscaleOptions = listOf(vipsDownscaleLanczos),
                )
            }
        }
    }
}

class DesktopViewModelFactory(private val dependencies: DesktopDependencyContainer) {
    fun getDecoderSettingsViewModel(): DecoderSettingsViewModel {
        return DecoderSettingsViewModel(
            settingsRepository = dependencies.settingsRepository,
            imageLoader = dependencies.imageLoader,
            onnxRuntimeInstaller = dependencies.onnxRuntimeInstaller,
            mangaJaNaiDownloader = dependencies.mangaJaNaiDownloader,
            appNotifications = dependencies.appNotifications,
            availableDecoders = dependencies.availableDecoders,
        )
    }
}