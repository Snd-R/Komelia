package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
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
import io.ktor.client.plugins.cookies.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private val stateFlowScope = CoroutineScope(Dispatchers.Default)
private val logger = KotlinLogging.logger {}

actual suspend fun createViewModelFactory(context: PlatformContext): ViewModelFactory {

    val settingsRepository = LocalStorageSettingsRepository()
    val secretsRepository = CookieStoreSecretsRepository()
    val baseUrl = settingsRepository.getServerUrl().stateIn(stateFlowScope)

    val cookiesStorage = RememberMePersistingCookieStore(baseUrl, secretsRepository)
    cookiesStorage.loadRememberMeCookie()

    val ktorClient = createKtorClient(baseUrl, cookiesStorage)
    logger.info { ktorClient.engine }
    val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient, cookiesStorage)

    logger.info { ktorClient.pluginOrNull(HttpCookies) }
    val coil = createCoil(baseUrl, ktorClient)
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
    cookiesStorage: RememberMePersistingCookieStore,
): HttpClient {
    fetchIncludeCredentials()
    return HttpClient(Js) {
        defaultRequest { url(baseUrl.value) }
        install(HttpCookies) { storage = cookiesStorage }
        install(HttpCache)
        expectSuccess = true
        followRedirects = false
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

private fun createCoil(
    url: StateFlow<String>,
    ktorClient: HttpClient,
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
            add(KtorNetworkFetcherFactory(httpClient = ktorClient))
        }
        .build()
}

private fun fetchIncludeCredentials() {
    js(
        """
    window.originalFetch = window.fetch;
    window.fetch = function (resource, init) {
        init = Object.assign({}, init);
        init.credentials = init.credentials !== undefined ? init.credentials : 'include';
        return window.originalFetch(resource, init);
    };
"""
    )
}