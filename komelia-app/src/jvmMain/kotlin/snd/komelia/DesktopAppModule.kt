package snd.komelia

import coil3.PlatformContext
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import snd.komelia.AppDirectories.onnxRuntimeWorkingDir
import snd.komelia.db.AppSettings
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.ExposedTransactionTemplate
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.KomeliaDatabase
import snd.komelia.db.KomfSettings
import snd.komelia.db.OfflineSettings
import snd.komelia.db.SettingsStateWrapper
import snd.komelia.db.color.ExposedBookColorCorrectionRepository
import snd.komelia.db.color.ExposedColorCurvesPresetRepository
import snd.komelia.db.color.ExposedColorLevelsPresetRepository
import snd.komelia.db.fonts.ExposedUserFontsRepository
import snd.komelia.db.homescreen.ExposedHomeScreenFilterRepository
import snd.komelia.db.offline.ExposedLogJournalRepository
import snd.komelia.db.offline.ExposedMediaRepository
import snd.komelia.db.offline.ExposedOfflineBookMetadataAggregationRepository
import snd.komelia.db.offline.ExposedOfflineBookMetadataRepository
import snd.komelia.db.offline.ExposedOfflineBookRepository
import snd.komelia.db.offline.ExposedOfflineLibraryRepository
import snd.komelia.db.offline.ExposedOfflineMediaServerRepository
import snd.komelia.db.offline.ExposedOfflineReadProgressRepository
import snd.komelia.db.offline.ExposedOfflineSeriesMetadataRepository
import snd.komelia.db.offline.ExposedOfflineSeriesRepository
import snd.komelia.db.offline.ExposedOfflineSettingsRepository
import snd.komelia.db.offline.ExposedOfflineTasksRepository
import snd.komelia.db.offline.ExposedOfflineThumbnailBookRepository
import snd.komelia.db.offline.ExposedOfflineThumbnailSeriesRepository
import snd.komelia.db.offline.ExposedOfflineUserRepository
import snd.komelia.db.offline.dto.ExposedOfflineBookDtoRepository
import snd.komelia.db.offline.dto.ExposedOfflineReferentialRepository
import snd.komelia.db.offline.dto.ExposedSeriesDtoRepository
import snd.komelia.db.repository.EpubReaderSettingsRepositoryWrapper
import snd.komelia.db.repository.HomeScreenFilterRepositoryWrapper
import snd.komelia.db.repository.KomfSettingsRepositoryWrapper
import snd.komelia.db.repository.OfflineSettingsRepositoryWrapper
import snd.komelia.db.repository.ReaderSettingsRepositoryWrapper
import snd.komelia.db.repository.SettingsRepositoryWrapper
import snd.komelia.db.settings.ExposedEpubReaderSettingsRepository
import snd.komelia.db.settings.ExposedImageReaderSettingsRepository
import snd.komelia.db.settings.ExposedKomfSettingsRepository
import snd.komelia.db.settings.ExposedSettingsRepository
import snd.komelia.homefilters.homeScreenDefaultFilters
import snd.komelia.http.komeliaUserAgent
import snd.komelia.image.DesktopOnnxRuntimeUpscaler
import snd.komelia.image.DesktopPanelDetector
import snd.komelia.image.DesktopReaderImageFactory
import snd.komelia.image.KomeliaImageDecoder
import snd.komelia.image.KomeliaPanelDetector
import snd.komelia.image.KomeliaUpscaler
import snd.komelia.image.ReaderImageFactory
import snd.komelia.image.SkiaBitmap
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.VipsImageDecoder
import snd.komelia.image.VipsSharedLibraries
import snd.komelia.image.processing.ImageProcessingPipeline
import snd.komelia.offline.DesktopOfflineModule
import snd.komelia.offline.OfflineModule
import snd.komelia.offline.OfflineRepositories
import snd.komelia.onnxruntime.JvmOnnxRuntime
import snd.komelia.onnxruntime.JvmOnnxRuntimeRfDetr
import snd.komelia.onnxruntime.JvmOnnxRuntimeUpscaler
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.TENSOR_RT
import snd.komelia.onnxruntime.OnnxRuntimeSharedLibraries
import snd.komelia.secrets.AppKeyring
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.KeyringSecretsRepository
import snd.komelia.ui.error.NonRestartableException
import snd.komelia.updates.DesktopAppUpdater
import snd.komelia.updates.DesktopOnnxModelDownloader
import snd.komelia.updates.DesktopOnnxRuntimeInstaller
import snd.komelia.updates.OnnxModelDownloader
import snd.komelia.updates.UpdateClient
import snd.komga.client.KomgaClientFactory
import snd.komga.client.user.KomgaUser
import snd.webview.WebviewSharedLibraries
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.createDirectories
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

