package snd.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import snd.komelia.api.RemoteActuatorApi
import snd.komelia.api.RemoteAnnouncementsApi
import snd.komelia.api.RemoteApi
import snd.komelia.api.RemoteBookApi
import snd.komelia.api.RemoteCollectionsApi
import snd.komelia.api.RemoteFileSystemApi
import snd.komelia.api.RemoteLibraryApi
import snd.komelia.api.RemoteReadListApi
import snd.komelia.api.RemoteReferentialApi
import snd.komelia.api.RemoteSeriesApi
import snd.komelia.api.RemoteSettingsApi
import snd.komelia.api.RemoteTaskApi
import snd.komelia.api.RemoteUserApi
import snd.komelia.http.RememberMePersistingCookieStore
import snd.komelia.image.BookImageLoader
import snd.komelia.image.KomeliaImageDecoder
import snd.komelia.image.KomeliaPanelDetector
import snd.komelia.image.KomeliaUpscaler
import snd.komelia.image.ReaderImageFactory
import snd.komelia.image.coil.CoilAwareDecoder
import snd.komelia.image.coil.CoilDecoder
import snd.komelia.image.coil.FileMapper
import snd.komelia.image.coil.KomeliaFetcherFactory
import snd.komelia.image.processing.ColorCorrectionStep
import snd.komelia.image.processing.CropBordersStep
import snd.komelia.image.processing.ImageProcessingPipeline
import snd.komelia.komga.api.KomgaApi
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.offline.OfflineDependencies
import snd.komelia.offline.OfflineModule
import snd.komelia.offline.OfflineRepositories
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.ui.DependencyContainer
import snd.komelia.ui.strings.EnStrings
import snd.komelia.updates.AppUpdater
import snd.komelia.updates.OnnxModelDownloader
import snd.komelia.updates.OnnxRuntimeInstaller
import snd.komelia.updates.UpdateClient
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUser
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger { }

abstract class AppModule {
    protected val initScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    protected val appNotifications = AppNotifications()

