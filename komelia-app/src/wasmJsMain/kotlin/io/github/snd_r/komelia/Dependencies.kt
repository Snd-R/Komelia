package io.github.snd_r.komelia

import WasmDependencyContainer
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ReaderImageFactory
import io.github.snd_r.komelia.image.WasmReaderImageFactory
import io.github.snd_r.komelia.image.coil.BlobFetcher
import io.github.snd_r.komelia.image.coil.CoilDecoder
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageThumbnailMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.image.processing.ColorCorrectionStep
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import io.github.snd_r.komelia.platform.BrowserWindowState
import io.github.snd_r.komelia.settings.CookieStoreSecretsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import snd.komelia.db.SettingsStateActor
import snd.komelia.db.color.IDBBookColorCorrectionRepository
import snd.komelia.db.color.IDBColorCurvesPresetRepository
import snd.komelia.db.color.IDBColorLevelsPresetRepository
import snd.komelia.db.getIndexedDb
import snd.komelia.db.repository.ActorEpubReaderSettingsRepository
import snd.komelia.db.repository.ActorKomfSettingsRepository
import snd.komelia.db.repository.ActorReaderSettingsRepository
import snd.komelia.db.repository.ActorSettingsRepository
import snd.komelia.db.settings.LocalStorageSettingsRepository
import snd.komelia.db.settings.NoopFontsRepository
import snd.komelia.image.ImageDecoder
import snd.komelia.image.wasm.client.WorkerImageDecoder
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

suspend fun initDependencies(stateFlowScope: CoroutineScope): WasmDependencyContainer {
    val workerDecoder = WorkerImageDecoder()
    workerDecoder.init()

    val localStorageRepository = LocalStorageSettingsRepository()
    val appSettingsRepository = ActorSettingsRepository(
        SettingsStateActor(
            localStorageRepository.getSettings(),
            localStorageRepository::saveAppSettings
        )
    )
    val imageReaderSettingsRepository = ActorReaderSettingsRepository(
        SettingsStateActor(
            localStorageRepository.getImageReaderSettings(),
            localStorageRepository::saveImageReaderSettings
        )
    )
    val epubReaderSettingsRepository = ActorEpubReaderSettingsRepository(
        SettingsStateActor(
            localStorageRepository.getEpubReaderSettings(),
            localStorageRepository::saveEpubReaderSettings
        )
    )
    val komfSettingsRepository = ActorKomfSettingsRepository(
        SettingsStateActor(
            localStorageRepository.getKomfSettings(),
            localStorageRepository::saveKomfSettings
        )
    )
    val secretsRepository = CookieStoreSecretsRepository()

    val idb = getIndexedDb()
    val bookColorCorrectionRepository = IDBBookColorCorrectionRepository(idb)
    val curvePresetsRepository = IDBColorCurvesPresetRepository(idb)
    val levelsPresetsRepository = IDBColorLevelsPresetRepository(idb)

    val baseUrl = appSettingsRepository.getServerUrl().stateIn(stateFlowScope)
    val komfUrl = komfSettingsRepository.getKomfUrl().stateIn(stateFlowScope)
    overrideFetch { baseUrl.value }

    val ktorClient = createKtorClient(baseUrl)
    val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient)

    val coil = createCoil(baseUrl, ktorClient, workerDecoder)
    SingletonImageLoader.setSafe { coil }

    val komfClientFactory = KomfClientFactory.Builder()
        .baseUrl { komfUrl.value }
        .ktor(ktorClient)
        .build()

    val colorCorrectionStep = ColorCorrectionStep(bookColorCorrectionRepository)
    val imagePipeline = createImagePipeline(colorCorrectionStep)

    val readerImageFactory = createReaderImageFactory(
        imagePreprocessingPipeline = imagePipeline,
        settings = imageReaderSettingsRepository,
        imageDecoder = workerDecoder,
        stateFlowScope = stateFlowScope
    )
    val readerImageLoader = createReaderImageLoader(
        baseUrl = baseUrl,
        ktorClient = ktorClient,
        decoder = workerDecoder,
        imageFactory = readerImageFactory
    )

    return WasmDependencyContainer(
        settingsRepository = appSettingsRepository,
        epubReaderSettingsRepository = epubReaderSettingsRepository,
        imageReaderSettingsRepository = imageReaderSettingsRepository,
        fontsRepository = NoopFontsRepository(),
        secretsRepository = secretsRepository,
        komfSettingsRepository = komfSettingsRepository,
        colorCurvesPresetsRepository = curvePresetsRepository,
        colorLevelsPresetRepository = levelsPresetsRepository,
        bookColorCorrectionRepository = bookColorCorrectionRepository,

        komgaClientFactory = komgaClientFactory,
        komfClientFactory = komfClientFactory,
        appUpdater = null,
        coilImageLoader = coil,
        bookImageLoader = readerImageLoader,
        windowState = BrowserWindowState(),
        imageDecoder = workerDecoder,
        readerImageFactory = readerImageFactory,
        colorCorrectionStep = colorCorrectionStep
    )
}

