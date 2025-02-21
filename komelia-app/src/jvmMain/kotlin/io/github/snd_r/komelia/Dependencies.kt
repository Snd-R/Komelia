package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppDirectories.coilCachePath
import io.github.snd_r.komelia.AppDirectories.readerCachePath
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.http.komeliaUserAgent
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.DesktopReaderImageFactory
import io.github.snd_r.komelia.image.ManagedOnnxUpscaler
import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.image.coil.CoilDecoder
import io.github.snd_r.komelia.image.coil.FileMapper
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.image.processing.ColorCorrectionStep
import io.github.snd_r.komelia.image.processing.CropBordersStep
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import io.github.snd_r.komelia.platform.AwtWindowState
import io.github.snd_r.komelia.secrets.AppKeyring
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.settings.KeyringSecretsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.ui.error.NonRestartableException
import io.github.snd_r.komelia.updates.DesktopAppUpdater
import io.github.snd_r.komelia.updates.DesktopMangaJaNaiDownloader
import io.github.snd_r.komelia.updates.DesktopOnnxRuntimeInstaller
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Path.Companion.toOkioPath
import snd.komelia.db.AppSettings
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.KomeliaDatabase
import snd.komelia.db.SettingsStateActor
import snd.komelia.db.color.ExposedBookColorCorrectionRepository
import snd.komelia.db.color.ExposedColorCurvesPresetRepository
import snd.komelia.db.color.ExposedColorLevelsPresetRepository
import snd.komelia.db.fonts.ExposedUserFontsRepository
import snd.komelia.db.repository.ActorEpubReaderSettingsRepository
import snd.komelia.db.repository.ActorReaderSettingsRepository
import snd.komelia.db.repository.ActorSettingsRepository
import snd.komelia.db.settings.ExposedEpubReaderSettingsRepository
import snd.komelia.db.settings.ExposedImageReaderSettingsRepository
import snd.komelia.db.settings.ExposedSettingsRepository
import snd.komelia.image.ImageDecoder
import snd.komelia.image.OnnxRuntimeSharedLibraries
import snd.komelia.image.OnnxRuntimeUpscaler
import snd.komelia.image.SkiaBitmap
import snd.komelia.image.VipsImageDecoder
import snd.komelia.image.VipsSharedLibraries
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import snd.webview.WebviewSharedLibraries
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.createDirectories
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

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

