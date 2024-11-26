package io.github.snd_r.komelia.ui.reader.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.getSystemFontNames
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia_core.generated.resources.Res
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.R2Device
import snd.komga.client.book.R2Location
import snd.komga.client.book.R2Locator
import snd.komga.client.book.R2LocatorText
import snd.komga.client.book.R2Progression
import snd.komga.client.book.WPPublication
import snd.webview.ResourceLoadResult
import snd.webview.Webview
import java.net.URI
import kotlin.math.roundToLong

private val logger = KotlinLogging.logger {}
private val resourceBaseUriRegex = "^http(s)?://.*/resource/".toRegex()

class TtsuEpubViewModel(
    bookId: KomgaBookId,
    book: KomgaBook?,
    private val navigator: Navigator,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
    private val ktor: HttpClient,
    private val markReadProgress: Boolean,
    private val settingsRepository: EpubReaderSettingsRepository
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    val bookId = MutableStateFlow(bookId)
    val book = MutableStateFlow(book)
    private val webview = MutableStateFlow<Webview?>(null)
    private val epubLoadTask = CompletableDeferred<TtuEpubData>()
    private val availableSystemFonts = MutableStateFlow<List<String>>(emptyList())

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return

        mutableState.value = LoadState.Loading
        notifications.runCatchingToNotifications {
            book.value = bookClient.getBook(bookId.value)
            screenModelScope.launch { epubLoadTask.complete(generateEpubHtml(bookId.value)) }
            availableSystemFonts.value = getSystemFontNames()
            mutableState.value = LoadState.Success(Unit)
        }.onFailure {
            mutableState.value = LoadState.Error(it)
        }
    }

    fun onWebviewCreated(webview: Webview) {
        this.webview.value = webview
        screenModelScope.launch {
            loadEpub(webview)
        }
    }

    fun closeWebview() {
        webview.value?.close()
        navigator replace MainScreen(book.value?.let { bookScreen(it) } ?: BookScreen(bookId.value))
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun loadEpub(webview: Webview) {
        webview.bind<Unit, String>("getCurrentBookId") { bookId.value.value }
        webview.bind<Unit, TtuBookData>("getBookData") {
            val data = epubLoadTask.await()
            TtuBookData(
                id = bookId.value.value,
                title = requireNotNull(book.value?.seriesTitle),
                styleSheet = data.stylesheet,
                elementHtml = data.element.html(),
                imageUrls = data.images.map { it.toString() },
                coverImage = null,
                hasThumb = false,
                characters = data.characterCount,
                sections = data.sections,
                lastBookModified = Clock.System.now().toEpochMilliseconds(),
                lastBookOpen = Clock.System.now().toEpochMilliseconds()
            )
        }

        webview.bind<Unit, TtsuReaderSettings>("getSettings") {
            settingsRepository.getTtsuReaderSettings()
        }
        webview.bind<TtsuReaderSettings, Unit>("putSettings") {
            settingsRepository.putTtsuReaderSettings(it)
        }
        webview.bind<Unit, TtuBookmarkData>("getBookmark") { getBookmark() }
        webview.bind<TtuBookmarkData, Unit>("putBookmark") {
            putBookmark(it)
        }

        webview.bind<Unit, Unit>("closeBook") { closeWebview() }
        webview.bind<Unit, List<String>>("getAvailableFonts") {
            availableSystemFonts.value
        }

        webview.registerRequestInterceptor { uri ->
            runCatching {
                when {
                    uri == "https://komelia/index.html" -> {
                        runBlocking {
                            val bytes = Res.readBytes("files/index.html")
                            ResourceLoadResult(data = bytes, contentType = "text/html")
                        }
                    }

                    uri.startsWith("https://komelia") -> error("invalid request uri $uri")

                    else -> runBlocking {
                        val bytes = ktor.get(uri).bodyAsBytes()
                        ResourceLoadResult(data = bytes, contentType = null)
                    }

                }
            }.onFailure { logger.catching(it) }.getOrNull()
        }

        webview.navigate("https://komelia/index.html")
        webview.start()
    }

    private suspend fun getBookmark(): TtuBookmarkData {
        val data = epubLoadTask.await()
        val progress = bookClient.getReadiumProgression(bookId.value)
            ?: return TtuBookmarkData(
                bookId = bookId.value.value,
                scrollX = null,
                scrollY = null,
                exploredCharCount = 0,
                progress = 0.0,
                lastBookmarkModified = Clock.System.now().toEpochMilliseconds(),
                chapterIndex = 0,
                chapterReference = requireNotNull(data.manifest.readingOrder.first().href),
            )

        val chapterIndex = data.manifest.readingOrder
            .indexOfFirst { it.href?.replace(resourceBaseUriRegex, "") == progress.locator.href }
        val section = data.sections[chapterIndex]
        val totalProgress = progress.locator.locations.totalProgression?.toDouble() ?: .0
        val bookCharCount = data.characterCount

        val exploredCharCount = if (progress.locator.text != null) {
            val sectionExploredChars: Long = data.element.getElementById(section.reference)?.let { sectionElement ->
                (progress.locator.text?.before ?: progress.locator.text?.before)?.let {
                    charCountForElementBeforeText(sectionElement, it)
                }
            } ?: 0L
            section.startCharacter + sectionExploredChars
        } else (bookCharCount * totalProgress).roundToLong()


        return TtuBookmarkData(
            bookId = bookId.value.value,
            scrollX = null,
            scrollY = null,
            exploredCharCount = exploredCharCount,
            progress = totalProgress,
            lastBookmarkModified = Clock.System.now().toEpochMilliseconds(),
            chapterIndex = chapterIndex,
            chapterReference = requireNotNull(data.manifest.readingOrder.first().href),
        )
    }

    private suspend fun putBookmark(bookmark: TtuBookmarkData) {
        val epubData = epubLoadTask.await()

        val manifestIndex = epubData.manifest.readingOrder.indexOfFirst { it.href == bookmark.chapterReference }
        if (manifestIndex == -1) return
        val manifestLink = epubData.manifest.readingOrder[manifestIndex]
        val type = manifestLink.type ?: return
        val epubSection = epubData.sections.find { it.reference == bookmark.chapterReference } ?: return

        val beforeText = bookmark.exploredCharCount
            ?.let { getBeforeTextForCharCount(epubData, epubSection, it) }

        val chapterProgressPercentage = bookmark.exploredCharCount
            ?.let {
                if (epubSection.characters == 0L) 0f
                else ((it - epubSection.startCharacter).toDouble() / epubSection.characters).toFloat()
            } ?: 0f

        val newProgression = R2Progression(
            modified = Instant.fromEpochMilliseconds(bookmark.lastBookmarkModified),
            device = R2Device("unused", "Komelia"),
            locator = R2Locator(
                href = bookmark.chapterReference.replace(resourceBaseUriRegex, ""),
                type = type,
                title = manifestLink.title,
                locations = R2Location(
                    fragment = emptyList(),
                    position = manifestIndex + 1,
                    progression = chapterProgressPercentage,
                    totalProgression = bookmark.exploredCharCount
                        ?.let { it.toDouble() / epubData.characterCount }?.toFloat()
                ),
                text = beforeText?.let { R2LocatorText(before = it) },
            )
        )
        bookClient.updateReadiumProgression(bookId.value, newProgression)
    }

    private fun getBeforeTextForCharCount(
        epubData: TtuEpubData,
        epubSection: TtuSection,
        exploredCharCount: Long
    ): String? {
        val elem = epubData.element.getElementById(epubSection.reference) ?: return null
        val charCount = exploredCharCount - epubSection.startCharacter
        val textNode = getTextNodeForCharCount(elem, charCount) ?: return null

        return textNode.text()
    }

    private suspend fun generateEpubHtml(bookId: KomgaBookId): TtuEpubData {
        val manifest = bookClient.getWebPubManifest(bookId)
        val sectionData: MutableList<TtuSection> = mutableListOf()
        val result = Element("div")

        var currentCharCount = 0L
        var currentMainChapterIndex: Int? = null
        val tocEntries = manifest.toc.associate {
            URLBuilder(requireNotNull(it.href)).apply { fragment = "" }.buildString() to requireNotNull(it.title)
        }
        val images = mutableListOf<Url>()
        for (chapter in manifest.readingOrder) {
            val chapterHref = chapter.href ?: continue

            val chapterData = ktor.get(chapterHref).bodyAsText()
            val chapterDocument = Ksoup.parse(chapterData)

            val chapterBody = chapterDocument.body()
            if (chapterBody.children().isEmpty()) {
                error("Unable to find valid body content while parsing EPUB")
            }
            val htmlClass = chapterDocument.selectFirst("html")?.className() ?: ""
            val chapterBodyId = chapterBody.id().ifBlank { null }
            val chapterBodyClass = chapterBody.className()

            images.addAll(scanAndReplaceImagePaths(manifest, URI(chapterHref), chapterBody))

            val childBodyDiv = Element("div")
            childBodyDiv.addClass("ttu-book-body-wrapper $chapterBodyClass")
            chapterBodyId?.let { childBodyDiv.id(it) }
            childBodyDiv.html(chapterBody.html())

            val childHtmlDiv = Element("div")
            childHtmlDiv.addClass("ttu-book-html-wrapper $htmlClass")
            childHtmlDiv.appendChild(childBodyDiv)

            val childWrapperDiv = Element("div")
            childWrapperDiv.id(chapterHref)
            childWrapperDiv.appendChild(childHtmlDiv)

            result.appendChild(childWrapperDiv)

            val chapterCharCount = charCountForElement(childWrapperDiv);
            val tocTitle = tocEntries[chapterHref]
            if (tocTitle != null) {
                val startCharacter = currentMainChapterIndex
                    ?.let { sectionData[it].startCharacter + sectionData[it].characters }
                    ?: 0L

                sectionData.add(
                    TtuSection(
                        reference = chapterHref,
                        charactersWeight = chapterCharCount,
                        label = tocTitle,
                        startCharacter = startCharacter,
                        characters = chapterCharCount,
                        parentChapter = null
                    )
                )
                currentMainChapterIndex = sectionData.size - 1
            } else {
                if (currentMainChapterIndex != null) {
                    val mainChapter = sectionData[currentMainChapterIndex]
                    sectionData[currentMainChapterIndex] = mainChapter.copy(
                        characters = mainChapter.characters + chapterCharCount
                    )
                    sectionData.add(
                        TtuSection(
                            reference = chapterHref,
                            charactersWeight = chapterCharCount,
                            startCharacter = currentCharCount,
                            characters = currentCharCount + chapterCharCount,
                            parentChapter = mainChapter.reference,
                            label = null,
                        )
                    )
                } else {
                    sectionData.add(
                        TtuSection(
                            reference = chapterHref,
                            charactersWeight = chapterCharCount,
                            startCharacter = currentCharCount,
                            characters = chapterCharCount,
                            parentChapter = null,
                            label = "Preface",
                        )
                    )
                    currentMainChapterIndex = sectionData.size - 1
                }
            }

            currentCharCount += chapterCharCount
        }

        val combinedDirtyStyleString = manifest.resources.asFlow()
            .filter { it.type == "text/css" }
            .mapNotNull { res -> res.href?.let { ktor.get(it).bodyAsText() } }
            .fold("") { acc, css -> acc + css }

        return TtuEpubData(
            element = result,
            stylesheet = combinedDirtyStyleString,
            characterCount = currentCharCount,
            sections = sectionData,
            manifest = manifest,
            images = images
        )
    }

    private val whitespaceRegex = "\\R|\\s".toRegex()
    private fun String.removeWhitespacesAndLineBreaks() = this.replace(whitespaceRegex, "")

    private fun charCountForElementBeforeText(elem: Element, beforeText: String): Long {
        var acc = 0L
        for (node in getTextOrGaijiNodes(elem)) {
            if (node is TextNode) {
                val nodeText = node.text()
                val indexOfText = nodeText.indexOf(beforeText)
                if (indexOfText != -1) {
                    acc += if (indexOfText + beforeText.length < nodeText.length)
                        nodeText.substring(0, indexOfText + beforeText.length)
                            .removeWhitespacesAndLineBreaks()
                            .codePoints().count()
                    else nodeText.removeWhitespacesAndLineBreaks().codePoints().count()

                    return acc
                } else {
                    acc += nodeText.removeWhitespacesAndLineBreaks().codePoints().count()
                }

            } else if (isNodeGaiji(node)) {
                acc += 1
            }
        }

        return 0
    }

    private fun charCountForElement(elem: Element): Long {
        return getTextOrGaijiNodes(elem).fold(0L) { acc, node ->
            when {
                node is TextNode -> acc + node.text().removeWhitespacesAndLineBreaks().codePoints().count()
                isNodeGaiji(node) -> acc + 1
                else -> acc
            }
        }
    }

    private fun getTextNodeForCharCount(elem: Element, targetCharCount: Long): TextNode? {
        var charAcc = 0L
        getTextOrGaijiNodes(elem).forEach { node ->
            if (node is TextNode) {
                charAcc += node.text().removeWhitespacesAndLineBreaks().codePoints().count()
                if (charAcc >= targetCharCount) return node
            } else if (isNodeGaiji(node)) {
                charAcc += 1
            }
        }

        return null
    }

    private fun getTextOrGaijiNodes(node: Node): List<Node> {
        if (node.childNodeSize() == 0 || !isValidParagraph(node)) {
            return emptyList()
        }

        return node.childNodes().flatMap { childNode ->
            when {
                childNode is TextNode && !childNode.isBlank() -> listOf(childNode)
                isNodeGaiji(childNode) && isValidParagraph(childNode) -> listOf(childNode)
                else -> getTextOrGaijiNodes(childNode)
            }
        }
    }

    private fun isValidParagraph(node: Node) = when {
        node !is Element -> true
        node.normalName() == "rt" -> false
        node.hasAttr("aria-hidden") || node.hasAttr("hidden") -> false
        else -> true
    }

    private fun isNodeGaiji(node: Node): Boolean {
        return if (node !is Element || node.tag().normalName() != "img") false
        else isElementGaiji(node)
    }

    private fun isElementGaiji(el: Element): Boolean {
        return el.classNames().any { it.contains("gaiji") }
    }

    private fun scanAndReplaceImagePaths(
        manifest: WPPublication,
        baseUri: URI,
        body: Element,
    ): List<Url> {

        val processedImageUrls = mutableListOf<Url>()
        val images = body.getElementsByTag("img").toMutableList()
        images.addAll(body.getElementsByTag("image").toMutableList())

        for (resource in manifest.resources) {
            val resourceHref = resource.href ?: continue

            val type = resource.type ?: continue
            if (!type.startsWith("image")) continue

            val iter = images.iterator()
            while (iter.hasNext()) {
                val image = iter.next()
                val srcAttr = when (image.tag().normalName()) {
                    "img" -> "src"
                    "image" -> if (image.hasAttr("xlink:href")) "xlink:href" else "href"
                    else -> error("unexpected image tag ${image.tag().name}")
                }
                val imageUri = image.attr(srcAttr).ifBlank { null }?.let { URI(it) } ?: continue
                val imageAbsoluteUri = baseUri.resolve(imageUri)
                val resourceUri = URI(resourceHref)
                if (imageAbsoluteUri == resourceUri) {
                    image.attr(srcAttr, resourceUri.toString())
                    if (!image.classNames().contains("gaiji")) {
                        processedImageUrls.add(Url(resourceUri.toString()))
                    }
                }
            }
        }

        return processedImageUrls
    }

    data class TtuEpubData(
        val element: Element,
        val stylesheet: String,
        val characterCount: Long,
        val sections: List<TtuSection>,
        val manifest: WPPublication,
        val images: List<Url>
    )

    @Serializable
    data class TtuSection(
        val reference: String,
        val charactersWeight: Long,
        val startCharacter: Long,
        val characters: Long,
        val label: String?,
        val parentChapter: String? = null
    )

    @Serializable
    data class TtuBookData(
        val id: String,
        val title: String,
        val styleSheet: String,
        val elementHtml: String,
        val imageUrls: List<String>,
        val coverImage: String?,
        val hasThumb: Boolean,
        val characters: Long,
        val sections: List<TtuSection>?,
        val lastBookModified: Long,
        val lastBookOpen: Long
    )

    @Serializable
    data class TtuBookmarkData(
        val bookId: String,
        val progress: Double?,
        val exploredCharCount: Long? = null,
        val scrollX: Int? = null,
        val scrollY: Int? = null,
        val lastBookmarkModified: Long,
        val chapterIndex: Int,
        val chapterReference: String,
    )
}
