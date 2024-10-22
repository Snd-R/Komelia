package io.github.snd_r.komelia.ui.reader.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.R2Progression
import snd.komga.client.book.WPPublication
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.webview.ResourceLoadResult
import snd.webview.Webview
import snd.webview.createWebview

private val resourceBaseUriRegex = "^komelia://.*/resource/".toRegex()
private val httpSchemeRegex = "https?://".toRegex()

class EpubViewModel(
    bookId: KomgaBookId,
    book: KomgaBook?,
    private val navigator: Navigator,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val settingsRepository: CommonSettingsRepository,
    private val notifications: AppNotifications,
    private val ktor: HttpClient,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val bookId = MutableStateFlow(bookId)
    val book = MutableStateFlow(book)
    val webview = MutableStateFlow<Webview?>(null)
    private val serverBaseUrl = settingsRepository.getServerUrl()

    suspend fun initialize() {
        if (state.value !is Uninitialized) return

        mutableState.value = LoadState.Loading
        notifications.runCatchingToNotifications {
            book.value = bookClient.getBook(bookId.value)
            val webview = createWebview()
            this.webview.value = webview
            loadEpub(webview)
            mutableState.value = LoadState.Success(Unit)

        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }


    private fun loadEpub(webview: Webview) {
        webview.bind<Unit, String>("bookId") {
            bookId.value.value
        }
        webview.bind("bookGet") { bookId: KomgaBookId ->
            bookClient.getBook(bookId)
        }
        webview.bind("bookGetProgression") { bookId: KomgaBookId ->
            bookClient.getReadiumProgression(bookId)?.let {
                progressionToWebview(it)
            }
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
            val manifest = bookClient.getWebPubManifest(bookId)
            replaceManifestLinks(manifest)
        }

        webview.bind<Unit, Unit>("closeBook") {
            webview.close()
            navigator replace MainScreen(book.value?.let { bookScreen(it) } ?: BookScreen(bookId.value))
        }

        webview.bind<Unit, JsonObject>("getSettings") {
            settingsRepository.getKomgaWebuiEpubReaderSettings().first()
        }

        webview.bind<JsonObject, Unit>("saveSettings") { newSettings ->
            println("save settings $newSettings")
            settingsRepository.putKomgaWebuiEpubReaderSettings(newSettings)
        }

        webview.registerResourceLoadHandler("komelia") { path ->
            runCatching {
                runBlocking {
                    val bytes = ktor.get(path).bodyAsBytes()
                    ResourceLoadResult(data = bytes, contentType = null)
                }
            }
                .onFailure { it.printStackTrace() }
                .getOrNull()
        }

        webview.loadUri("file:////home/den/tmp/komga-webui/index.html")
    }

    private suspend fun progressionToWebview(progress: R2Progression): R2Progression {
        val baseUrl = serverBaseUrl.first()
        val targetHref = buildString {
            append(baseUrl.replace(httpSchemeRegex, "komelia://"))
            append("/api/v1/books/0HM0KE8NNT7TF/resource/")
            append(progress.locator.href)
        }
        return progress.copy(locator = progress.locator.copy(href = targetHref))
    }

    private fun progressionFromWebview(progress: R2Progression): R2Progression {
        return progress.copy(
            locator = progress.locator.copy(
                href = progress.locator.href.replace(resourceBaseUriRegex, "")
            )
        )
    }

    private fun replaceManifestLinks(manifest: WPPublication): WPPublication {
        return manifest.copy(
            readingOrder = manifest.readingOrder.map { order -> order.copy(href = order.href?.replaceScheme()) },
            resources = manifest.resources.map { resource -> resource.copy(href = resource.href?.replaceScheme()) },
            toc = manifest.toc.map { toc -> toc.copy(href = toc.href?.replaceScheme()) },
            landmarks = manifest.landmarks.map { landmark -> landmark.copy(href = landmark.href?.replaceScheme()) },
            pageList = manifest.pageList.map { page -> page.copy(href = page.href?.replaceScheme()) },
            links = manifest.links.map { link -> link.copy(href = link.href?.replaceScheme()) },
        )
    }

    private fun String.replaceScheme() = this.replace(httpSchemeRegex, "komelia://")

    private suspend fun proxyRequest(url: String): String? {
        val urlPath = parseUrl(url)?.fullPath ?: return null

        return ktor.get(urlPath) {
            accept(ContentType.Any)
        }.bodyAsText()
    }
}