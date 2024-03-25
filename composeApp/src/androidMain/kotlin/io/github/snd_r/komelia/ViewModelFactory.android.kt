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
import io.github.snd_r.komelia.image.coil.KomgaBookMapper
import io.github.snd_r.komelia.image.coil.KomgaBookPageMapper
import io.github.snd_r.komelia.image.coil.KomgaCollectionMapper
import io.github.snd_r.komelia.image.coil.KomgaReadListMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesMapper
import io.github.snd_r.komelia.image.coil.KomgaSeriesThumbnailMapper
import io.github.snd_r.komelia.image.coil.PathMapper
import io.github.snd_r.komelia.settings.AndroidSecretsRepository
import io.github.snd_r.komelia.settings.AndroidSettingsRepository
import io.github.snd_r.komelia.settings.AppSettingsSerializer
import io.github.snd_r.komga.KomgaClientFactory
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.cookies.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

actual suspend fun createViewModelFactory(
    scope: CoroutineScope,
    context: Context,
): ViewModelFactory {

    val datastore = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { context.dataStoreFile("settings.pb") },
        corruptionHandler = null,
        migrations = listOf(),
        scope = scope
    )

    val settingsRepository = AndroidSettingsRepository(datastore)
    val secretsRepository = AndroidSecretsRepository(datastore)

    val baseUrl = settingsRepository.getServerUrl().stateIn(scope)

    val okHttpClient = createOkHttpClient()
    val cookiesStorage = RememberMePersistingCookieStore(baseUrl, secretsRepository).also { it.loadRememberMeCookie() }
    val ktorClient = createKtorClient(baseUrl, okHttpClient, cookiesStorage)
    val komgaClientFactory = createKomgaClientFactory(
        baseUrl = baseUrl,
        ktorClient = ktorClient,
        okHttpClient = okHttpClient,
        cookiesStorage = cookiesStorage,
        context = context
    )

    val coil = createCoil(baseUrl, ktorClient, context)
    SingletonImageLoader.setSafe { coil }

    return ViewModelFactory(
        komgaClientFactory = komgaClientFactory,
        settingsRepository = settingsRepository,
        secretsRepository = secretsRepository,
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
    baseUrl: StateFlow<String>,
    okHttpClient: OkHttpClient,
    cookiesStorage: RememberMePersistingCookieStore,
): HttpClient {
    return HttpClient(OkHttp) {
        engine { preconfigured = okHttpClient }
        defaultRequest { url(baseUrl.value) }
        install(HttpCookies) { storage = cookiesStorage }

//        install(Logging) {
//            logger = Logger.DEFAULT
//            level = LogLevel.INFO
//        }
        expectSuccess = true
    }
}

private fun createKomgaClientFactory(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
    okHttpClient: OkHttpClient,
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
        .okHttp(okHttpClient)
        .baseUrl { baseUrl.value }
        .cookieStorage(cookiesStorage)
        .build()
}

private fun createCoil(url: StateFlow<String>, ktorClient: HttpClient, context: PlatformContext): ImageLoader {

    return ImageLoader.Builder(context)
        .components {
            add(KomgaBookPageMapper(url))
            add(KomgaSeriesMapper(url))
            add(KomgaBookMapper(url))
            add(KomgaCollectionMapper(url))
            add(KomgaReadListMapper(url))
            add(KomgaSeriesThumbnailMapper(url))
            add(PathMapper())
            add(KtorNetworkFetcherFactory(httpClient = ktorClient))
        }
        .build()
}
