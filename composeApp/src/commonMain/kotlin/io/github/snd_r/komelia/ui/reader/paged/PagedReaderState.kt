package io.github.snd_r.komelia.ui.reader.paged

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageResult
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.BookState
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.SpreadIndex
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageSpread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class PagedReaderState(
    imageLoader: ImageLoader,
    imageLoaderContext: PlatformContext,
    private val settingsRepository: ReaderSettingsRepository,
    private val appNotifications: AppNotifications,
    private val readerState: ReaderState,
    val screenScaleState: ScreenScaleState
) : PagedReaderPageState {
    private val pagedReaderImageLoader = PagedReaderImageLoader(imageLoader, imageLoaderContext)
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val resampleScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val pageSpreads = MutableStateFlow<List<List<PageMetadata>>>(emptyList())
    override val currentSpread: MutableStateFlow<PageSpread> = MutableStateFlow(PageSpread(emptyList()))
    override val currentSpreadIndex = MutableStateFlow(0)

    override val layout = MutableStateFlow(SINGLE_PAGE)
    override val layoutOffset = MutableStateFlow(false)
    override val scaleType = MutableStateFlow(LayoutScaleType.SCREEN)
    override val readingDirection = MutableStateFlow(ReadingDirection.LEFT_TO_RIGHT)

    suspend fun initialize() {
        layout.value = settingsRepository.getPagedReaderDisplayLayout().first()
        scaleType.value = settingsRepository.getPagedReaderScaleType().first()
        readingDirection.value = settingsRepository.getPagedReaderReadingDirection().first()

        readerState.decoder
            .drop(1)
            .onEach {
                pagedReaderImageLoader.clearCache()
                loadPage(currentSpreadIndex.value)
            }.launchIn(stateScope)

        readerState.bookState
            .filterNotNull()
            .onEach { newBook -> onNewBookLoaded(newBook) }
            .launchIn(stateScope)

        screenScaleState.areaSize
            .onEach { loadPage(currentSpreadIndex.value) }
            .launchIn(stateScope)

        screenScaleState.transformation
            .map { it.scale }
            .filter { it.isFinite() }
            .distinctUntilChanged()
            .onEach { newScaleFactor ->
                if (currentSpread.value.pages.any { it.scaleFactor != newScaleFactor })
                    resamplePages()
            }
            .launchIn(stateScope)

        readerState.allowUpsample
            .drop(1)
            .onEach { resamplePages() }
            .launchIn(stateScope)

    }

    fun stop() {
        stateScope.coroutineContext.cancelChildren()
        pagedReaderImageLoader.clearCache()
    }

    private fun onNewBookLoaded(bookState: BookState) {
        val pageSpreads = buildSpreadMap(bookState.bookPages, layout.value)
        this.pageSpreads.value = pageSpreads

        val lastReadSpreadIndex = pageSpreads.indexOfFirst { spread ->
            spread.any { it.pageNumber == readerState.readProgressPage.value }
        }

        currentSpread.value = PageSpread(
            pages = pageSpreads[lastReadSpreadIndex].map { Page(it, null, null) },
        )
        currentSpreadIndex.value = lastReadSpreadIndex

        loadPage(lastReadSpreadIndex)
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

        pageLoadScope.coroutineContext.cancelChildren()
        pageLoadScope.launch { loadSpread(spreadIndex) }
    }

    private suspend fun loadSpread(loadSpreadIndex: Int) {
        val loadRange = getSpreadLoadRange(loadSpreadIndex)
        val containerSize = screenScaleState.areaSize.value

        val maybeUnsizedPages = pageSpreads.value[loadSpreadIndex]
        val displayJob = pagedReaderImageLoader.launchImageLoadJob(
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
                pagedReaderImageLoader.launchImageLoadJob(
                    scope = pageLoadScope,
                    loadPages = spread,
                    containerSize = containerSize,
                    layout = layout.value,
                    scaleType = scaleType.value,
                    allowUpsample = readerState.allowUpsample.value
                )
            }

        if (displayJob.isActive) {
            currentSpread.value = PageSpread(maybeUnsizedPages.map { Page(it, null, null) })
            currentSpreadIndex.value = loadSpreadIndex
        }

        val completedJob = displayJob.await()
        val loadedPageMetadata = completedJob.pages.map { it.metadata }
        if (maybeUnsizedPages.any { it.size == null }) {
            pageSpreads.update { current ->
                val mutable = current.toMutableList()
                mutable[loadSpreadIndex] = loadedPageMetadata
                mutable
            }
        }

        currentSpread.value = PageSpread(completedJob.pages)
        currentSpreadIndex.value = loadSpreadIndex

        screenScaleState.setTargetSize(
            targetSize = completedJob.scale.targetSize.value,
            zoom = completedJob.scale.zoom.value
        )
    }

    private fun resamplePages() {
        resampleScope.coroutineContext.cancelChildren()
        resampleScope.launch {
            delay(100) // debounce

            val currentScaleFactor = screenScaleState.transformation.value.scale
            val resampled = pagedReaderImageLoader.loadScaledPages(
                pages = currentSpread.value.pages.map { it.metadata },
                containerSize = screenScaleState.areaSize.value,
                zoomFactor = currentScaleFactor,
                scaleType = scaleType.value,
                allowUpsample = readerState.allowUpsample.value
            )

            currentSpread.update { current ->
                current.copy(
                    pages = resampled.map { Page(it.page, it.image, currentScaleFactor) })
            }
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

    override fun onLayoutChange(layout: PageDisplayLayout) {
        this.layout.value = layout
        stateScope.launch { settingsRepository.putPagedReaderDisplayLayout(layout) }
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
        stateScope.launch { settingsRepository.putPagedReaderScaleType(scale) }
        appNotifications.add(AppNotification.Normal("Changed scale type to $scale"))
    }

    override fun onScaleTypeCycle() {
        val options = LayoutScaleType.entries
        val newScale = options[(scaleType.value.ordinal + 1) % options.size]
        onScaleTypeChange(newScale)
    }

    override fun onReadingDirectionChange(readingDirection: ReadingDirection) {
        this.readingDirection.value = readingDirection
        stateScope.launch { settingsRepository.putPagedReaderReadingDirection(readingDirection) }
        appNotifications.add(AppNotification.Normal("Changed reading direction to $readingDirection"))
    }

    enum class ReadingDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    data class PageSpread(val pages: List<Page>)

    data class Page(
        val metadata: PageMetadata,
        val imageResult: ImageResult?,
        val scaleFactor: Float?,
    )

    data class SpreadImageLoadJob(
        val pages: List<Page>,
        val scale: ScreenScaleState,
    )

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

    val layout: StateFlow<PageDisplayLayout>
    val layoutOffset: StateFlow<Boolean>
    val scaleType: StateFlow<LayoutScaleType>
    val readingDirection: StateFlow<PagedReaderState.ReadingDirection>

    fun onPageChange(page: Int)
    fun nextPage()
    fun previousPage()

    fun onLayoutChange(layout: PageDisplayLayout)
    fun onLayoutCycle()
    fun onLayoutOffsetChange(offset: Boolean)
    fun onScaleTypeChange(scale: LayoutScaleType)
    fun onScaleTypeCycle()
    fun onReadingDirectionChange(readingDirection: PagedReaderState.ReadingDirection)
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

