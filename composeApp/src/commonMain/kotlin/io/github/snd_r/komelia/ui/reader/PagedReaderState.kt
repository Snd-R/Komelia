package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout.SINGLE_PAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class PagedReaderState(
    private val readerImageLoader: ReaderImageLoader,
    private val settingsRepository: SettingsRepository,
    private val appNotifications: AppNotifications,
    private val readerState: ReaderState,
) : PagedReaderPageState {
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val resampleScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val pageSpreads = MutableStateFlow<List<List<PageMetadata>>>(emptyList())
    override val currentSpread: MutableStateFlow<PageSpread> = MutableStateFlow(PageSpread.EMPTY_SPREAD)
    override val currentSpreadIndex = MutableStateFlow(0)
    override val containerSize = MutableStateFlow<IntSize?>(null)
    override val layout = MutableStateFlow(SINGLE_PAGE)
    override val layoutOffset = MutableStateFlow(false)
    override val scaleType = MutableStateFlow(LayoutScaleType.SCREEN)
    override val readingDirection = MutableStateFlow(ReadingDirection.LEFT_TO_RIGHT)

    suspend fun initialize() {
        layout.value = settingsRepository.getReaderPageLayout().first()
        scaleType.value = settingsRepository.getReaderScaleType().first()
        readingDirection.value = settingsRepository.getReaderReadingDirection().first()

        readerState.decoder
            .drop(1)
            .onEach {
                readerImageLoader.clearCache()
                loadPage(currentSpreadIndex.value)
            }.launchIn(stateScope)

        readerState.bookState
            .filterNotNull()
            .onEach { newBook -> onNewBookLoaded(newBook) }
            .launchIn(stateScope)
    }

    private fun onNewBookLoaded(bookState: BookState) {
        val pageSpreads = buildSpreadMap(bookState.bookPages, layout.value)
        this.pageSpreads.value = pageSpreads

        val readProgress = bookState.book.readProgress
        val lastReadSpreadIndex = when {
            readProgress == null || readProgress.completed -> 0
            else -> pageSpreads.indexOfFirst { spread ->
                spread.any { it.pageNumber == readProgress.page }
            }
        }

        currentSpread.value = PageSpread(
            pages = pageSpreads[lastReadSpreadIndex].map { Page(it, null) },
        )
        currentSpreadIndex.value = lastReadSpreadIndex

        if (containerSize.value != null) {
            loadPage(lastReadSpreadIndex)
        }
    }

    override fun nextPage() {
        val currentSpreadIndex = currentSpreadIndex.value
        if (currentSpreadIndex == pageSpreads.value.size - 1) {
            readerState.loadNextBook()
        } else {
            onPageChange(currentSpreadIndex + 1)
        }
    }

    override fun previousPage() {
        val currentSpreadIndex = currentSpreadIndex.value
        if (currentSpreadIndex == 0) {
            readerState.loadPreviousBook()
            return
        }

        onPageChange(currentSpreadIndex - 1)
    }

    override fun onPageChange(page: Int) {
        if (currentSpreadIndex.value == page) return
        loadPage(page)
    }

    private fun loadPage(spreadIndex: Int) {
        if (spreadIndex != currentSpreadIndex.value) {
            val pageNumber = pageSpreads.value[spreadIndex].last().pageNumber
            readerState.onProgressChange(pageNumber)
            currentSpreadIndex.value = spreadIndex
        }

        pageLoadScope.launch { loadSpread(spreadIndex) }
    }

    private suspend fun loadSpread(loadSpreadIndex: Int) {
        val loadRange = getSpreadLoadRange(loadSpreadIndex)
        val containerSize = requireNotNull(containerSize.value)

        val maybeUnsizedPages = pageSpreads.value[loadSpreadIndex]
        val displayJob = readerImageLoader.launchImageLoadJob(
            scope = pageLoadScope,
            loadPages = maybeUnsizedPages,
            containerSize = containerSize,
            layout = layout.value,
            scaleType = scaleType.value,
            allowUpsample = readerState.allowUpsample.value
        )

        loadRange.filter { it != loadSpreadIndex }
            .map { index -> pageSpreads.value[index] }
            .forEach { spread ->
                readerImageLoader.launchImageLoadJob(
                    scope = pageLoadScope,
                    loadPages = spread,
                    containerSize = containerSize,
                    layout = layout.value,
                    scaleType = scaleType.value,
                    allowUpsample = readerState.allowUpsample.value
                )
            }

        if (displayJob.pageJob.isActive) {
            currentSpread.value = PageSpread(maybeUnsizedPages.map { Page(it, null) }, null)
            currentSpreadIndex.value = loadSpreadIndex
        }

        val loadedPages = displayJob.pageJob.await()
        val loadedPageMetadata = loadedPages.map { it.metadata }
        if (maybeUnsizedPages.any { it.size == null }) {
            pageSpreads.update { current ->
                val mutable = current.toMutableList()
                mutable[loadSpreadIndex] = loadedPageMetadata
                mutable
            }
        }

        val scaleState = PageSpreadScaleState()
        scaleState.limitPagesInsideArea(
            pages = loadedPageMetadata,
            areaSize = containerSize,
            maxPageSize = getMaxPageSize(loadedPageMetadata, containerSize),
            scaleType = scaleType.value
        )

        currentSpread.value = PageSpread(loadedPages, scaleState)
        currentSpreadIndex.value = loadSpreadIndex
    }

//    private suspend fun markReadProgress(spreadIndex: Int) {
//        if (markReadProgress) {
//            val currentBook = requireNotNull(book)
//            val pageNumber = pageSpreads.value[spreadIndex].last().pageNumber
//            bookClient.markReadProgress(currentBook.id, KomgaBookReadProgressUpdateRequest(pageNumber))
//        }
//    }

    private fun resamplePages() {
        val containerSize = containerSize.value ?: return
        val scaleFactor = currentSpread.value.scaleState?.transformation?.value?.scale ?: return
        resampleScope.coroutineContext.cancelChildren()
        resampleScope.launch {
            delay(100) // debounce

            val resampled = readerImageLoader.loadScaledPages(
                pages = currentSpread.value.pages.map { it.metadata },
                containerSize = containerSize,
                zoomFactor = scaleFactor,
                scaleType = scaleType.value,
                allowUpsample = readerState.allowUpsample.value
            )

            currentSpread.update { it.copy(pages = resampled) }
        }
    }

    private fun buildSpreadMap(pages: List<PageMetadata>, layout: PageDisplayLayout): List<List<PageMetadata>> {
        return when (layout) {
            SINGLE_PAGE -> pages.map { listOf(it) }

            DOUBLE_PAGES -> buildSpreadMapForDoublePages(pages, layoutOffset.value)
        }
    }

    private fun buildSpreadMapForDoublePages(pages: List<PageMetadata>, offset: Boolean): List<List<PageMetadata>> {
        val rawSpreads: List<List<PageMetadata>> = if (offset) {
            buildList {
                add(listOf(pages.first()))
                addAll(pages.drop(1).chunked(2))
            }
        } else pages.chunked(2)

        val processedSpreads = ArrayList<List<PageMetadata>>()
        for (rawSpread in rawSpreads) {
            var currentSpread = emptyList<PageMetadata>()

            for (page in rawSpread) {

                if (page.isLandscape()) {
                    // if landscape is the second page - add both as separate spreads
                    if (currentSpread.isNotEmpty()) {
                        processedSpreads.add(currentSpread)
                    }

                    processedSpreads.add(listOf(page))
                    currentSpread = emptyList()
                } else {
                    currentSpread = currentSpread + page
                }
            }

            if (currentSpread.isNotEmpty()) processedSpreads.add(currentSpread)
        }
        return processedSpreads
    }

    private fun spreadIndexOf(page: PageMetadata): SpreadIndex {
        return pageSpreads.value.indexOfFirst { spread ->
            spread.any { it.pageNumber == page.pageNumber }
        }
    }

    private fun getSpreadLoadRange(spreadIndex: Int): IntRange {
        val spreads = pageSpreads.value
        return (spreadIndex - 1).coerceAtLeast(0)..(spreadIndex + 1).coerceAtMost(spreads.size - 1)
    }

    private fun getMaxPageSize(pages: List<PageMetadata>, containerSize: IntSize): IntSize {
        return IntSize(
            width = containerSize.width / pages.size,
            height = containerSize.height
        )
    }

    override fun onContentSizeChange(areaSize: IntSize) {
        if (containerSize.value == areaSize) return
        containerSize.value = areaSize

        logger.info { "container size change: $areaSize" }
        loadPage(currentSpreadIndex.value)
    }


    override fun addZoom(zoomMultiplier: Float, focus: Offset) {
        val currentScale = currentSpread.value.scaleState ?: return
        when {
            zoomMultiplier > 1 && !currentScale.canZoomIn() -> return
            zoomMultiplier < 1 && !currentScale.canZoomOUt() -> return
            else -> {
                currentScale.addZoom(zoomMultiplier, focus)
                resamplePages()
            }
        }
    }

    override fun addPan(pan: Offset) {
        val currentScale = currentSpread.value.scaleState ?: return
        currentScale.addPan(pan)
    }

    override fun onLayoutChange(layout: PageDisplayLayout) {
        this.layout.value = layout
        stateScope.launch { settingsRepository.putReaderPageLayout(layout) }
        appNotifications.add(AppNotification.Normal("Changed layout to $layout"))

        val pages = readerState.bookState.value?.bookPages ?: return
        pageSpreads.value = buildSpreadMap(pages, layout)

        val currentPage = currentSpread.value.pages.first().metadata
        loadPage(spreadIndexOf(currentPage))
    }

    override fun onLayoutCycle() {
        val options = PageDisplayLayout.entries
        val newLayout = options[(layout.value.ordinal + 1) % options.size]
        onLayoutChange(newLayout)
    }

    override fun onLayoutOffsetChange(offset: Boolean) {
        val currentLayout = layout.value
        if (currentLayout != DOUBLE_PAGES) return
        this.layoutOffset.value = offset

        val pages = readerState.bookState.value?.bookPages ?: return
        pageSpreads.value = buildSpreadMap(pages, currentLayout)

        val currentPage = currentSpread.value.pages.first().metadata
        loadPage(spreadIndexOf(currentPage))
        appNotifications.add(AppNotification.Normal("Changed offset"))
    }

    override fun onScaleTypeChange(scale: LayoutScaleType) {
        this.scaleType.value = scale
        val currentPage = currentSpread.value.pages.first().metadata
        loadPage(spreadIndexOf(currentPage))
        stateScope.launch { settingsRepository.putReaderScaleType(scale) }
        appNotifications.add(AppNotification.Normal("Changed scale type to $scale"))
    }

    override fun onScaleTypeCycle() {
        val options = LayoutScaleType.entries
        val newScale = options[(scaleType.value.ordinal + 1) % options.size]
        onScaleTypeChange(newScale)
    }

    override fun onReadingDirectionChange(readingDirection: ReadingDirection) {
        this.readingDirection.value = readingDirection
        stateScope.launch { settingsRepository.putReaderReadingDirection(readingDirection) }
        appNotifications.add(AppNotification.Normal("Changed reading direction to $readingDirection"))
    }
}

