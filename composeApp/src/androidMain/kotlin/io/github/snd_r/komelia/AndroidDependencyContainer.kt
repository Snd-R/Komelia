package io.github.snd_r.komelia

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor.KtorNetworkFetcherFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.http.RememberMePersistingCookieStore
import io.github.snd_r.komelia.image.coil.FileMapper
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.AndroidReaderSettingsRepository
import io.github.snd_r.komelia.settings.AndroidSecretsRepository
import io.github.snd_r.komelia.settings.AndroidSettingsRepository
import io.github.snd_r.komelia.settings.AppSettingsSerializer
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.updates.AndroidAppUpdater
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.UpdateClient
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class AndroidDependencyContainer(
    override val settingsRepository: SettingsRepository,
    override val readerSettingsRepository: ReaderSettingsRepository,
    override val secretsRepository: SecretsRepository,
    override val appUpdater: AppUpdater,
    override val availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    override val komgaClientFactory: KomgaClientFactory,
    override val imageLoader: ImageLoader,
    override val imageLoaderContext: PlatformContext
) : DependencyContainer {
    override val appNotifications: AppNotifications = AppNotifications()

    companion object {
        suspend fun createInstance(scope: CoroutineScope, context: Context): AndroidDependencyContainer {
            val datastore = DataStoreFactory.create(
                serializer = AppSettingsSerializer,
                produceFile = { context.dataStoreFile("settings.pb") },
                corruptionHandler = null,
                migrations = listOf(),
            )

            val settingsRepository = AndroidSettingsRepository(datastore)
            val readerSettingsRepository = AndroidReaderSettingsRepository(datastore)
            val secretsRepository = AndroidSecretsRepository(datastore)

            val baseUrl = settingsRepository.getServerUrl().stateIn(scope)

            val okHttpClient = createOkHttpClient()
            val cookiesStorage =
                RememberMePersistingCookieStore(baseUrl, secretsRepository).also { it.loadRememberMeCookie() }
            val ktorClient = createKtorClient(okHttpClient)
            val komgaClientFactory = createKomgaClientFactory(
                baseUrl = baseUrl,
                ktorClient = ktorClient,
                cookiesStorage = cookiesStorage,
                context = context
            )
            val appUpdater = createAppUpdater(ktorClient, context)

            val coil = createCoil(ktorClient, baseUrl, cookiesStorage, context)
            SingletonImageLoader.setSafe { coil }

            return AndroidDependencyContainer(
                settingsRepository = settingsRepository,
                readerSettingsRepository = readerSettingsRepository,
                secretsRepository = secretsRepository,
                appUpdater = appUpdater,
                availableDecoders = emptyFlow(),
                komgaClientFactory = komgaClientFactory,
                imageLoader = coil,
                imageLoaderContext = context
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
            }
        }

        private fun createKomgaClientFactory(
            baseUrl: StateFlow<String>,
            ktorClient: HttpClient,
            cookiesStorage: RememberMePersistingCookieStore,
            context: Context
        ): KomgaClientFactory {
            val ktorKomgaClient = ktorClient.config {
                install(HttpCache) {
                    privateStorage(FileStorage(context.cacheDir))
                    publicStorage(FileStorage(context.cacheDir))
                }
            }

            return KomgaClientFactory.Builder()
                .ktor(ktorKomgaClient)
                .baseUrl { baseUrl.value }
                .cookieStorage(cookiesStorage)
                .build()
        }

        private fun createCoil(
            ktorClient: HttpClient,
            url: StateFlow<String>,
            cookiesStorage: RememberMePersistingCookieStore,
            context: PlatformContext
        ): ImageLoader {
            val coilKtorClient = ktorClient.config {
                defaultRequest { url(url.value) }
                install(HttpCookies) { storage = cookiesStorage }
            }

            return ImageLoader.Builder(context)
                .components {
                    add(KomgaBookPageMapper(url))
                    add(KomgaSeriesMapper(url))
                    add(KomgaBookMapper(url))
                    add(KomgaCollectionMapper(url))
                    add(KomgaReadListMapper(url))
                    add(KomgaSeriesThumbnailMapper(url))
                    add(FileMapper())
                    add(KtorNetworkFetcherFactory(httpClient = coilKtorClient))
                }
                .build()
        }

        private fun createAppUpdater(ktor: HttpClient, context: Context): AndroidAppUpdater {
            val githubClient = UpdateClient(
                ktor.config {
                    install(HttpCache)
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }
            )
            return AndroidAppUpdater(githubClient, context)
        }
    }
}