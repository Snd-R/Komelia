package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.snd_r.komelia.image.ImageWorker
import io.github.snd_r.komelia.image.VipsImageDecoder
import io.github.snd_r.komelia.image.coil.BlobFetcher
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.AppSettings
import io.github.snd_r.komelia.settings.CookieStoreSecretsRepository
import io.github.snd_r.komelia.settings.LocalStorageReaderSettingsRepository
import io.github.snd_r.komelia.settings.LocalStorageSettingsRepository
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.updates.AppRelease
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.UpdateProgress
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import snd.komga.client.KomgaClientFactory

class WasmDependencyContainer(
    override val settingsRepository: SettingsRepository,
    override val readerSettingsRepository: ReaderSettingsRepository,
    override val secretsRepository: SecretsRepository,
    override val appUpdater: AppUpdater,
    override val availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    override val komgaClientFactory: KomgaClientFactory,
    override val imageLoader: ImageLoader,
) : DependencyContainer {
    override val imageLoaderContext: PlatformContext = PlatformContext.INSTANCE
    override val appNotifications: AppNotifications = AppNotifications()

    companion object {
        suspend fun createInstance(stateFlowScope: CoroutineScope): WasmDependencyContainer {
            val imageWorker = ImageWorker()
            // async loading of wasm code does not guaranty that worker is ready to receive messages after js file is loaded
            // retry until init confirmation is received
            while (!imageWorker.initialized) {
                imageWorker.init()
                delay(50)
            }

            val settings = MutableStateFlow(AppSettings.loadSettings())
            val settingsRepository = LocalStorageSettingsRepository(settings)
            val readerSettingsRepository = LocalStorageReaderSettingsRepository(settings)
            val secretsRepository = CookieStoreSecretsRepository()
            val baseUrl = settingsRepository.getServerUrl().stateIn(stateFlowScope)

            val ktorClient = createKtorClient(baseUrl)
            val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient)

            val decoderType = settingsRepository.getDecoderSettings().stateIn(stateFlowScope)
            val coil = createCoil(baseUrl, ktorClient, imageWorker)
            SingletonImageLoader.setSafe { coil }

            return WasmDependencyContainer(
                komgaClientFactory = komgaClientFactory,
                appUpdater = NoopAppUpdater,
                settingsRepository = settingsRepository,
                readerSettingsRepository = readerSettingsRepository,
                secretsRepository = secretsRepository,
                imageLoader = coil,
                availableDecoders = emptyFlow()
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
            imageWorker: ImageWorker,
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
                    add(VipsImageDecoder.Factory(imageWorker))
                    add(KtorNetworkFetcherFactory(httpClient = ktorClient))
                }
                .memoryCache(
                    MemoryCache.Builder()
                        .maxSizeBytes(128 * 1024 * 1024) // 128 Mib
                        .build()
                )
                .build()
        }


    }
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

private object NoopAppUpdater : AppUpdater {
    override suspend fun getReleases(): List<AppRelease> = emptyList()
    override suspend fun updateToLatest(): Flow<UpdateProgress>? = null
    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? = null
}