//        return if (offset) {
//            val spreads: MutableList<List<PageMetadata>> = ArrayList()
//
//            // if contains landscape pages - offset every chunk between them
//            val landscapeTerminatedChunks = mutableListOf<List<PageMetadata>>()
//            var currentChunk = mutableListOf<PageMetadata>()
//            for (page in pages) {
//                if (page.isLandscape()) {
//                    currentChunk.add(page)
//                    landscapeTerminatedChunks.add(currentChunk)
//                    currentChunk = mutableListOf()
//                } else {
//                    currentChunk.add(page)
//                }
//            }
//
//            for (chunk in landscapeTerminatedChunks) {
//                spreads.add(listOf(chunk.first()))
//                spreads.addAll(buildDoubleSpreadMap(chunk.drop(1)))
////                spreads.add(listOf(chunk.last()))
//            }
//            spreads
//
//        } else {
//            buildDoubleSpreadMap(pages)
//        }


//    private fun buildDoubleSpreadMap(pages: List<PageMetadata>): List<List<PageMetadata>> {
//        val spreads: MutableList<List<PageMetadata>> = ArrayList()
//        var currentSpread = emptyList<PageMetadata>()
//        for (page in pages) {
//
//            if (page.isLandscape()) {
//                if (currentSpread.isNotEmpty()) {
//                    spreads.add(currentSpread)
//                }
//                spreads.add(listOf(page))
//                currentSpread = emptyList()
//                continue
//            }
//
//            when (currentSpread.size) {
//                0 -> currentSpread = listOf(page)
//                1 -> currentSpread = currentSpread + page
//                2 -> {
//                    spreads.add(currentSpread)
//                    currentSpread = emptyList()
//                }
//
//                else -> throw IllegalStateException("Should not be reachable")
//            }
//        }
//
//        if (currentSpread.isNotEmpty()) {
//            spreads.add(currentSpread)
//        }
//
//        return spreads
//    }

