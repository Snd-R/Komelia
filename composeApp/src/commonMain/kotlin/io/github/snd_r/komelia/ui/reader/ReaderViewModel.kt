package io.github.snd_r.komelia.ui.reader

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class ReaderViewModel(
    imageLoader: ImageLoader,
    imageLoaderContext: PlatformContext,
    bookClient: KomgaBookClient,
    navigator: Navigator,
    appNotifications: AppNotifications,
    settingsRepository: SettingsRepository,
    markReadProgress: Boolean,
) : ScreenModel {
    private val readerImageLoader = ReaderImageLoader(imageLoader, imageLoaderContext)

    val readerState: ReaderState = ReaderState(
        bookClient = bookClient,
        navigator = navigator,
        appNotifications = appNotifications,
        settingsRepository = settingsRepository,
        markReadProgress = markReadProgress,
        coroutineScope = screenModelScope
    )

    val pagedReaderState = PagedReaderState(
        readerImageLoader = readerImageLoader,
        readerState = readerState,
        appNotifications = appNotifications,
        settingsRepository = settingsRepository
    )

    fun initialize(bookId: KomgaBookId) {
        screenModelScope.launch {
            readerState.initialize(bookId)
            pagedReaderState.initialize()
        }
    }

}
//    private val readerImageLoader = ReaderImageLoader(imageLoader, imageLoaderContext)
//    private val pageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//    private val resampleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
//
//    override val book = MutableStateFlow<KomgaBook?>(null)
//    override val bookPages = MutableStateFlow<List<PageMetadata>>(emptyList())
//    private var bookSiblingPrev by mutableStateOf<KomgaBook?>(null)
//    private var bookSiblingNext by mutableStateOf<KomgaBook?>(null)
//
//
//    private val currentSpreadScale = PageSpreadScaleState()
//
//    override val layout = MutableStateFlow(SINGLE_PAGE)
//    override val layoutOffset = MutableStateFlow(false)
//    override val scaleType = MutableStateFlow(SCREEN)
//    override val readingDirection = MutableStateFlow(LEFT_TO_RIGHT)
//    override val allowUpsample = MutableStateFlow(false)
//    override val decoder = MutableStateFlow<SamplerType?>(null)
//
//    private val pagedReaderState = PagedReaderState(
//        imageLoader = imageLoader,
//        imageLoaderContext = imageLoaderContext,
//        settings = this,
//        coroutineScope = screenModelScope,
//        onNextPage = { markReadProgress(it) },
//        onEndReached = { loadNextBook() },
//        onStartReached = { previousPage() }
//    )
//
//
//    fun initialize(bookId: KomgaBookId) {
//        if (state.value !is Uninitialized) return
//        screenModelScope.launch {
//            layout.value = settingsRepository.getReaderPageLayout().first()
//            scaleType.value = settingsRepository.getReaderScaleType().first()
//            readingDirection.value = settingsRepository.getReaderReadingDirection().first()
//            allowUpsample.value = settingsRepository.getReaderUpsample().first()
//            decoder.value = settingsRepository.getDecoderType().first()
//
//            loadBook(bookId)
//        }
//    }
//
//    private suspend fun loadBook(bookId: KomgaBookId) {
//        mutableState.value = LoadState.Loading
//        appNotifications.runCatchingToNotifications {
//            val newBook = bookClient.getBook(bookId)
//            val pages = bookClient.getBookPages(bookId)
//
//            bookPages.value = pages.map {
//                val width = it.width
//                val height = it.height
//                PageMetadata(
//                    bookId = bookId,
//                    pageNumber = it.number,
//                    size = if (width != null && height != null) IntSize(width, height) else null
//                )
//            }
//            initSpreadMap()
//
//            val readProgress = newBook.readProgress
//            val lastReadSpreadIndex = when {
//                readProgress == null || readProgress.completed -> 0
//                else -> pageSpreads.value.indexOfFirst { spread ->
//                    spread.any { it.pageNumber == readProgress.page }
//                }
//            }
//
//            try {
//                bookSiblingPrev = bookClient.getBookSiblingPrevious(bookId)
//            } catch (e: ClientRequestException) {
//                if (e.response.status != NotFound) throw e
//                bookSiblingPrev = null
//            }
//            try {
//                bookSiblingNext = bookClient.getBookSiblingNext(bookId)
//            } catch (e: ClientRequestException) {
//                if (e.response.status != NotFound) throw e
//                bookSiblingNext = null
//            }
//            currentSpread.value = PageSpread(
//                pageSpreads.value[lastReadSpreadIndex].map { Page(it, null) },
//            )
//            currentSpreadIndex.value = lastReadSpreadIndex
//
//            book.value = newBook
//            if (containerSize.value != null) {
//                loadPage(lastReadSpreadIndex)
//            }
//            mutableState.value = LoadState.Success(newBook)
//        }.onFailure { mutableState.value = LoadState.Error(it) }
//
//    }
//
//    override fun nextPage() {
//        val currentSpreadIndex = currentSpreadIndex.value
//        if (currentSpreadIndex == pageSpreads.value.size - 1) {
//            loadNextBook()
//        } else {
//            onPageChange(currentSpreadIndex + 1)
//        }
//    }
//
//    private fun loadNextBook() {
//        val nextBook = bookSiblingNext
//        if (nextBook != null) {
//            screenModelScope.launch {
//                loadBook(nextBook.id)
//                appNotifications.add(AppNotification.Normal("Loaded next book"))
//            }
//        } else {
//            val currentBook = requireNotNull(book.value)
//            navigator replace MainScreen(BookScreen(currentBook.id))
//        }
//    }
//
//    fun previousPage() {
//        val prevBook = bookSiblingPrev
//        if (prevBook != null) {
//            screenModelScope.launch {
//                loadBook(prevBook.id)
//                appNotifications.add(AppNotification.Normal("Loaded previous book"))
//            }
//        } else
//            appNotifications.add(AppNotification.Normal("You're at the beginning of the book"))
//        return
//    }
//
//    override fun onContentSizeChange(areaSize: IntSize) {
//        if (containerSize.value == areaSize) return
//        containerSize.value = areaSize
//
//        logger.info { "container size change: $areaSize" }
//        loadPage(currentSpreadIndex.value)
//    }
//
//    override fun onPageChange(page: Int) {
//        if (currentSpreadIndex.value == page) return
//        loadPage(page)
//    }
//
//    private fun loadPage(spreadIndex: Int) {
//        if (spreadIndex != currentSpreadIndex.value) {
//            screenModelScope.launch { markReadProgress(spreadIndex) }
//            currentSpreadIndex.value = spreadIndex
//        }
//
//        pageLoadScope.coroutineContext.cancelChildren()
//        pageLoadScope.launch { loadSpread(spreadIndex) }
//    }
//
//    override fun addZoom(zoomMultiplier: Float, focus: Offset) {
//        if (zoomMultiplier == 1.0f) return
//        currentSpreadScale.addZoom(zoomMultiplier, focus)
//        resamplePages()
//    }
//
//    override fun addPan(pan: Offset) {
//        currentSpreadScale.addPan(pan)
//    }
//
//    private suspend fun loadSpread(loadSpreadIndex: Int) {
//        val loadRange = getSpreadLoadRange(loadSpreadIndex)
//        val containerSize = requireNotNull(containerSize.value)
//
//        val maybeUnsizedPages = pageSpreads.value[loadSpreadIndex]
//        val displayJob = readerImageLoader.launchImageLoadJob(
//            scope = pageLoadScope,
//            loadSpread = maybeUnsizedPages,
//            containerSize = containerSize,
//            layout = layout.value,
//            scaleType = scaleType.value,
//            allowUpsample = allowUpsample
//        )
//
//        loadRange.filter { it != loadSpreadIndex }
//            .map { index -> pageSpreads.value[index] }
//            .forEach { spread ->
//                readerImageLoader.launchImageLoadJob(
//                    scope = pageLoadScope,
//                    loadSpread = spread,
//                    containerSize = containerSize,
//                    layout = layout,
//                    scaleType = scaleType,
//                    allowUpsample = allowUpsample
//                )
//            }
//
//        if (displayJob.pageJob.isActive) {
//            currentSpread.value = PageSpread(maybeUnsizedPages.map { Page(it, null) })
//            currentSpreadIndex.value = loadSpreadIndex
//        }
//
//        val loadedPages = displayJob.pageJob.await()
//        val loadedPageMetadata = loadedPages.map { it.metadata }
//        if (maybeUnsizedPages.any { it.size == null }) {
//            pageSpreads.update { current ->
//                val mutable = current.toMutableList()
//                mutable[loadSpreadIndex] = loadedPageMetadata
//                mutable
//            }
//        }
//
//        currentSpreadScale.limitPagesInsideArea(
//            pages = loadedPageMetadata,
//            areaSize = containerSize,
//            maxPageSize = getMaxPageSize(loadedPageMetadata, containerSize),
//            scaleType = scaleType
//        )
//
//
//
//        currentSpread.value = PageSpread(loadedPages)
//        currentSpreadIndex.value = loadSpreadIndex
//    }
//
//    private fun markReadProgress(spreadIndex: Int) {
//        screenModelScope.launch {
//            if (markReadProgress) {
//                val currentBook = requireNotNull(book.value)
//                val pageNumber = pageSpreads.value[spreadIndex].last().pageNumber
//                bookClient.markReadProgress(currentBook.id, KomgaBookReadProgressUpdateRequest(pageNumber))
//            }
//        }
//    }
//
//    private fun resamplePages() {
//        resampleScope.coroutineContext.cancelChildren()
//        resampleScope.launch {
//            delay(100) // debounce
//
//            val resampled = readerImageLoader.loadScaledPages(
//                pages = currentSpread.value.pages.map { it.metadata },
//                containerSize = requireNotNull(containerSize.value),
//                zoomFactor = currentSpreadScale.transformation.value.scale,
//                scaleType = scaleType,
//                allowUpsample = allowUpsample
//            )
//
//            currentSpread.update { it.copy(pages = resampled) }
//        }
//    }
//
//    private fun buildSpreadMapForDoublePages(pages: List<PageMetadata>, offset: Boolean): List<List<PageMetadata>> {
//        val rawSpreads: List<List<PageMetadata>> = if (offset) {
//            buildList {
//                add(listOf(pages.first()))
//                addAll(pages.drop(1).chunked(2))
//            }
//        } else pages.chunked(2)
//
//        val processedSpreads = ArrayList<List<PageMetadata>>()
//        for (rawSpread in rawSpreads) {
//            var currentSpread = emptyList<PageMetadata>()
//
//            for (page in rawSpread) {
//
//                if (page.isLandscape()) {
//                    // if landscape is the second page - add both as separate spreads
//                    if (currentSpread.isNotEmpty()) {
//                        processedSpreads.add(currentSpread)
//                    }
//
//                    processedSpreads.add(listOf(page))
//                    currentSpread = emptyList()
//                } else {
//                    currentSpread = currentSpread + page
//                }
//            }
//
//            if (currentSpread.isNotEmpty()) processedSpreads.add(currentSpread)
//        }
//        return processedSpreads
//
////        return if (offset) {
////            val spreads: MutableList<List<PageMetadata>> = ArrayList()
////
////            // if contains landscape pages - offset every chunk between them
////            val landscapeTerminatedChunks = mutableListOf<List<PageMetadata>>()
////            var currentChunk = mutableListOf<PageMetadata>()
////            for (page in pages) {
////                if (page.isLandscape()) {
////                    currentChunk.add(page)
////                    landscapeTerminatedChunks.add(currentChunk)
////                    currentChunk = mutableListOf()
////                } else {
////                    currentChunk.add(page)
////                }
////            }
////
////            for (chunk in landscapeTerminatedChunks) {
////                spreads.add(listOf(chunk.first()))
////                spreads.addAll(buildDoubleSpreadMap(chunk.drop(1)))
//////                spreads.add(listOf(chunk.last()))
////            }
////            spreads
////
////        } else {
////            buildDoubleSpreadMap(pages)
////        }
//
//    }
//
////    private fun buildDoubleSpreadMap(pages: List<PageMetadata>): List<List<PageMetadata>> {
////        val spreads: MutableList<List<PageMetadata>> = ArrayList()
////        var currentSpread = emptyList<PageMetadata>()
////        for (page in pages) {
////
////            if (page.isLandscape()) {
////                if (currentSpread.isNotEmpty()) {
////                    spreads.add(currentSpread)
////                }
////                spreads.add(listOf(page))
////                currentSpread = emptyList()
////                continue
////            }
////
////            when (currentSpread.size) {
////                0 -> currentSpread = listOf(page)
////                1 -> currentSpread = currentSpread + page
////                2 -> {
////                    spreads.add(currentSpread)
////                    currentSpread = emptyList()
////                }
////
////                else -> throw IllegalStateException("Should not be reachable")
////            }
////        }
////
////        if (currentSpread.isNotEmpty()) {
////            spreads.add(currentSpread)
////        }
////
////        return spreads
////    }
//
//
//    private fun getSpreadLoadRange(spreadIndex: Int): IntRange {
//        val spreads = pageSpreads.value
//        return (spreadIndex - 1).coerceAtLeast(0)..(spreadIndex + 1).coerceAtMost(spreads.size - 1)
//    }
//
//    private fun getMaxPageSize(pages: List<PageMetadata>, containerSize: IntSize): IntSize {
//        return IntSize(
//            width = containerSize.width / pages.size,
//            height = containerSize.height
//        )
//    }
//
//    override fun onLayoutChange(layout: PageDisplayLayout) {
//        this.layout = layout
//        initSpreadMap()
//        val currentPage = currentSpread.value.pages.first().metadata
//
//        loadPage(spreadIndexOf(currentPage))
//        screenModelScope.launch { settingsRepository.putReaderPageLayout(layout) }
//        appNotifications.add(AppNotification.Normal("Changed layout to $layout"))
//    }
//
//    override fun onLayoutCycle() {
//        val options = PageDisplayLayout.entries
//        val newLayout = options[(layout.ordinal + 1) % options.size]
//        onLayoutChange(newLayout)
//    }
//
//    override fun onLayoutOffsetChange(offset: Boolean) {
//        if (layout != DOUBLE_PAGES) return
//        this.layoutOffset = offset
//        initSpreadMap()
//        val currentPage = currentSpread.value.pages.first().metadata
//        loadPage(spreadIndexOf(currentPage))
//        appNotifications.add(AppNotification.Normal("Changed offset"))
//    }
//
//    override fun onScaleTypeChange(scale: LayoutScaleType) {
//        this.scaleType = scale
//        val currentPage = currentSpread.value.pages.first().metadata
//        loadPage(spreadIndexOf(currentPage))
//        screenModelScope.launch { settingsRepository.putReaderScaleType(scale) }
//        appNotifications.add(AppNotification.Normal("Changed scale type to $scale"))
//    }
//
//    override fun onScaleTypeCycle() {
//        val options = LayoutScaleType.entries
//        val newScale = options[(scaleType.ordinal + 1) % options.size]
//        onScaleTypeChange(newScale)
//    }
//
//    override fun onAllowUpsampleChange(upsample: Boolean) {
//        this.allowUpsample = upsample
//        resamplePages()
//        screenModelScope.launch { settingsRepository.putReaderUpsample(upsample) }
//
//        val upsampleText = if (upsample) "Enabled"
//        else "Disabled"
//        appNotifications.add(AppNotification.Normal("$upsampleText upsample beyond image dimensions"))
//    }
//
//    override fun onReadingDirectionChange(readingDirection: ReadingDirection) {
//        this.readingDirection = readingDirection
//        screenModelScope.launch { settingsRepository.putReaderReadingDirection(readingDirection) }
//        appNotifications.add(AppNotification.Normal("Changed reading direction to $readingDirection"))
//    }
//
//    override fun onDecoderChange(type: SamplerType) {
//        this.decoder = type
//        readerImageLoader.clearCache()
//        loadPage(currentSpreadIndex.value)
//        screenModelScope.launch { settingsRepository.putDecoderType(type) }
//    }
//
//    private fun spreadIndexOf(page: PageMetadata): SpreadIndex {
//        return pageSpreads.value.indexOfFirst { spread ->
//            spread.any { it.pageNumber == page.pageNumber }
//        }
//    }
//
//}
//
//data class PageMetadata(
//    val bookId: KomgaBookId,
//    val pageNumber: Int,
//    val size: IntSize?,
//) {
//    fun contentSizeForArea(maxPageSize: IntSize): IntSize {
//        val pageSize = size ?: return maxPageSize
//
//        val bestRatio = (maxPageSize.width.toDouble() / pageSize.width)
//            .coerceAtMost(maxPageSize.height.toDouble() / pageSize.height)
//
//        val scaledSize = IntSize(
//            width = (pageSize.width * bestRatio).roundToInt(),
//            height = (pageSize.height * bestRatio).roundToInt()
//        )
//
//        return scaledSize
//    }
//
//    fun isLandscape(): Boolean {
//        if (size == null) return false
//        return size.width > size.height
//    }
//}
//
//data class PageSpread(
//    val pages: List<Page>,
//)
//
//data class Page(
//    val metadata: PageMetadata,
//    val imageResult: ImageResult?
//)
//
//data class SpreadImageLoadJob(
//    val pageJob: Deferred<List<Page>>,
//    val hash: Int,
//) {
//}
//
//enum class PageDisplayLayout {
//    SINGLE_PAGE,
//    DOUBLE_PAGES,
//}
//
//enum class LayoutScaleType {
//    SCREEN,
//    FIT_WIDTH,
//    FIT_HEIGHT,
//    ORIGINAL
//}
//
//enum class ReadingDirection {
//    LEFT_TO_RIGHT,
//    RIGHT_TO_LEFT
//}

//interface ReaderState {
//    val book: StateFlow<KomgaBook?>
//    val bookPages: StateFlow<List<PageMetadata>>
//    val layout: StateFlow<PageDisplayLayout>
//    val layoutOffset: StateFlow<Boolean>
//    val scaleType: StateFlow<LayoutScaleType>
//    val allowUpsample: StateFlow<Boolean>
//    val readingDirection: StateFlow<ReadingDirection>
//    val decoder: StateFlow<SamplerType?>
//
//    fun onLayoutChange(layout: PageDisplayLayout)
//    fun onLayoutCycle()
//    fun onLayoutOffsetChange(offset: Boolean)
//
//    fun onScaleTypeChange(scale: LayoutScaleType)
//    fun onScaleTypeCycle()
//
//    fun onAllowUpsampleChange(upsample: Boolean)
//
//    fun onReadingDirectionChange(readingDirection: ReadingDirection)
//    fun onDecoderChange(type: SamplerType)
//}
