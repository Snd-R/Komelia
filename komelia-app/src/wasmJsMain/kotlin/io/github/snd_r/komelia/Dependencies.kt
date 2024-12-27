package io.github.snd_r.komelia

import WasmDependencyContainer
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.snd_r.komelia.image.CropBordersStep
import io.github.snd_r.komelia.image.ImageProcessingPipeline
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.image.WasmReaderImageFactory
import io.github.snd_r.komelia.image.coil.BlobFetcher
import io.github.snd_r.komelia.image.coil.CoilDecoder
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.platform.BrowserWindowState
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.CookieStoreSecretsRepository
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import snd.komelia.db.SettingsStateActor
import snd.komelia.db.repository.ActorEpubReaderSettingsRepository
import snd.komelia.db.repository.ActorReaderSettingsRepository
import snd.komelia.db.repository.ActorSettingsRepository
import snd.komelia.db.settings.LocalStorageSettingsRepository
import snd.komelia.db.settings.NoopFontsRepository
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
    val secretsRepository = CookieStoreSecretsRepository()
    val baseUrl = appSettingsRepository.getServerUrl().stateIn(stateFlowScope)
    val komfUrl = appSettingsRepository.getKomfUrl().stateIn(stateFlowScope)

    overrideFetch { baseUrl.value }

    val ktorClient = createKtorClient(baseUrl)
    val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient)

    val coil = createCoil(baseUrl, ktorClient, workerDecoder)
    SingletonImageLoader.setSafe { coil }

    val komfClientFactory = KomfClientFactory.Builder()
        .baseUrl { komfUrl.value }
        .ktor(ktorClient)
        .build()

    val imagePipeline = createImagePipeline(
        cropBorders = imageReaderSettingsRepository.getCropBorders().stateIn(stateFlowScope)
    )
    val decoderSettings = appSettingsRepository.getDecoderSettings().stateIn(stateFlowScope)
    val readerImageLoader = createReaderImageLoader(
        baseUrl = baseUrl,
        ktorClient = ktorClient,
        upscaleOption = decoderSettings.map { it.upscaleOption }.stateIn(stateFlowScope),
        pipeline = imagePipeline,
        stretchImages = imageReaderSettingsRepository.getStretchToFit().stateIn(stateFlowScope),
        showDebugGrid = appSettingsRepository.getImageReaderShowDebugGrid().stateIn(stateFlowScope),
        decoder = workerDecoder,
    )

    return WasmDependencyContainer(
        settingsRepository = appSettingsRepository,
        epubReaderSettingsRepository = epubReaderSettingsRepository,
        imageReaderSettingsRepository = imageReaderSettingsRepository,
        fontsRepository = NoopFontsRepository(),
        secretsRepository = secretsRepository,
        komgaClientFactory = komgaClientFactory,
        komfClientFactory = komfClientFactory,
        appUpdater = null,
        imageDecoderDescriptor = emptyFlow(),
        imageLoader = coil,
        readerImageLoader = readerImageLoader,
        windowState = BrowserWindowState()
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
    upscaleOption: StateFlow<UpscaleOption>,
    pipeline: ImageProcessingPipeline,
    showDebugGrid: StateFlow<Boolean>,
    stretchImages: StateFlow<Boolean>,
    decoder: WorkerImageDecoder,
): ReaderImageLoader {
    val bookClient = KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .build()
        .bookClient()

    return ReaderImageLoader(
        bookClient = bookClient,
        decoder = WasmReaderImageFactory(
            upscaleOptionFlow = upscaleOption,
            stretchImages = stretchImages,
            processingPipeline = pipeline,
            showDebugGrid = showDebugGrid,
            decoder = decoder,

            ),
        diskCache = null
    )
}

private fun createImagePipeline(
    cropBorders: StateFlow<Boolean>,
): ImageProcessingPipeline {
    val pipeline = ImageProcessingPipeline()
    pipeline.addStep(CropBordersStep(cropBorders))
    return pipeline
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
