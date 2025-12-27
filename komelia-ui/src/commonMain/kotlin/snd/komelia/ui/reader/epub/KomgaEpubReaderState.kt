package snd.komelia.ui.reader.epub

import cafe.adriel.voyager.navigator.Navigator
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parser.Parser.Companion.xmlParser
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import snd.komelia.AppNotifications
import snd.komelia.AppWindowState
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.EpubReaderSettingsRepository
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.MainScreen
import snd.komelia.ui.book.BookScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.PlatformType.WEB_KOMF
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.R2Progression
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId
import snd.webview.KomeliaWebview
import snd.webview.ResourceLoadResult

private val logger = KotlinLogging.logger {}
private val resourceBaseUriRegex = "^http(s)?://.*/resource/".toRegex()

class KomgaEpubReaderState(
    bookId: KomgaBookId,
    book: KomeliaBook?,
    private val bookApi: KomgaBookApi,
    private val seriesApi: KomgaSeriesApi,
    private val readListApi: KomgaReadListApi,
    private val settingsRepository: CommonSettingsRepository,
    private val epubSettingsRepository: EpubReaderSettingsRepository,
    private val notifications: AppNotifications,
    private val markReadProgress: Boolean,
    private val windowState: AppWindowState,
    private val platformType: PlatformType,
    private val coroutineScope: CoroutineScope,
    private val bookSiblingsContext: BookSiblingsContext,
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
            if (book.value == null) book.value = bookApi.getOne(bookId.value)
            state.value = LoadState.Success(Unit)
        }.onFailure {
            state.value = LoadState.Error(it)
        }
    }

    override fun onWebviewCreated(webview: KomeliaWebview) {
        this.webview.value = webview
        coroutineScope.launch { loadEpub(webview) }
    }

    override fun onBackButtonPress() {
        closeWebview()
    }

    override fun closeWebview() {
        webview.value?.close()
        if (platformType == PlatformType.MOBILE) windowState.setFullscreen(false)
        navigator.value?.let { nav ->
            if (nav.canPop) nav.pop()
            else {
                val screen = book.value?.let { bookScreen(book = it, bookSiblingsContext = bookSiblingsContext) }
                    ?: BookScreen(bookId = bookId.value, bookSiblingsContext = bookSiblingsContext)
                nav.replaceAll(MainScreen(screen))
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadEpub(webview: KomeliaWebview) {
        val serverUrl = settingsRepository.getServerUrl().stateIn(coroutineScope)
        webview.bind<Unit, String>("bookId") {
            bookId.value.value
        }
        webview.bind<Unit, Boolean>("incognito") {
            !markReadProgress
        }
        webview.bind<KomgaBookId, KomeliaBook>("bookGet") { bookId: KomgaBookId ->
            val book = bookApi.getOne(bookId)
            this.book.value = book
            this.bookId.value = book.id
            book
        }
        webview.bind("bookGetProgression") { bookId: KomgaBookId ->
            bookApi.getReadiumProgression(bookId)
                ?.let { progressionToWebview(it) }
        }

        @Serializable
        data class BookUpdateProgression(val bookId: KomgaBookId, val progression: R2Progression)
        webview.bind("bookUpdateProgression") { request: BookUpdateProgression ->
            bookApi.updateReadiumProgression(request.bookId, progressionFromWebview(request.progression))
        }

        webview.bind("bookGetBookSiblingNext") { bookId: KomgaBookId ->
            bookApi.getBookSiblingNext(bookId)
        }

        webview.bind("bookGetBookSiblingPrevious") { bookId: KomgaBookId ->
            bookApi.getBookSiblingPrevious(bookId)
        }

        webview.bind("getOneSeries") { seriesId: KomgaSeriesId ->
            seriesApi.getOneSeries(seriesId)
        }

        webview.bind("readListGetOne") { readListId: KomgaReadListId ->
            readListApi.getOne(readListId)
        }

        webview.bind("d2ReaderGetContent") { href: String ->
            getD2Content(href)
        }
        webview.bind("d2ReaderGetContentBytesLength") { href: String ->
            proxyResourceRequest(bookApi, href, serverUrl).data.size
        }

        webview.bind("externalFetch") { href: String ->
            proxyResourceRequest(bookApi, href, serverUrl).data.decodeToString()
        }

        webview.bind("getPublication") { bookId: KomgaBookId ->
            bookApi.getWebPubManifest(bookId)
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
                when (val urlString = request.url.toString()) {
                    "http://komelia/komga.html" -> {
                        val bytes = Res.readBytes("files/komga.html")
                        ResourceLoadResult(data = bytes, contentType = "text/html")
                    }

                    "http://komelia/favicon.ico" -> null
                    else -> proxyResourceRequest(bookApi, urlString, serverUrl)
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

    private suspend fun getD2Content(url: String): String? {
        return runCatching {
            val textResponse = proxyResourceRequest(
                bookApi = bookApi,
                urlString = url,
                serverUrl = settingsRepository.getServerUrl()
            ).data.decodeToString()
            if (platformType == WEB_KOMF) {
                val document = Ksoup.parse(textResponse, xmlParser()) //strict xhtml rules
                addCrossOriginToElements(document)
                document.outerHtml()
            } else textResponse
        }
            .onFailure { logger.catching(it) }
            .getOrNull()
    }

    private fun addCrossOriginToElements(body: Element) {
        buildList {
            addAll(body.getElementsByTag("link"))
            addAll(body.getElementsByTag("img"))
            addAll(body.getElementsByTag("image"))
        }.forEach { it.attr("crossorigin", "use-credentials") }
    }
}