private fun createKtorClient(
    baseUrl: StateFlow<String>,
): HttpClient {
    return HttpClient(Js) {
        defaultRequest { url(baseUrl.value) }
        expectSuccess = true
        followRedirects = false
    }
}

private fun createKomgaClientFactory(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
): KomgaClientFactory {

    return KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .build()
}

private fun createCoil(
    url: StateFlow<String>,
    ktorClient: HttpClient,
    imageWorker: WorkerImageDecoder,
): ImageLoader {
    return ImageLoader.Builder(PlatformContext.INSTANCE)
        .components {
            add(KomgaBookPageMapper(url))
            add(KomgaBookPageThumbnailMapper(url))
            add(KomgaSeriesMapper(url))
            add(KomgaBookMapper(url))
            add(KomgaCollectionMapper(url))
            add(KomgaReadListMapper(url))
            add(KomgaSeriesThumbnailMapper(url))
            add(BlobFetcher.Factory())
            add(CoilDecoder.Factory(imageWorker))
            add(KtorNetworkFetcherFactory(httpClient = ktorClient))
        }
        .memoryCache(
            MemoryCache.Builder()
                .maxSizeBytes(64 * 1024 * 1024) // 64 Mib
                .build()
        )
        .build()
}

private fun createReaderImageLoader(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
    decoder: WorkerImageDecoder,
    imageFactory: ReaderImageFactory
): BookImageLoader {
    val bookClient = KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .build()
        .bookClient()

    return BookImageLoader(
        bookClient = bookClient,
        imageDecoder = decoder,
        readerImageFactory = imageFactory,
        diskCache = null
    )
}

private fun createImagePipeline(
    colorCorrectionStep: ColorCorrectionStep
): ImageProcessingPipeline {
    val pipeline = ImageProcessingPipeline()
    pipeline.addStep(colorCorrectionStep)
    return pipeline
}

private suspend fun createReaderImageFactory(
    imagePreprocessingPipeline: ImageProcessingPipeline,
    settings: ImageReaderSettingsRepository,
    imageDecoder: ImageDecoder,
    stateFlowScope: CoroutineScope,
): WasmReaderImageFactory {
    return WasmReaderImageFactory(
        imageDecoder = imageDecoder,
        downSamplingKernel = settings.getDownsamplingKernel().stateIn(stateFlowScope),
        upsamplingMode = settings.getUpsamplingMode().stateIn(stateFlowScope),
        linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(stateFlowScope),
        processingPipeline = imagePreprocessingPipeline,
        stretchImages = settings.getStretchToFit().stateIn(stateFlowScope),
    )
}

private fun overrideFetch(komgaUrl: () -> String) {
    js(
        """
    window.originalFetch = window.fetch;
    window.fetch = function (resource, init) {
        init = Object.assign({}, init);
        if(typeof resource =='string' && resource.startsWith(komgaUrl())) {
            init.headers = Object.assign( { 'X-Requested-With' : 'XMLHttpRequest' }, init.headers) 
            init.credentials = 'include';
        } 
        return window.originalFetch(resource, init);
    };
"""
    )
}