suspend fun initDependencies(
    initScope: CoroutineScope,
    windowState: AwtWindowState,
): DesktopDependencyContainer {
    if (DesktopPlatform.Current != DesktopPlatform.Linux) {
        loadVipsLibraries()
        loadWebviewLibraries()
    }
    checkVipsLibraries()

    val database = KomeliaDatabase(AppDirectories.databaseFile.toString())
    val settingsRepository = createCommonSettingsRepository(database)
    val imageReaderRepository = createImageReaderSettingsRepository(database)
    val epubReaderSettingsRepository = createEpubReaderSettings(database)
    val fontsRepository = ExposedUserFontsRepository(database.database)
    val colorCurvesPresetsRepository = ExposedColorCurvesPresetRepository(database.database)
    val colorLevelsPresetsRepository = ExposedColorLevelsPresetRepository(database.database)
    val bookColorCorrectionRepository = ExposedBookColorCorrectionRepository(database.database)

    val secretsRepository = createSecretsRepository()

    val baseUrl = settingsRepository.getServerUrl().stateIn(initScope)
    val komfUrl = settingsRepository.getKomfUrl().stateIn(initScope)

    val okHttpWithoutCache = createOkHttpClient()
    val okHttpWithCache = okHttpWithoutCache.newBuilder()
        .cache(
            Cache(
                directory = AppDirectories.okHttpCachePath.createDirectories().toFile(),
                maxSize = 50L * 1024L * 1024L // 50 MiB
            )
        ).build()
    val ktorWithCache = createKtorClient(okHttpWithCache)
    val ktorWithoutCache = createKtorClient(okHttpWithoutCache)
    val cookiesStorage = createCookieStorage(initScope, baseUrl, secretsRepository)
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
    val onnxRuntimeInstaller = DesktopOnnxRuntimeInstaller(updateClient)
    val mangaJaNaiDownloader = DesktopMangaJaNaiDownloader(updateClient, notifications)

    val vipsDecoder = VipsImageDecoder()

    val coil = createCoil(
        ktorClient = ktorWithoutCache,
        decoder = vipsDecoder,
        url = baseUrl,
        cookiesStorage = cookiesStorage,
        tempDir = coilCachePath.createDirectories()
    )

    val onnxUpscaler = createOnnxRuntimeUpscaler(imageReaderRepository)
    val colorCorrectionStep = ColorCorrectionStep(bookColorCorrectionRepository)
    val imagePipeline = createImagePipeline(
        cropBorders = imageReaderRepository.getCropBorders().stateIn(initScope),
        colorCorrectionStep = colorCorrectionStep
    )
    val readerImageFactory = createReaderImageFactory(
        imagePreprocessingPipeline = imagePipeline,
        onnxRuntimeUpscaler = onnxUpscaler,
        settings = imageReaderRepository,
        stateFlowScope = initScope
    )

    val readerImageLoader = createReaderImageLoader(
        baseUrl = baseUrl,
        ktorClient = ktorWithoutCache,
        cookiesStorage = cookiesStorage,
        vipsDecoder = vipsDecoder
    )

    val komfClientFactory = KomfClientFactory.Builder()
        .baseUrl { komfUrl.value }
        .ktor(ktorWithCache)
        .build()

    return DesktopDependencyContainer(
        settingsRepository = settingsRepository,
        epubReaderSettingsRepository = epubReaderSettingsRepository,
        imageReaderSettingsRepository = imageReaderRepository,
        fontsRepository = fontsRepository,
        colorCurvesPresetsRepository = colorCurvesPresetsRepository,
        colorLevelsPresetRepository = colorLevelsPresetsRepository,
        bookColorCorrectionRepository = bookColorCorrectionRepository,
        secretsRepository = secretsRepository,

        komgaClientFactory = komgaClientFactory,
        appUpdater = appUpdater,
        coilImageLoader = coil,
        bookImageLoader = readerImageLoader,
        readerImageFactory = readerImageFactory,
        appNotifications = notifications,
        komfClientFactory = komfClientFactory,
        windowState = windowState,
        imageDecoder = vipsDecoder,
        colorCorrectionStep = colorCorrectionStep,
        mangaJaNaiDownloader = mangaJaNaiDownloader,
        onnxRuntimeInstaller = onnxRuntimeInstaller,
        onnxRuntime = onnxUpscaler,
    )
}

private fun checkVipsLibraries() {
    VipsSharedLibraries.loadError?.let {
        throw NonRestartableException("Failed to load libvips shared libraries. ${it.message}", it)
    }
    if (!VipsSharedLibraries.isAvailable)
        throw NonRestartableException("libvips shared libraries were not loaded. libvips is required for image decoding")
    SkiaBitmap.load()
}

