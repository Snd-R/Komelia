package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.VipsDecoder
import io.github.snd_r.VipsOnnxRuntimeDecoder
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.image.DesktopDecoder
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
import io.github.snd_r.komelia.platform.PlatformDecoderType.IMAGE_IO
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.imageIoDownscale
import io.github.snd_r.komelia.platform.imageIoUpscale
import io.github.snd_r.komelia.platform.vipsDownscaleLanczos
import io.github.snd_r.komelia.platform.vipsUpscaleBicubic
import io.github.snd_r.komelia.secrets.AppKeyring
import io.github.snd_r.komelia.settings.ActorMessage
import io.github.snd_r.komelia.settings.FileSystemSettingsActor
import io.github.snd_r.komelia.settings.FilesystemReaderSettingsRepository
import io.github.snd_r.komelia.settings.FilesystemSettingsRepository
import io.github.snd_r.komelia.settings.KeyringSecretsRepository
import io.github.snd_r.komelia.ui.settings.decoder.DecoderSettingsViewModel
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.DesktopAppUpdater
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import io.github.snd_r.komelia.updates.UpdateClient
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class DesktopDependencyContainer private constructor(
    override val komgaClientFactory: KomgaClientFactory,
    override val appUpdater: AppUpdater,
    override val settingsRepository: FilesystemSettingsRepository,
    override val readerSettingsRepository: FilesystemReaderSettingsRepository,
    override val secretsRepository: KeyringSecretsRepository,
    override val imageLoader: ImageLoader,
    override val availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    val onnxRuntimeInstaller: OnnxRuntimeInstaller
) : DependencyContainer {
    override val imageLoaderContext: PlatformContext = PlatformContext.INSTANCE
    override val appNotifications: AppNotifications = AppNotifications()

    companion object {
        suspend fun createInstance(scope: CoroutineScope): DesktopDependencyContainer {
            measureTime {
                try {
                    VipsOnnxRuntimeDecoder.load()
                } catch (e: UnsatisfiedLinkError) {
                    logger.error(e) { "Couldn't load ONNX Runtime. ONNX upscaling will not work" }
                }
            }.also { logger.info { "completed ONNX Runtime load in $it" } }

            val settingsActor = createSettingsActor()
            val settingsRepository = FilesystemSettingsRepository(settingsActor)
            val readerSettingsRepository = FilesystemReaderSettingsRepository(settingsActor)

            val secretsRepository = createSecretsRepository()

            val baseUrl = settingsRepository.getServerUrl().stateIn(scope)
            val decoderType = settingsRepository.getDecoderType().stateIn(scope)
            val onnxModelsPath = settingsRepository.getOnnxModelsPath().stateIn(scope)

            val okHttpClient = createOkHttpClient()
            val cookiesStorage = RememberMePersistingCookieStore(baseUrl, secretsRepository)

            measureTime { cookiesStorage.loadRememberMeCookie() }
                .also { logger.info { "loaded remember-me cookie from keyring in $it" } }

            val ktorClient = createKtorClient(okHttpClient)
            val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient, cookiesStorage)

            val updateClient = UpdateClient(
                ktorClient.config {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }
            )
            val appUpdater = DesktopAppUpdater(updateClient)
            val onnxRuntimeInstaller = OnnxRuntimeInstaller(updateClient)

            val coil = createCoil(ktorClient, baseUrl, cookiesStorage, decoderType, onnxModelsPath)
            SingletonImageLoader.setSafe { coil }

            val availableDecoders = createAvailableDecodersFlow(settingsRepository, scope)

            return DesktopDependencyContainer(
                komgaClientFactory = komgaClientFactory,
                appUpdater = appUpdater,
                settingsRepository = settingsRepository,
                readerSettingsRepository = readerSettingsRepository,
                secretsRepository = secretsRepository,
                imageLoader = coil,
                availableDecoders = availableDecoders,
                onnxRuntimeInstaller = onnxRuntimeInstaller
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

                val tempDir = Path(System.getProperty("java.io.tmpdir")).resolve("komelia_http").createDirectories()
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
            ktorClient: HttpClient,
            url: StateFlow<String>,
            cookiesStorage: RememberMePersistingCookieStore,
            decoderState: StateFlow<PlatformDecoderSettings>,
            onnxModelsPath: StateFlow<String>,
        ): ImageLoader {
            val coilKtorClient = ktorClient.config {
                defaultRequest { url(url.value) }
                install(HttpCookies) { storage = cookiesStorage }
            }

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
                        add(DesktopDecoder.Factory(decoderState, onnxModelsPath))
                        add(KtorNetworkFetcherFactory(httpClient = coilKtorClient))
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

        private fun createSecretsRepository(): KeyringSecretsRepository {
            return measureTimedValue { KeyringSecretsRepository(AppKeyring()) }
                .also { logger.info { "initialized keyring in ${it.duration}" } }
                .value
        }

        private suspend fun createAvailableDecodersFlow(
            settingsRepository: FilesystemSettingsRepository,
            scope: CoroutineScope
        ): Flow<List<PlatformDecoderDescriptor>> {
            val decoderFlow =
                if (VipsDecoder.isAvailable && VipsOnnxRuntimeDecoder.isAvailable) {
                    settingsRepository.getOnnxModelsPath()
                        .map { listOf(getOnnxDecoderDescriptor(Path.of(it))) }
                        .stateIn(scope)

                } else if (VipsDecoder.isAvailable) {
                    MutableStateFlow(
                        listOf(
                            PlatformDecoderDescriptor(
                                platformType = PlatformDecoderType.VIPS,
                                upscaleOptions = listOf(vipsUpscaleBicubic),
                                downscaleOptions = listOf(vipsDownscaleLanczos),
                            )
                        )
                    )
                } else {
                    MutableStateFlow(
                        listOf(
                            PlatformDecoderDescriptor(
                                platformType = IMAGE_IO,
                                upscaleOptions = listOf(imageIoUpscale),
                                downscaleOptions = listOf(imageIoDownscale),
                            )
                        )
                    )
                }

            decoderFlow.onEach { decoders ->
                val current = settingsRepository.getDecoderType().first()
                val currentDescriptor = decoders.firstOrNull { it.platformType == current.platformType }
                if (currentDescriptor == null) {
                    val newDecoder = decoders.first()
                    settingsRepository.putDecoderType(
                        PlatformDecoderSettings(
                            platformType = newDecoder.platformType,
                            upscaleOption = newDecoder.upscaleOptions.first(),
                            downscaleOption = newDecoder.downscaleOptions.first()
                        )
                    )
                } else if (!currentDescriptor.upscaleOptions.contains(current.upscaleOption)) {
                    settingsRepository.putDecoderType(
                        current.copy(upscaleOption = currentDescriptor.upscaleOptions.first())
                    )
                }
            }.launchIn(scope)

            return decoderFlow
        }

        private fun getOnnxDecoderDescriptor(path: Path): PlatformDecoderDescriptor {
            try {
                val models = Files.list(path)
                    .filter { !it.isDirectory() }
                    .map { it.fileName.toString() }
                    .filter { it.endsWith(".onnx") }
                    .sorted()
                    .toList()
                return PlatformDecoderDescriptor(
                    platformType = PlatformDecoderType.VIPS_ONNX,
                    upscaleOptions = listOf(vipsUpscaleBicubic) + models.map { UpscaleOption(it) },
                    downscaleOptions = listOf(vipsDownscaleLanczos),
                )
            } catch (e: java.nio.file.NoSuchFileException) {
                return PlatformDecoderDescriptor(
                    platformType = PlatformDecoderType.VIPS_ONNX,
                    upscaleOptions = listOf(vipsUpscaleBicubic),
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
            appNotifications = dependencies.appNotifications,
            availableDecoders = dependencies.availableDecoders,
        )
    }
}