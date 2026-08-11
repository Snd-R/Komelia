package snd.komelia

import coil3.PlatformContext
import coil3.memory.MemoryCache
import com.juul.indexeddb.Database
import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import snd.komelia.db.SettingsStateWrapper
import snd.komelia.db.color.IDBBookColorCorrectionRepository
import snd.komelia.db.color.IDBColorCurvesPresetRepository
import snd.komelia.db.color.IDBColorLevelsPresetRepository
import snd.komelia.db.homescreen.LocalStorageHomeScreenFilterRepository
import snd.komelia.db.repository.EpubReaderSettingsRepositoryWrapper
import snd.komelia.db.repository.HomeScreenFilterRepositoryWrapper
import snd.komelia.db.repository.KomfSettingsRepositoryWrapper
import snd.komelia.db.repository.ReaderSettingsRepositoryWrapper
import snd.komelia.db.repository.SettingsRepositoryWrapper
import snd.komelia.db.settings.LocalStorageSettingsRepository
import snd.komelia.db.settings.NoopFontsRepository
import snd.komelia.image.KomeliaImageDecoder
import snd.komelia.image.KomeliaPanelDetector
import snd.komelia.image.KomeliaUpscaler
import snd.komelia.image.ReaderImageFactory
import snd.komelia.image.WasmReaderImageFactory
import snd.komelia.image.processing.ImageProcessingPipeline
import snd.komelia.image.wasm.client.WorkerImageDecoder
import snd.komelia.offline.OfflineModule
import snd.komelia.offline.OfflineRepositories
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komelia.settings.CookieStoreSecretsRepository
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.ui.DependencyContainer
import snd.komelia.ui.home.edit.getDefaultFilters
import snd.komelia.updates.AppUpdater
import snd.komelia.updates.OnnxModelDownloader
import snd.komelia.updates.OnnxRuntimeInstaller
import snd.komelia.updates.UpdateClient
import snd.komga.client.KomgaClientFactory
import snd.komga.client.user.KomgaUser

class WasmAppModule(
    val idb: Database
) : AppModule() {
    val settingsRepository = LocalStorageSettingsRepository()
    val homeFiltersRepository = LocalStorageHomeScreenFilterRepository()


    override suspend fun afterInit(dependencies: DependencyContainer) {
        val baseUrl = dependencies.appRepositories.settingsRepository.getServerUrl().stateIn(initScope)
        overrideFetch { baseUrl.value }
    }

    override suspend fun createAppRepositories(): AppRepositories {
        return AppRepositories(
            settingsRepository = SettingsRepositoryWrapper(
                SettingsStateWrapper(
                    settingsRepository.getSettings(),
                    settingsRepository::saveAppSettings
                )
            ),
            epubReaderSettingsRepository = EpubReaderSettingsRepositoryWrapper(
                SettingsStateWrapper(
                    settingsRepository.getEpubReaderSettings(),
                    settingsRepository::saveEpubReaderSettings
                )
            ),
            imageReaderSettingsRepository = ReaderSettingsRepositoryWrapper(
                SettingsStateWrapper(
                    settingsRepository.getImageReaderSettings(),
                    settingsRepository::saveImageReaderSettings
                )
            ),
            fontsRepository = NoopFontsRepository(),
            colorCurvesPresetsRepository = IDBColorCurvesPresetRepository(idb),
            colorLevelsPresetRepository = IDBColorLevelsPresetRepository(idb),
            bookColorCorrectionRepository = IDBBookColorCorrectionRepository(idb),
            secretsRepository = CookieStoreSecretsRepository(),
            komfSettingsRepository = KomfSettingsRepositoryWrapper(
                SettingsStateWrapper(
                    settingsRepository.getKomfSettings(),
                    settingsRepository::saveKomfSettings
                )
            ),
            homeScreenFilterRepository = HomeScreenFilterRepositoryWrapper(
                SettingsStateWrapper(
                    settings = homeFiltersRepository.getFilters() ?: getDefaultFilters(),
                    saveSettings = homeFiltersRepository::putFilters
                )
            )
        )
    }

    override suspend fun createOfflineRepositories(): OfflineRepositories? {
        return null
    }

    override fun createKtorClient(): HttpClient {
        return HttpClient(Js) {
            expectSuccess = true
            followRedirects = false
        }
    }

    override fun createKtorClientWithoutCache(): HttpClient {
        return HttpClient(Js) {
            expectSuccess = true
            followRedirects = false
        }
    }

    override fun createAppUpdater(updateClient: UpdateClient): AppUpdater? {
        return null
    }

    override suspend fun createImageDecoder(): KomeliaImageDecoder {
        val workerDecoder = WorkerImageDecoder()
        workerDecoder.init()
        return workerDecoder
    }

    override suspend fun createReaderImageFactory(
        imageDecoder: KomeliaImageDecoder,
        pipeline: ImageProcessingPipeline,
        settings: ImageReaderSettingsRepository,
        onnxRuntimeUpscaler: KomeliaUpscaler?
    ): ReaderImageFactory {
        return WasmReaderImageFactory(
            imageDecoder = imageDecoder,
            downSamplingKernel = settings.getDownsamplingKernel().stateIn(initScope),
            upsamplingMode = settings.getUpsamplingMode().stateIn(initScope),
            linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(initScope),
            processingPipeline = pipeline,
            stretchImages = settings.getStretchToFit().stateIn(initScope),
        )
    }

    override fun createWindowState(): AppWindowState {
        return BrowserWindowState()
    }

    override fun createCoilContext(): PlatformContext {
        return PlatformContext.INSTANCE
    }

    override fun createOnnxRuntimeInstaller(updateClient: UpdateClient): OnnxRuntimeInstaller? {
        return null
    }

    override fun createOnnxModelDownloader(updateClient: UpdateClient): OnnxModelDownloader? {
        return null
    }

    override fun createOnnxRuntime(): OnnxRuntime? {
        return null
    }

    override suspend fun createUpscaler(
        onnxRuntime: OnnxRuntime,
        modelDownloader: OnnxModelDownloader,
        settings: ImageReaderSettingsRepository
    ): KomeliaUpscaler? {
        return null
    }

    override suspend fun createPanelDetector(
        onnxRuntime: OnnxRuntime,
        modelDownloader: OnnxModelDownloader,
        settings: ImageReaderSettingsRepository
    ): KomeliaPanelDetector? {
        return null
    }

    override fun getCoilCacheDirectory(): Path? {
        return null
    }

    override fun createCoilMemoryCache(): MemoryCache {
        return MemoryCache.Builder()
            .maxSizeBytes(64 * 1024 * 1024) // 64 Mib
            .build()
    }

    override fun getReaderCacheDirectory(): Path? {
        return null
    }

    override fun createOfflineModule(
        repositories: OfflineRepositories,
        onlineUser: StateFlow<KomgaUser?>,
        onlineServerUrl: StateFlow<String>,
        isOffline: StateFlow<Boolean>,
        komgaClientFactory: KomgaClientFactory
    ): OfflineModule? {
        return null
    }
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
