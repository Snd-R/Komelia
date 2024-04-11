package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.snd_r.komelia.image.Vips
import io.github.snd_r.komelia.image.VipsImageDecoder
import io.github.snd_r.komelia.image.coil.BlobFetcher
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.settings.CookieStoreSecretsRepository
import io.github.snd_r.komelia.settings.LocalStorageSettingsRepository
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private val stateFlowScope = CoroutineScope(Dispatchers.Default)

actual suspend fun createViewModelFactory(context: PlatformContext): ViewModelFactory {
    val vips = loadVips()

    val settingsRepository = LocalStorageSettingsRepository()
    val secretsRepository = CookieStoreSecretsRepository()
    val baseUrl = settingsRepository.getServerUrl().stateIn(stateFlowScope)

    val ktorClient = createKtorClient(baseUrl)
    val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient)

    val coil = createCoil(baseUrl, ktorClient, vips)
    SingletonImageLoader.setSafe { coil }

    return ViewModelFactory(
        komgaClientFactory = komgaClientFactory,
        settingsRepository = settingsRepository,
        secretsRepository = secretsRepository,
        imageLoader = coil,
        imageLoaderContext = context,
    )
}

private fun createKtorClient(
    baseUrl: StateFlow<String>,
): HttpClient {
    overrideFetch()
    return HttpClient(Js) {
        defaultRequest { url(baseUrl.value) }
        install(HttpCache)
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
    vips: JsAny
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
            add(VipsImageDecoder.Factory(vips))
            add(KtorNetworkFetcherFactory(httpClient = ktorClient))
        }
        .memoryCache(
            MemoryCache.Builder()
                .maxSizeBytes(128 * 1024 * 1024) // 128 Mib
                .build()
        )
        .build()
}

private fun overrideFetch() {
    js(
        """
    window.originalFetch = window.fetch;
    window.fetch = function (resource, init) {
        init = Object.assign({}, init);
        init.headers = Object.assign( { 'X-Requested-With' : 'XMLHttpRequest' }, init.headers) 
        init.credentials = init.credentials !== undefined ? init.credentials : 'include';
        return window.originalFetch(resource, init);
    };
"""
    )
}

private suspend fun loadVips(): JsAny {
    val config = vipsConfig()
    return Vips(config).asDeferred<JsAny>().await()
}

private fun vipsConfig(): JsAny {
    js(
        """
    return {
                dynamicLibraries: [],
                mainScriptUrlOrBlob: './vips.js',
                locateFile: (fileName, scriptDirectory) => fileName,
            };
    """
    )
}