interface PagedReaderPageState {
    val currentSpread: StateFlow<PageSpread>
    val pageSpreads: StateFlow<List<List<PageMetadata>>>
    val currentSpreadIndex: StateFlow<SpreadIndex>
    val containerSize: StateFlow<IntSize?>

    val layout: StateFlow<PageDisplayLayout>
    val layoutOffset: StateFlow<Boolean>
    val scaleType: StateFlow<LayoutScaleType>
    val readingDirection: StateFlow<ReadingDirection>

    fun onContentSizeChange(areaSize: IntSize)
    fun onPageChange(page: Int)
    fun nextPage()
    fun previousPage()
    fun addZoom(zoomMultiplier: Float, focus: Offset)
    fun addPan(pan: Offset)

    fun onLayoutChange(layout: PageDisplayLayout)
    fun onLayoutCycle()
    fun onLayoutOffsetChange(offset: Boolean)
    fun onScaleTypeChange(scale: LayoutScaleType)
    fun onScaleTypeCycle()
    fun onReadingDirectionChange(readingDirection: ReadingDirection)
}


enum class PageDisplayLayout {
    SINGLE_PAGE,
    DOUBLE_PAGES,
}

enum class LayoutScaleType {
    SCREEN,
    FIT_WIDTH,
    FIT_HEIGHT,
    ORIGINAL
}

enum class ReadingDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}
