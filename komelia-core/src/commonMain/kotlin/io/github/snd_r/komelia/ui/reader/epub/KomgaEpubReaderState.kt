package io.github.snd_r.komelia.ui.reader.epub

import cafe.adriel.voyager.navigator.Navigator
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.AppWindowState
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia_core.generated.resources.Res
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.R2Progression
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.webview.KomeliaWebview
import snd.webview.ResourceLoadResult
import snd.webview.runRequest

private val logger = KotlinLogging.logger {}
private val resourceBaseUriRegex = "^http(s)?://.*/resource/".toRegex()

class KomgaEpubReaderState(
    bookId: KomgaBookId,
    book: KomgaBook?,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val settingsRepository: CommonSettingsRepository,
    private val epubSettingsRepository: EpubReaderSettingsRepository,
    private val notifications: AppNotifications,
    private val ktor: HttpClient,
    private val markReadProgress: Boolean,
    private val windowState: AppWindowState,
    private val platformType: PlatformType,
    private val coroutineScope: CoroutineScope,
) : EpubReaderState {
    override val state = MutableStateFlow<LoadState<Unit>>(Uninitialized)
    override val book = MutableStateFlow(book)

    val bookId = MutableStateFlow(bookId)
    private val webview = MutableStateFlow<KomeliaWebview?>(null)
    private val navigator = MutableStateFlow<Navigator?>(null)

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun initialize(navigator: Navigator) {
        this.navigator.value = navigator
        if (platformType == PlatformType.MOBILE) windowState.setFullscreen(true)
        if (state.value !is Uninitialized) return

        state.value = LoadState.Loading
        notifications.runCatchingToNotifications {
            Res.getUri("files/komga.html")
            if (book.value == null) book.value = bookClient.getBook(bookId.value)
            state.value = LoadState.Success(Unit)
        }.onFailure {
            state.value = LoadState.Error(it)
        }
    }

    override fun onWebviewCreated(webview: KomeliaWebview) {
        this.webview.value = webview
        coroutineScope.launch { loadEpub(webview) }
    }

    override fun closeWebview() {
        webview.value?.close()
        if (platformType == PlatformType.MOBILE) windowState.setFullscreen(false)
        navigator.value?.replaceAll(
            MainScreen(book.value?.let { bookScreen(it) } ?: BookScreen(bookId.value))
        )
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadEpub(webview: KomeliaWebview) {
        webview.bind<Unit, String>("bookId") {
            bookId.value.value
        }
        webview.bind<Unit, Boolean>("incognito") {
            !markReadProgress
        }
        webview.bind<KomgaBookId, KomgaBook>("bookGet") { bookId: KomgaBookId ->
            val book = bookClient.getBook(bookId)
            this.book.value = book
            this.bookId.value = book.id
            book
        }
        webview.bind("bookGetProgression") { bookId: KomgaBookId ->
            bookClient.getReadiumProgression(bookId)
                ?.let { progressionToWebview(it) }
        }

        @Serializable
        data class BookUpdateProgression(val bookId: KomgaBookId, val progression: R2Progression)
        webview.bind("bookUpdateProgression") { request: BookUpdateProgression ->
            bookClient.updateReadiumProgression(request.bookId, progressionFromWebview(request.progression))
        }

        webview.bind("bookGetBookSiblingNext") { bookId: KomgaBookId ->
            bookClient.getBookSiblingNext(bookId)
        }

        webview.bind("bookGetBookSiblingPrevious") { bookId: KomgaBookId ->
            bookClient.getBookSiblingPrevious(bookId)
        }

        webview.bind("getOneSeries") { seriesId: KomgaSeriesId ->
            seriesClient.getOneSeries(seriesId)
        }

        webview.bind("readListGetOne") { readListId: KomgaReadListId ->
            readListClient.getOne(readListId)
        }

        webview.bind("d2ReaderGetContent") { href: String ->
            proxyRequest(href)
        }
        webview.bind("d2ReaderGetContentBytesLength") { href: String ->
            proxyRequest(href)?.length
        }

        webview.bind("externalFetch") { href: String ->
            proxyRequest(href)
        }

        webview.bind("getPublication") { bookId: KomgaBookId ->
            bookClient.getWebPubManifest(bookId)
        }

        webview.bind<Unit, Unit>("closeBook") { closeWebview() }

        webview.bind<Unit, String>("getServerUrl") {
            settingsRepository.getServerUrl().first()
        }

        webview.bind<Unit, JsonObject>("getSettings") {
            epubSettingsRepository.getKomgaReaderSettings()
        }

        webview.bind<JsonObject, Unit>("saveSettings") { newSettings ->
            epubSettingsRepository.putKomgaReaderSettings(newSettings)
        }
        webview.bind<Unit, Boolean>("isFullscreenAvailable") {
            platformType != PlatformType.MOBILE
        }
        webview.bind<Unit, Unit>("toggleFullscreen") {
            val fullscreen = windowState.isFullscreen.first()
            windowState.setFullscreen(!fullscreen)
        }

        webview.registerRequestInterceptor { request ->
            runCatching {
                when (request.url.toString()) {
                    "http://komelia/komga.html" -> {
                        val bytes = Res.readBytes("files/komga.html")
                        ResourceLoadResult(data = bytes, contentType = "text/html")
                    }

                    "http://komelia/favicon.ico" -> null

                    else -> ktor.runRequest(request)
                }
            }.onFailure { logger.catching(it) }.getOrNull()
        }

        webview.navigate("http://komelia/komga.html")
        webview.start()
    }

    private suspend fun progressionToWebview(progress: R2Progression): R2Progression {
        val baseUrl = settingsRepository.getServerUrl().first()
        return progress.copy(
            locator = progress.locator.copy(
                href = "$baseUrl/api/v1/books/${bookId.value}/resource/${progress.locator.href}"
            )
        )
    }

    private fun progressionFromWebview(progress: R2Progression): R2Progression {
        return progress.copy(
            locator = progress.locator.copy(
                href = progress.locator.href.replace(resourceBaseUriRegex, "")
            )
        )
    }

    private suspend fun proxyRequest(url: String): String? {
        val urlPath = parseUrl(url)?.fullPath ?: return null
        return ktor.get(urlPath) { accept(ContentType.Any) }.bodyAsText()
    }
}