class DesktopAppModule(
    private val windowState: AwtWindowState
) : AppModule() {
    private val databases = KomeliaDatabase(AppDirectories.databaseDirectory.toString())

    private val okHttpLogger = KotlinLogging.logger("http.logging")
    private val okHttpClientWithoutCache: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor { okHttpLogger.info { it } }
            .setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()
    private val okHttpClient = okHttpClientWithoutCache.newBuilder().cache(
        Cache(
            directory = AppDirectories.okHttpCachePath.createDirectories().toFile(),
            maxSize = 64 * 1024L * 1024L // 64 MiB
        )
    ).build()

    private val okHttpClientSse = okHttpClientWithoutCache.newBuilder()
        .readTimeout(0.seconds)
        .build()

    override suspend fun beforeInit() {
        if (DesktopPlatform.Current != DesktopPlatform.Linux) {
            loadVipsLibraries()
            loadWebviewLibraries()
        }
        checkVipsLibraries()
        loadOnnxRuntimeLibraries()
    }


    private fun checkVipsLibraries() {
        VipsSharedLibraries.loadError?.let {
            throw NonRestartableException("Failed to load libvips shared libraries. ${it.message}", it)
        }
        if (!VipsSharedLibraries.isAvailable)
            throw NonRestartableException("libvips shared libraries were not loaded. libvips is required for image decoding")
        SkiaBitmap.load()
    }

    private fun loadOnnxRuntimeLibraries() {
        runCatching { OnnxRuntimeSharedLibraries.load() }
            .onFailure { logger.error(it) { "Couldn't load ONNX Runtime" } }
    }


    override suspend fun createAppRepositories(): AppRepositories {
        return AppRepositories(
            settingsRepository = ExposedSettingsRepository(databases.app).let { repository ->
                SettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get()
                            ?: AppSettings(),
                        saveSettings = repository::save
                    )
                )
            },
            epubReaderSettingsRepository = ExposedEpubReaderSettingsRepository(databases.app).let { repository ->
                EpubReaderSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: EpubReaderSettings(),
                        saveSettings = repository::save
                    )
                )
            },
            imageReaderSettingsRepository = ExposedImageReaderSettingsRepository(databases.app).let { repository ->
                ReaderSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: ImageReaderSettings(upsamplingMode = UpsamplingMode.CATMULL_ROM),
                        saveSettings = repository::save
                    )
                )
            },
            fontsRepository = ExposedUserFontsRepository(databases.app),
            colorCurvesPresetsRepository = ExposedColorCurvesPresetRepository(databases.app),
            colorLevelsPresetRepository = ExposedColorLevelsPresetRepository(databases.app),
            bookColorCorrectionRepository = ExposedBookColorCorrectionRepository(databases.app),
            secretsRepository = KeyringSecretsRepository(AppKeyring()),
            komfSettingsRepository = ExposedKomfSettingsRepository(databases.app).let { repository ->
                KomfSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: KomfSettings(),
                        saveSettings = repository::save
                    )
                )
            },
            homeScreenFilterRepository = ExposedHomeScreenFilterRepository(databases.app).let { repository ->
                HomeScreenFilterRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.getFilters() ?: homeScreenDefaultFilters,
                        saveSettings = repository::putFilters
                    )
                )
            }
        )
    }

    override suspend fun createOfflineRepositories(): OfflineRepositories {
        return OfflineRepositories(
            mediaServerRepository = ExposedOfflineMediaServerRepository(databases.offline),
            mediaRepository = ExposedMediaRepository(databases.offline),
            bookRepository = ExposedOfflineBookRepository(databases.offline),
            bookMetadataRepository = ExposedOfflineBookMetadataRepository(databases.offline),
            bookMetadataAggregationRepository = ExposedOfflineBookMetadataAggregationRepository(databases.offline),
            libraryRepository = ExposedOfflineLibraryRepository(databases.offline),
            readProgressRepository = ExposedOfflineReadProgressRepository(databases.offline),
            seriesMetadataRepository = ExposedOfflineSeriesMetadataRepository(databases.offline),
            seriesRepository = ExposedOfflineSeriesRepository(databases.offline),
            thumbnailBookRepository = ExposedOfflineThumbnailBookRepository(databases.offline),
            thumbnailSeriesRepository = ExposedOfflineThumbnailSeriesRepository(databases.offline),
            userRepository = ExposedOfflineUserRepository(databases.offline),
            tasksRepository = ExposedOfflineTasksRepository(databases.offline),
            logJournalRepository = ExposedLogJournalRepository(databases.offline),
            offlineSettingsRepository = ExposedOfflineSettingsRepository(databases.offline).let { repo ->
                OfflineSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repo.get()
                            ?: OfflineSettings(downloadDirectory = PlatformFile(AppDirectories.defaultOfflineLibraryPath.toString())),
                        saveSettings = repo::save
                    )
                )
            },
            transactionTemplate = ExposedTransactionTemplate(databases.offline),

            bookDtoRepository = ExposedOfflineBookDtoRepository(databases.offlineReadOnly),
            seriesDtoRepository = ExposedSeriesDtoRepository(databases.offlineReadOnly),
            referentialRepository = ExposedOfflineReferentialRepository(databases.offlineReadOnly),
        )
    }

    override fun createKtorClient(): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            expectSuccess = true

            install(UserAgent) {
                agent = komeliaUserAgent
            }
        }
    }

    override fun createKtorClientWithoutCache(): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClientWithoutCache }
            expectSuccess = true

            install(UserAgent) {
                agent = komeliaUserAgent
            }
        }
    }

    override fun createAppUpdater(updateClient: UpdateClient) = DesktopAppUpdater(updateClient)

    override fun createImageDecoder() = VipsImageDecoder()

    override suspend fun createReaderImageFactory(
        imageDecoder: KomeliaImageDecoder,
        pipeline: ImageProcessingPipeline,
        settings: ImageReaderSettingsRepository,
        onnxRuntimeUpscaler: KomeliaUpscaler?,
    ): ReaderImageFactory {
        return DesktopReaderImageFactory(
            imageDecoder = imageDecoder,
            downSamplingKernel = settings.getDownsamplingKernel().stateIn(initScope),
            upsamplingMode = settings.getUpsamplingMode().stateIn(initScope),
            linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(initScope),
            processingPipeline = pipeline,
            stretchImages = settings.getStretchToFit().stateIn(initScope),
            onnxUpscaler = onnxRuntimeUpscaler,
        )
    }

    override fun createWindowState() = windowState

    override fun createCoilContext() = PlatformContext.INSTANCE

    override fun createOnnxRuntimeInstaller(updateClient: UpdateClient) = DesktopOnnxRuntimeInstaller(updateClient)

    override fun createOnnxModelDownloader(updateClient: UpdateClient) =
        DesktopOnnxModelDownloader(updateClient, appNotifications)

    override fun createOnnxRuntime(): OnnxRuntime? {
        if (!OnnxRuntimeSharedLibraries.isAvailable) {
            logger.warn { "OnnxRuntime is not available" }
            return null
        }
        onnxRuntimeWorkingDir.createDirectories()
        return JvmOnnxRuntime.create(onnxRuntimeWorkingDir.toString())
    }

    override suspend fun createUpscaler(
        onnxRuntime: OnnxRuntime,
        modelDownloader: OnnxModelDownloader,
        settings: ImageReaderSettingsRepository,
    ): KomeliaUpscaler {
        val upscaler = JvmOnnxRuntimeUpscaler.create(onnxRuntime as JvmOnnxRuntime)
        return DesktopOnnxRuntimeUpscaler(
            settingsRepository = settings,
            executionProvider = OnnxRuntimeSharedLibraries.executionProvider,
            ortUpscaler = upscaler,
            updateFlow = modelDownloader.downloadCompletionEvents.filterIsInstance()
        ).also {
            it.initialize()
            Runtime.getRuntime().addShutdownHook(thread(start = false) { it.clearCache() })
        }
    }

    override suspend fun createPanelDetector(
        onnxRuntime: OnnxRuntime,
        modelDownloader: OnnxModelDownloader,
        settings: ImageReaderSettingsRepository,
    ): KomeliaPanelDetector {
        val rfDetr = JvmOnnxRuntimeRfDetr.create(onnxRuntime as JvmOnnxRuntime)
        val provider = when (OnnxRuntimeSharedLibraries.executionProvider) {
            TENSOR_RT -> CUDA // TRT is broken. fallback to cuda
            DirectML -> CPU // DirectML is broken. fallback to cpu
            else -> OnnxRuntimeSharedLibraries.executionProvider
        }
        val detector = DesktopPanelDetector(
            rfDetr = rfDetr,
            executionProvider = provider,
            deviceId = settings.getOnnxRuntimeDeviceId().stateIn(initScope),
            updateFlow = modelDownloader.downloadCompletionEvents.filterIsInstance()

        )
        detector.initialize()

        return detector
    }

    override fun getCoilCacheDirectory(): Path {
        return Path(AppDirectories.coilCachePath.toString())
    }

    override fun createCoilMemoryCache(): MemoryCache {
        return MemoryCache.Builder()
            .maxSizeBytes(64 * 1024 * 1024) // 64 Mib
            .build()
    }

    override fun getReaderCacheDirectory(): Path {
        return Path(AppDirectories.readerCachePath.toString())
    }

    override fun createOfflineModule(
        repositories: OfflineRepositories,
        onlineUser: StateFlow<KomgaUser?>,
        onlineServerUrl: StateFlow<String>,
        isOffline: StateFlow<Boolean>,
        komgaClientFactory: KomgaClientFactory
    ): OfflineModule {
        return DesktopOfflineModule(
            repositories = repositories,
            onlineUser = onlineUser,
            onlineServerUrl = onlineServerUrl,
            isOffline = isOffline,
            komgaClientFactory = komgaClientFactory
        )
    }
}

fun loadWebviewLibraries() {
    measureTime {
        try {
            WebviewSharedLibraries.load()
        } catch (e: UnsatisfiedLinkError) {
            logger.error(e) { "Couldn't load webview library. Epub reader will not work" }
        }
    }.also { logger.info { "Completed Webview library load in $it" } }
}

fun loadVipsLibraries() {
    measureTime {
        try {
            VipsSharedLibraries.load()
        } catch (e: UnsatisfiedLinkError) {
            logger.error(e) { "Couldn't load libvips. Vips decoder will not work" }
        }
    }.also { logger.info { "completed vips load in $it" } }
}
