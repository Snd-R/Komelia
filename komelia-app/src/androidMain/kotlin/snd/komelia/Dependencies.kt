package snd.komelia

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AndroidDependencyContainer
import io.github.snd_r.komelia.fonts.fontsDirectory
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.http.komeliaUserAgent
import io.github.snd_r.komelia.image.AndroidReaderImageFactory
import io.github.snd_r.komelia.image.BookImageLoader
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
import io.github.snd_r.komelia.platform.AndroidWindowState
import io.github.snd_r.komelia.settings.AndroidSecretsRepository
import io.github.snd_r.komelia.settings.AppSettingsSerializer
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.updates.AndroidAppUpdater
import io.github.snd_r.komelia.updates.UpdateClient
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.FileSystem
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
import snd.komelia.image.AndroidSharedLibrariesLoader
import snd.komelia.image.ImageDecoder
import snd.komelia.image.VipsImageDecoder
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

suspend fun initDependencies(
    initScope: CoroutineScope,
    context: Context,
    mainActivity: StateFlow<Activity?>,
): AndroidDependencyContainer {
    measureTime {
        try {
            AndroidSharedLibrariesLoader.load()
        } catch (e: UnsatisfiedLinkError) {
            logger.error(e) { "Couldn't load vips shared libraries. reader image loading will not work" }
        }
    }.also { logger.info { "completed vips libraries load in $it" } }

    fontsDirectory = Path(context.filesDir.resolve("fonts").absolutePath)

    val datastore = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { context.dataStoreFile("settings.pb") },
        corruptionHandler = null,
    )
    val secretsRepository = AndroidSecretsRepository(datastore)

    val database = KomeliaDatabase(context.filesDir.resolve("komelia.sqlite").absolutePath)
    val settingsRepository = createCommonSettingsRepository(database)
    val imageReaderSettingsRepository = createImageReaderSettingsRepository(database)
    val epubReaderSettingsRepository = createEpubReaderSettings(database)
    val fontsRepository = ExposedUserFontsRepository(database.database)
    val colorCurvesPresetsRepository = ExposedColorCurvesPresetRepository(database.database)
    val colorLevelsPresetsRepository = ExposedColorLevelsPresetRepository(database.database)
    val bookColorCorrectionRepository = ExposedBookColorCorrectionRepository(database.database)

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

    val colorCorrectionStep = ColorCorrectionStep(bookColorCorrectionRepository)
    val imagePipeline = createImagePipeline(
        cropBorders = imageReaderSettingsRepository.getCropBorders().stateIn(initScope),
        colorCorrectionStep = colorCorrectionStep
    )
    val readerImageFactory = createReaderImageFactory(
        imagePreprocessingPipeline = imagePipeline,
        settings = imageReaderSettingsRepository,
        stateFlowScope = initScope
    )

    val vipsDecoder = VipsImageDecoder()
    val readerImageLoader = createReaderImageLoader(
        baseUrl = baseUrl,
        ktorClient = ktorWithoutCache,
        cookiesStorage = cookiesStorage,
        decoder = vipsDecoder
    )

    val coil = createCoil(
        ktorClient = ktorWithoutCache,
        url = baseUrl,
        cookiesStorage = cookiesStorage,
        decoder = vipsDecoder,
        context = context
    )
    SingletonImageLoader.setSafe { coil }

    val komfClientFactory = KomfClientFactory.Builder()
        .baseUrl { komfUrl.value }
        .ktor(ktorWithCache)
        .build()

    val appUpdater = createAppUpdater(ktorWithCache, ktorWithoutCache, context)
    return AndroidDependencyContainer(
        settingsRepository = settingsRepository,
        epubReaderSettingsRepository = epubReaderSettingsRepository,
        imageReaderSettingsRepository = imageReaderSettingsRepository,
        fontsRepository = fontsRepository,
        colorCurvesPresetsRepository = colorCurvesPresetsRepository,
        colorLevelsPresetRepository = colorLevelsPresetsRepository,
        bookColorCorrectionRepository = bookColorCorrectionRepository,
        secretsRepository = secretsRepository,

        appUpdater = appUpdater,
        komgaClientFactory = komgaClientFactory,
        coilImageLoader = coil,
        platformContext = context,
        bookImageLoader = readerImageLoader,
        komfClientFactory = komfClientFactory,
        windowState = AndroidWindowState(mainActivity),
        imageDecoder = vipsDecoder,
        colorCorrectionStep = colorCorrectionStep,
        readerImageFactory = readerImageFactory,
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
    decoder: ImageDecoder,
): BookImageLoader {
    val bookClient = KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .cookieStorage(cookiesStorage)
        .build()
        .bookClient()
    return BookImageLoader(
        bookClient = bookClient,
        decoder = decoder,
        diskCache = DiskCache.Builder()
            .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "komelia_reader_cache")
            .build()
    )
}

private fun createCoil(
    ktorClient: HttpClient,
    url: StateFlow<String>,
    cookiesStorage: RememberMePersistingCookieStore,
    decoder: ImageDecoder,
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
            add(CoilDecoder.Factory(decoder))
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

private fun createImagePipeline(
    cropBorders: StateFlow<Boolean>,
    colorCorrectionStep: ColorCorrectionStep,
): ImageProcessingPipeline {
    val pipeline = ImageProcessingPipeline()
    pipeline.addStep(colorCorrectionStep)
    pipeline.addStep(CropBordersStep(cropBorders))
    return pipeline
}

private suspend fun createCommonSettingsRepository(database: KomeliaDatabase): CommonSettingsRepository {
    val repository = ExposedSettingsRepository(database.database)

    val stateActor = SettingsStateActor(
        settings = repository.get() ?: AppSettings(cardWidth = 150),
        saveSettings = repository::save
    )
    return ActorSettingsRepository(stateActor)
}

private suspend fun createImageReaderSettingsRepository(database: KomeliaDatabase): ImageReaderSettingsRepository {
    val repository = ExposedImageReaderSettingsRepository(database.database)
    val stateActor = SettingsStateActor(
        settings = repository.get() ?: ImageReaderSettings(upsamplingMode = UpsamplingMode.BILINEAR),
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
    settings: ImageReaderSettingsRepository,
    stateFlowScope: CoroutineScope,
): AndroidReaderImageFactory {
    return AndroidReaderImageFactory(
        downSamplingKernel = settings.getDownsamplingKernel().stateIn(stateFlowScope),
        upsamplingMode = settings.getUpsamplingMode().stateIn(stateFlowScope),
        linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(stateFlowScope),
        processingPipeline = imagePreprocessingPipeline,
        stretchImages = settings.getStretchToFit().stateIn(stateFlowScope),
    )
}