private fun createOnnxRuntimeUpscaler(settingsRepository: ImageReaderSettingsRepository): ManagedOnnxUpscaler? {
    return measureTimedValue {
        try {
            OnnxRuntimeSharedLibraries.load()
            ManagedOnnxUpscaler(settingsRepository).also {
                it.initialize()
                Runtime.getRuntime().addShutdownHook(thread(start = false) { it.clearCache() })
            }
        } catch (e: UnsatisfiedLinkError) {
            logger.error(e) { "Couldn't load ONNX Runtime. ONNX upscaling will not work" }
            null
        } catch (e: OnnxRuntimeUpscaler.OrtException) {
            logger.error(e) { "Couldn't load ONNX Runtime. ONNX upscaling will not work" }
            null
        }

    }.also { logger.info { "completed ONNX Runtime load in ${it.duration}" } }
        .value
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

private suspend fun createCookieStorage(
    scope: CoroutineScope,
    baseUrl: Flow<String>,
    secretsRepository: SecretsRepository
): CookiesStorage {
    val cookiesStorage = RememberMePersistingCookieStore(
        baseUrl.map { Url(it) }.stateIn(scope),
        secretsRepository
    )
    measureTime { cookiesStorage.loadRememberMeCookie() }
        .also { logger.info { "loaded remember-me cookie from keyring in $it" } }

    return cookiesStorage
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
    cookiesStorage: CookiesStorage,
): KomgaClientFactory {
    return KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .cookieStorage(cookiesStorage)
        .build()
}

private fun createImagePipeline(
    cropBorders: StateFlow<Boolean>,
    colorCorrectionStep: ColorCorrectionStep,
): ImageProcessingPipeline {
    val pipeline = ImageProcessingPipeline()
    pipeline.addStep(colorCorrectionStep)

    pipeline.addStep(CropBordersStep(cropBorders))
    return pipeline
}

private fun createReaderImageLoader(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
    cookiesStorage: CookiesStorage,
    vipsDecoder: VipsImageDecoder
): BookImageLoader {
    val bookClient = KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .cookieStorage(cookiesStorage)
        .build()
        .bookClient()

    return BookImageLoader(
        bookClient,
        vipsDecoder,
        DiskCache.Builder()
            .directory(readerCachePath.createDirectories().toOkioPath())
            .build()
    )
}

private fun createCoil(
    ktorClient: HttpClient,
    url: StateFlow<String>,
    decoder: ImageDecoder,
    cookiesStorage: CookiesStorage,
    tempDir: Path,
): ImageLoader {
    val timed = measureTimedValue {
        val coilKtorClient = ktorClient.config {
            defaultRequest { url(url.value) }
            install(HttpCookies) { storage = cookiesStorage }
        }
        val diskCache = DiskCache.Builder()
            .directory(tempDir.toOkioPath())
            .build()
        diskCache.clear()

        ImageLoader.Builder(PlatformContext.INSTANCE)
            .components {
                add(KomgaBookPageMapper(url))
                add(KomgaSeriesMapper(url))
                add(KomgaBookMapper(url))
                add(KomgaCollectionMapper(url))
                add(KomgaReadListMapper(url))
                add(KomgaSeriesThumbnailMapper(url))
                add(FileMapper())
                add(CoilDecoder.Factory(decoder))
                add(KtorNetworkFetcherFactory(httpClient = coilKtorClient))
            }
            .memoryCache(
                MemoryCache.Builder()
                    .maxSizeBytes(128 * 1024 * 1024) // 128 Mib
                    .build()
            )
            .diskCache { diskCache }
            .build()
            .also { loader -> SingletonImageLoader.setSafe { loader } }
    }
    logger.info { "initialized Coil in ${timed.duration}" }
    return timed.value
}

private fun createSecretsRepository(): KeyringSecretsRepository {
    return measureTimedValue { KeyringSecretsRepository(AppKeyring()) }
        .also { logger.info { "initialized keyring in ${it.duration}" } }
        .value
}

private suspend fun createCommonSettingsRepository(database: KomeliaDatabase): CommonSettingsRepository {
    val repository = ExposedSettingsRepository(database.database)

    val stateActor = SettingsStateActor(
        settings = repository.get()
            ?: AppSettings(),
        saveSettings = repository::save
    )
    return ActorSettingsRepository(stateActor)
}

private suspend fun createImageReaderSettingsRepository(database: KomeliaDatabase): ImageReaderSettingsRepository {
    val repository = ExposedImageReaderSettingsRepository(database.database)
    val stateActor = SettingsStateActor(
        settings = repository.get() ?: ImageReaderSettings(upsamplingMode = UpsamplingMode.CATMULL_ROM),
        saveSettings = repository::save
    )
    return ActorReaderSettingsRepository(stateActor)
}

private suspend fun createEpubReaderSettings(database: KomeliaDatabase): EpubReaderSettingsRepository {
    val repository = ExposedEpubReaderSettingsRepository(database.database)
    val stateActor = SettingsStateActor(
        settings = repository.get() ?: EpubReaderSettings(),
        saveSettings = repository::save
    )
    return ActorEpubReaderSettingsRepository(stateActor)
}

private suspend fun createReaderImageFactory(
    imagePreprocessingPipeline: ImageProcessingPipeline,
    onnxRuntimeUpscaler: ManagedOnnxUpscaler?,
    settings: ImageReaderSettingsRepository,
    stateFlowScope: CoroutineScope,
): DesktopReaderImageFactory {
    return DesktopReaderImageFactory(
        downSamplingKernel = settings.getDownsamplingKernel().stateIn(stateFlowScope),
        upsamplingMode = settings.getUpsamplingMode().stateIn(stateFlowScope),
        linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(stateFlowScope),
        processingPipeline = imagePreprocessingPipeline,
        stretchImages = settings.getStretchToFit().stateIn(stateFlowScope),
        onnxUpscaler = onnxRuntimeUpscaler,
    )
}