    suspend fun initDependencies(): DependencyContainer {
        beforeInit()
        val appRepositories = createAppRepositories()
        val offlineRepositories = createOfflineRepositories()
        val ktor = createKtorClient()
        val ktorWithoutCache = createKtorClientWithoutCache()

        val updateClient = UpdateClient(
            ktor = ktor.config {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            },
            ktorWithoutCache = ktorWithoutCache.config {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        )

        val baseUrl = appRepositories.settingsRepository.getServerUrl().stateIn(initScope)
        val komfUrl = appRepositories.komfSettingsRepository.getKomfUrl().stateIn(initScope)

        val cookiesStorage = RememberMePersistingCookieStore(
            baseUrl.map { Url(it) }.stateIn(initScope),
            appRepositories.secretsRepository
        )
        cookiesStorage.loadRememberMeCookie()

        val komgaClientFactory = KomgaClientFactory.Builder()
            .ktor(ktor)
            .baseUrl { baseUrl.value }
            .cookieStorage(cookiesStorage)
            .build()

        val komgaClientFactoryNoCache = KomgaClientFactory.Builder()
            .ktor(ktor)
            .baseUrl { baseUrl.value }
            .cookieStorage(cookiesStorage)
            .build()

        val komfClientFactory = KomfClientFactory.Builder()
            .baseUrl { komfUrl.value }
            .ktor(ktor)
            .build()

        val imageDecoder = createImageDecoder()

        val isOffline = offlineRepositories.offlineSettingsRepository.getOfflineMode().stateIn(initScope)
        val currentUserFlow = MutableStateFlow<KomgaUser?>(null)
        val currentServerUrl = appRepositories.settingsRepository.getServerUrl().stateIn(initScope)

        val androidContext = createCoilContext()
        val offlineModule: OfflineDependencies = createOfflineModule(
            repositories = offlineRepositories,
            komgaClientFactory = komgaClientFactory,
            onlineUser = currentUserFlow
                .combine(isOffline) { user, isOffline -> if (isOffline) null else user }
                .stateIn(initScope),
            onlineServerUrl = appRepositories.settingsRepository.getServerUrl().stateIn(initScope),
            isOffline = isOffline,
        ).initDependencies()

        val komgaApi = isOffline.map { offline ->
            if (offline) offlineModule.komgaApi
            else createRemoteApi(
                komgaClientFactory = komgaClientFactory,
                offlineRepositories = offlineRepositories,
                offlineEvents = offlineModule.komgaEvents
            )
        }.stateIn(initScope)

        val komgaNoRemoteCacheApi = isOffline.map { offline ->
            if (offline) offlineModule.komgaApi
            else createRemoteApi(
                komgaClientFactory = komgaClientFactoryNoCache,
                offlineRepositories = offlineRepositories,
                offlineEvents = offlineModule.komgaEvents
            )
        }.stateIn(initScope)

        val komgaSharedState = KomgaAuthenticationState(
            userApi = komgaApi.map { it.userApi }.stateIn(initScope),
            libraryApi = komgaApi.map { it.libraryApi }.stateIn(initScope),
            currentUserFlow = currentUserFlow,
            serverUrl = currentServerUrl
        )


        val colorCorrectionStep = ColorCorrectionStep(appRepositories.bookColorCorrectionRepository)
        val imagePipeline = createImagePipeline(
            cropBorders = appRepositories.imageReaderSettingsRepository.getCropBorders().stateIn(initScope),
            colorCorrectionStep = colorCorrectionStep
        )
        val onnxRuntimeInstaller = createOnnxRuntimeInstaller(updateClient)
        val onnxModelDownloader = createOnnxModelDownloader(updateClient)
        val onnxRuntime = createOnnxRuntime()

        val upscaler = if (onnxRuntime != null && onnxModelDownloader != null) {
            createUpscaler(
                onnxRuntime,
                onnxModelDownloader,
                appRepositories.imageReaderSettingsRepository
            )
        } else null

        val panelDetector = if (onnxRuntime != null && onnxModelDownloader != null) {
            createPanelDetector(
                onnxRuntime,
                onnxModelDownloader,
                appRepositories.imageReaderSettingsRepository
            )
        } else null

        val coil = createCoil(
            komgaApi = komgaApi,
            context = androidContext,
            decoder = imageDecoder,
        )

        val komgaEvents = ManagedKomgaEvents(
            komgaApi = komgaApi,
            memoryCache = coil.memoryCache,
            diskCache = coil.diskCache,
            libraryApi = komgaApi.map { it.libraryApi },
            komgaSharedState = komgaSharedState
        )

        val readerImageFactory = createReaderImageFactory(
            imageDecoder = imageDecoder,
            pipeline = imagePipeline,
            settings = appRepositories.imageReaderSettingsRepository,
            onnxRuntimeUpscaler = upscaler,
        )

        return DependencyContainer(
            appStrings = MutableStateFlow(EnStrings),
            appRepositories = appRepositories,

            komgaApi = komgaApi,
            isOffline = isOffline,
            komfClientFactory = komfClientFactory,
            appNotifications = appNotifications,
            komgaSharedState = komgaSharedState,
            komgaEvents = komgaEvents,
            appUpdater = createAppUpdater(updateClient),

            coilContext = androidContext,
            coilImageLoader = coil,
            imageDecoder = imageDecoder,
            bookImageLoader = createReaderImageLoader(
                bookApi = komgaNoRemoteCacheApi.map { it.bookApi }.stateIn(initScope),
                imageFactory = readerImageFactory,
                imageDecoder = createImageDecoder()
            ),
            readerImageFactory = readerImageFactory,
            windowState = createWindowState(),
            colorCorrectionStep = colorCorrectionStep,
            onnxRuntimeInstaller = onnxRuntimeInstaller,
            onnxModelDownloader = onnxModelDownloader,
            onnxRuntime = onnxRuntime,
            upscaler = upscaler,
            panelDetector = panelDetector,
            offlineDependencies = offlineModule,
        )
    }

    protected open suspend fun beforeInit() = Unit

    protected fun createRemoteApi(
        komgaClientFactory: KomgaClientFactory,
        offlineRepositories: OfflineRepositories,
        offlineEvents: SharedFlow<KomgaEvent>,
    ) = RemoteApi(
        actuatorApi = RemoteActuatorApi(komgaClientFactory.actuatorClient()),
        announcementsApi = RemoteAnnouncementsApi(komgaClientFactory.announcementClient()),
        bookApi = RemoteBookApi(
            bookClient = komgaClientFactory.bookClient(),
            offlineBookRepository = offlineRepositories.bookRepository
        ),
        collectionsApi = RemoteCollectionsApi(komgaClientFactory.collectionClient()),
        fileSystemApi = RemoteFileSystemApi(komgaClientFactory.fileSystemClient()),
        libraryApi = RemoteLibraryApi(komgaClientFactory.libraryClient()),
        readListApi = RemoteReadListApi(
            readListClient = komgaClientFactory.readListClient(),
            offlineBookRepository = offlineRepositories.bookRepository
        ),
        referentialApi = RemoteReferentialApi(komgaClientFactory.referentialClient()),
        seriesApi = RemoteSeriesApi(komgaClientFactory.seriesClient()),
        settingsApi = RemoteSettingsApi(komgaClientFactory.settingsClient()),
        tasksApi = RemoteTaskApi(komgaClientFactory.taskClient()),
        userApi = RemoteUserApi(komgaClientFactory.userClient()),
        komgaClientFactory = komgaClientFactory,
        offlineEvents = offlineEvents
    )

    protected fun createCoil(
        komgaApi: StateFlow<KomgaApi>,
        context: PlatformContext,
        decoder: KomeliaImageDecoder,
    ): ImageLoader {

        val timed = measureTimedValue {
            val diskCache = getCoilCacheDirectory()?.let { kotlinxPath ->
                DiskCache.Builder()
                    // kotlinx -> okio path
                    .directory(kotlinxPath.toString().toPath())
                    .build()
            }
            diskCache?.clear()
            val coilAwareDecoder = CoilAwareDecoder(decoder)

            ImageLoader.Builder(context)
                .components {
                    add(FileMapper())
                    add(CoilDecoder.Factory(coilAwareDecoder))
                    add(KomeliaFetcherFactory(komgaApi, coilAwareDecoder))
                }
                .memoryCache(createCoilMemoryCache())
                .diskCache { diskCache }
                .build()
                .also { loader -> SingletonImageLoader.setSafe { loader } }
        }
        logger.info { "initialized Coil in ${timed.duration}" }
        return timed.value
    }

    protected fun createReaderImageLoader(
        bookApi: StateFlow<KomgaBookApi>,
        imageFactory: ReaderImageFactory,
        imageDecoder: KomeliaImageDecoder
    ): BookImageLoader {
        val diskCache = getReaderCacheDirectory()?.let { kotlinxPath ->
            DiskCache.Builder()
                .directory(kotlinxPath.toString().toPath())
                .build()
        }
        return BookImageLoader(
            bookClient = bookApi,
            readerImageFactory = imageFactory,
            imageDecoder = imageDecoder,
            diskCache = diskCache
        )
    }

    protected fun createImagePipeline(
        cropBorders: StateFlow<Boolean>,
        colorCorrectionStep: ColorCorrectionStep,
    ): ImageProcessingPipeline {
        val pipeline = ImageProcessingPipeline()
        pipeline.addStep(colorCorrectionStep)

        pipeline.addStep(CropBordersStep(cropBorders))
        return pipeline
    }


    protected abstract suspend fun createAppRepositories(): AppRepositories
    protected abstract suspend fun createOfflineRepositories(): OfflineRepositories
    protected abstract fun createKtorClient(): HttpClient
    protected abstract fun createKtorClientWithoutCache(): HttpClient

    protected abstract fun createAppUpdater(updateClient: UpdateClient): AppUpdater?

    protected abstract fun createImageDecoder(): KomeliaImageDecoder
    protected abstract suspend fun createReaderImageFactory(
        imageDecoder: KomeliaImageDecoder,
        pipeline: ImageProcessingPipeline,
        settings: ImageReaderSettingsRepository,
        onnxRuntimeUpscaler: KomeliaUpscaler?,
    ): ReaderImageFactory

    protected abstract fun createWindowState(): AppWindowState
    protected abstract fun createCoilContext(): PlatformContext
    protected abstract fun createOnnxRuntimeInstaller(updateClient: UpdateClient): OnnxRuntimeInstaller?
    protected abstract fun createOnnxModelDownloader(updateClient: UpdateClient): OnnxModelDownloader?
    protected abstract fun createOnnxRuntime(): OnnxRuntime?
    protected abstract suspend fun createUpscaler(
        onnxRuntime: OnnxRuntime,
        modelDownloader: OnnxModelDownloader,
        settings: ImageReaderSettingsRepository,
    ): KomeliaUpscaler?

    protected abstract suspend fun createPanelDetector(
        onnxRuntime: OnnxRuntime,
        modelDownloader: OnnxModelDownloader,
        settings: ImageReaderSettingsRepository,
    ): KomeliaPanelDetector?

    protected abstract fun getCoilCacheDirectory(): Path?
    protected abstract fun createCoilMemoryCache(): MemoryCache?
    protected abstract fun getReaderCacheDirectory(): Path?

    protected abstract fun createOfflineModule(
        repositories: OfflineRepositories,
        onlineUser: StateFlow<KomgaUser?>,
        onlineServerUrl: StateFlow<String>,
        isOffline: StateFlow<Boolean>,
        komgaClientFactory: KomgaClientFactory,
    ): OfflineModule
}
