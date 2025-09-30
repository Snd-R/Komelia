package io.github.snd_r.komelia.ui.reader.image.paged

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.reactivecircus.cache4k.Cache
import io.github.reactivecircus.cache4k.CacheEvent.Evicted
import io.github.reactivecircus.cache4k.CacheEvent.Expired
import io.github.reactivecircus.cache4k.CacheEvent.Removed
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.ReaderImageResult
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.strings.AppStrings
import io.github.snd_r.komelia.ui.reader.image.BookState
import io.github.snd_r.komelia.ui.reader.image.PageMetadata
import io.github.snd_r.komelia.ui.reader.image.ReaderState
import io.github.snd_r.komelia.ui.reader.image.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.image.SpreadIndex
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.DOUBLE_PAGES_NO_COVER
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage.BookEnd
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage.BookStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.common.KomgaReadingDirection
import kotlin.math.max
import kotlin.math.roundToInt

class PagedReaderState(
    private val cleanupScope: CoroutineScope,
    private val settingsRepository: ImageReaderSettingsRepository,
    private val appNotifications: AppNotifications,
    private val readerState: ReaderState,
    private val imageLoader: BookImageLoader,
    private val appStrings: Flow<AppStrings>,
    private val pageChangeFlow: MutableSharedFlow<Unit>,
    val screenScaleState: ScreenScaleState,
) {
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val pageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val imageCache = Cache.Builder<PageId, Deferred<Page>>()
        .maximumCacheSize(10)
        .eventListener {
            val value = when (it) {
                is Evicted -> it.value
                is Expired -> it.value
                is Removed -> it.value
                else -> null
            } ?: return@eventListener

            cleanupScope.launch { value.await().imageResult?.image?.close() }
        }
        .build()

    val pageSpreads = MutableStateFlow<List<List<PageMetadata>>>(emptyList())
    val currentSpreadIndex = MutableStateFlow(0)
    val currentSpread: MutableStateFlow<PageSpread> = MutableStateFlow(PageSpread(emptyList()))
    val transitionPage: MutableStateFlow<TransitionPage?> = MutableStateFlow(null)

    val layout = MutableStateFlow(SINGLE_PAGE)
    val layoutOffset = MutableStateFlow(false)
    val scaleType = MutableStateFlow(LayoutScaleType.SCREEN)
    val readingDirection = MutableStateFlow(LEFT_TO_RIGHT)

    suspend fun initialize() {
        layout.value = settingsRepository.getPagedReaderDisplayLayout().first()
        scaleType.value = settingsRepository.getPagedReaderScaleType().first()
        readingDirection.value = when (readerState.series.value?.metadata?.readingDirection) {
            KomgaReadingDirection.LEFT_TO_RIGHT -> LEFT_TO_RIGHT
            KomgaReadingDirection.RIGHT_TO_LEFT -> RIGHT_TO_LEFT
            else -> settingsRepository.getPagedReaderReadingDirection().first()
        }

        screenScaleState.setScrollState(null)
        screenScaleState.setScrollOrientation(Orientation.Vertical, false)

        combine(
            screenScaleState.transformation,
            screenScaleState.areaSize,
            readerState.imageStretchToFit
        ) { }.drop(1)
            .conflate()
            .onEach {
                val spread = currentSpread.value
                updateSpreadImageState(
                    spread = spread,
                    screenScaleState = screenScaleState,
                    readingDirection = readingDirection.value,
                )
                val containerSize = screenScaleState.areaSize.value
                val maxPageSize = getMaxPageSize(spread.pages.map { it.metadata }, containerSize)
               val targetSize= fitToScreenZoom(spread.pages, maxPageSize, layout.value)
                screenScaleState.setTargetSize(targetSize.toSize())
                delay(100)
            }
            .launchIn(stateScope)

        readerState.booksState
            .filterNotNull()
            .onEach { newBook -> onNewBookLoaded(newBook) }
            .launchIn(stateScope)

        val strings = appStrings.first().pagedReader
        appNotifications.add(AppNotification.Normal("Paged ${strings.forReadingDirection(readingDirection.value)}"))
    }

    fun stop() {
        stateScope.coroutineContext.cancelChildren()
        imageCache.invalidateAll()
    }

    private suspend fun updateSpreadImageState(
        spread: PageSpread,
        screenScaleState: ScreenScaleState,
        readingDirection: ReadingDirection,
    ) {
        val maxPageSize = getMaxPageSize(spread.pages.map { it.metadata }, screenScaleState.areaSize.value)
        val zoomFactor = screenScaleState.transformation.value.scale
        val offset = screenScaleState.transformation.value.offset
        val areaSize = screenScaleState.areaSize.value.toSize()
        val stretchToFit = readerState.imageStretchToFit.value


        val pages = spread.pages
        var xOffset = offset.x
        pages.forEachIndexed { index, result ->
            if (result.imageResult is ReaderImageResult.Success) {
                val image = result.imageResult.image
                val imageDisplaySize = image.calculateSizeForArea(maxPageSize, stretchToFit)

                val imageHorizontalVisibleWidth =
                    (imageDisplaySize.width * zoomFactor - areaSize.width) / 2

                val imageHorizontalDisplayOffset = when (pages.size) {
                    1 -> offset.x
                    2 -> {
                        //TODO simplify
                        when (readingDirection) {
                            LEFT_TO_RIGHT -> {
                                if (index == 0) offset.x - (imageDisplaySize.width / 2 * zoomFactor)
                                else offset.x + (imageDisplaySize.width / 2 * zoomFactor)
                            }

                            RIGHT_TO_LEFT -> {
                                if (index == 0) offset.x + (imageDisplaySize.width / 2 * zoomFactor)
                                else offset.x - (imageDisplaySize.width / 2 * zoomFactor)
                            }
                        }
                    }

                    else -> throw IllegalStateException("can't display more than 2 images")   //TODO 3 or more images?
                }

                val top =
                    ((((imageDisplaySize.height * zoomFactor - areaSize.height) / 2) - offset.y) / zoomFactor)
                        .roundToInt()
                        .coerceIn(0..imageDisplaySize.height)

                val left =
                    ((imageHorizontalVisibleWidth - imageHorizontalDisplayOffset) / zoomFactor)
                        .roundToInt()
                        .coerceIn(0..imageDisplaySize.width)

                val visibleArea = IntRect(
                    top = top,
                    left = left,
                    bottom = (top + areaSize.height / zoomFactor)
                        .roundToInt()
                        .coerceAtMost(imageDisplaySize.height),
                    right = (left + (areaSize.width) / zoomFactor)
                        .roundToInt()
                        .coerceAtMost(imageDisplaySize.width),
                )

                image.requestUpdate(
                    visibleDisplaySize = visibleArea,
                    zoomFactor = zoomFactor,
                    maxDisplaySize = maxPageSize
                )
                xOffset += maxPageSize.width

            }
        }
    }

    private fun onNewBookLoaded(bookState: BookState) {
        val pageSpreads = buildSpreadMap(bookState.currentBookPages, layout.value)
        this.pageSpreads.value = pageSpreads

        val newSpreadIndex = pageSpreads.indexOfFirst { spread ->
            spread.any { it.pageNumber == readerState.readProgressPage.value }
        }

        currentSpread.value = PageSpread(
            pages = pageSpreads[newSpreadIndex].map { Page(it, null) },
        )
        currentSpreadIndex.value = newSpreadIndex

        loadPage(newSpreadIndex)
    }

    fun nextPage() {
        val currentSpreadIndex = currentSpreadIndex.value
        val currentTransitionPage = transitionPage.value
        when {
            currentSpreadIndex < pageSpreads.value.size - 1 -> {
                if (currentTransitionPage != null) this.transitionPage.value = null
                else onPageChange(currentSpreadIndex + 1)
            }

            currentTransitionPage == null -> {
                val bookState = readerState.booksState.value ?: return
                this.transitionPage.value = BookEnd(
                    currentBook = bookState.currentBook,
                    nextBook = bookState.nextBook
                )
            }

            currentTransitionPage is BookEnd && currentTransitionPage.nextBook != null -> {
                stateScope.launch {
                    currentSpread.value = PageSpread(emptyList())
                    transitionPage.value = null
                    readerState.loadNextBook()
                }
            }
        }
    }

    fun previousPage() {
        val currentSpreadIndex = currentSpreadIndex.value
        val currentTransitionPage = transitionPage.value
        when {
            currentSpreadIndex != 0 -> {
                if (currentTransitionPage != null) this.transitionPage.value = null
                else onPageChange(currentSpreadIndex - 1)

            }

            currentTransitionPage == null -> {
                val bookState = readerState.booksState.value ?: return
                this.transitionPage.value = BookStart(
                    currentBook = bookState.currentBook,
                    previousBook = bookState.previousBook
                )
            }

            currentTransitionPage is BookStart && currentTransitionPage.previousBook != null -> {
                stateScope.launch {
                    currentSpread.value = PageSpread(emptyList())
                    transitionPage.value = null
                    readerState.loadPreviousBook()
                }
            }
        }
    }

    fun onPageChange(page: Int) {
        if (currentSpreadIndex.value == page) return
        pageChangeFlow.tryEmit(Unit)
        loadPage(page)
    }

    fun moveToLastPage() {
        val lastPageIndex = pageSpreads.value.size - 1
        if (currentSpreadIndex.value == lastPageIndex) return
        pageChangeFlow.tryEmit(Unit)
        loadPage(lastPageIndex)
    }

    private fun loadPage(spreadIndex: Int) {
        if (spreadIndex != currentSpreadIndex.value) {
            val pageNumber = pageSpreads.value[spreadIndex].last().pageNumber
            stateScope.launch { readerState.onProgressChange(pageNumber) }
            currentSpreadIndex.value = spreadIndex
        }

        pageLoadScope.coroutineContext.cancelChildren()
        pageLoadScope.launch { loadSpread(spreadIndex) }
    }

    private suspend fun loadSpread(loadSpreadIndex: Int) {
        val loadRange = getSpreadLoadRange(loadSpreadIndex)
        val currentSpreadMetadata = pageSpreads.value[loadSpreadIndex]
        val currentSpreadJob = launchSpreadLoadJob(currentSpreadMetadata)

        loadRange.filter { it != loadSpreadIndex }.forEach { spreadIndex ->
            enqueueSpreadLoadJob(pageSpreads.value[spreadIndex])
        }

        if (currentSpreadJob.isActive) {
            delay(10)
            if (currentSpreadJob.isActive) {
                currentSpread.value = PageSpread(currentSpreadMetadata.map { Page(it, null) })
                currentSpreadIndex.value = loadSpreadIndex
                transitionPage.value = null
            }
        }

        val completedPagesJob = currentSpreadJob.await()
        currentSpread.value = completedPagesJob.spread
        currentSpreadIndex.value = loadSpreadIndex
        transitionPage.value = null

        val newScale = completedPagesJob.scale
        screenScaleState.apply(newScale)
    }

    data class PagesLoadJob(
        val spread: PageSpread,
        val scale: ScreenScaleState
    )

    private fun launchSpreadLoadJob(pagesMeta: List<PageMetadata>): Deferred<PagesLoadJob> {
        val pages = pagesMeta.map { meta ->
            val pageId = meta.toPageId()
            val cached = imageCache.get(pageId)

            if (cached != null && !cached.isCancelled) cached
            else pageLoadScope.async {
                val imageResult = imageLoader.loadReaderImage(meta.bookId, meta.pageNumber)
                Page(meta, imageResult)
            }.also { imageCache.put(pageId, it) }
        }

        return pageLoadScope.async {
            val completed = pages.awaitAll()
            completeLoadJob(completed)
        }
    }

    private suspend fun completeLoadJob(pages: List<Page>): PagesLoadJob {
        val containerSize = screenScaleState.areaSize.value
        val maxPageSize = getMaxPageSize(pages.map { it.metadata }, containerSize)
        val newScale = calculateScreenScale(
            pages,
            areaSize = containerSize,
            maxPageSize = maxPageSize,
            scaleType = scaleType.value,
            displayLayout = layout.value,
            stretchToFit = readerState.imageStretchToFit.value
        )
        val spread = PageSpread(pages)
        when (readingDirection.value) {
            LEFT_TO_RIGHT -> newScale.addPan(
                Offset(
                    newScale.offsetXLimits.value.endInclusive,
                    newScale.offsetYLimits.value.endInclusive
                )
            )

            RIGHT_TO_LEFT -> newScale.addPan(
                Offset(
                    newScale.offsetXLimits.value.start,
                    newScale.offsetYLimits.value.endInclusive
                )
            )
        }
        updateSpreadImageState(spread, newScale, readingDirection.value)

        return PagesLoadJob(spread, newScale)

    }

    @Suppress("DeferredResultUnused")
    private fun enqueueSpreadLoadJob(pagesMeta: List<PageMetadata>) {
        launchSpreadLoadJob(pagesMeta)
    }

    private fun getMaxPageSize(pages: List<PageMetadata>, containerSize: IntSize): IntSize {
        return IntSize(
            width = containerSize.width / pages.size,
            height = containerSize.height
        )
    }

    private fun buildSpreadMap(pages: List<PageMetadata>, layout: PageDisplayLayout): List<List<PageMetadata>> {
        return when (layout) {
            SINGLE_PAGE -> pages.map { listOf(it) }
            DOUBLE_PAGES -> buildSpreadMapForDoublePages(
                pages = pages,
                withCover = true,
                offset = layoutOffset.value
            )

            DOUBLE_PAGES_NO_COVER -> buildSpreadMapForDoublePages(
                pages = pages,
                withCover = false,
                offset = layoutOffset.value
            )
        }
    }

    data class DoublePageSegment(
        val singlePages: List<PageMetadata>,
        val landscapePage: PageMetadata?
    )

    private fun buildSpreadMapForDoublePages(
        pages: List<PageMetadata>,
        withCover: Boolean,
        offset: Boolean
    ): List<List<PageMetadata>> {
        val segments = constructDoublePageSegments(pages, withCover)

        val processedSpreads = mutableListOf<List<PageMetadata>>()
        for (segment in segments) {
            val segmentPages = segment.singlePages

            if (offset) {
                segmentPages.firstOrNull()?.let { processedSpreads.add(listOf(it)) }
                processedSpreads.addAll(segmentPages.drop(1).chunked(2))
            } else processedSpreads.addAll(segmentPages.chunked(2))

            segment.landscapePage?.let { processedSpreads.add(listOf(it)) }

        }

        return processedSpreads
    }

    private fun constructDoublePageSegments(
        pages: List<PageMetadata>,
        withCover: Boolean,
    ): List<DoublePageSegment> {
        val independentSegments = mutableListOf<DoublePageSegment>()

        val pagesToProcess = if (withCover) {
            independentSegments.add(DoublePageSegment(listOf(pages.first()), null))
            pages.drop(1)
        } else pages

        var segmentPages: MutableList<PageMetadata> = mutableListOf()
        for (page in pagesToProcess) {
            if (page.isLandscape()) {
                independentSegments.add(DoublePageSegment(segmentPages, page))
                segmentPages = mutableListOf()
            } else {
                segmentPages.add(page)
            }
        }
        if (segmentPages.isNotEmpty()) independentSegments.add(DoublePageSegment(segmentPages, null))

        return independentSegments
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

    fun onLayoutChange(layout: PageDisplayLayout) {
        this.layout.value = layout
        stateScope.launch { settingsRepository.putPagedReaderDisplayLayout(layout) }

        val pages = readerState.booksState.value?.currentBookPages ?: return
        pageSpreads.value = buildSpreadMap(pages, layout)

        val currentPage = currentSpread.value.pages.first().metadata
        loadPage(spreadIndexOf(currentPage))
    }

    fun onLayoutCycle() {
        val options = PageDisplayLayout.entries
        val newLayout = options[(layout.value.ordinal + 1) % options.size]
        onLayoutChange(newLayout)
    }

    fun onLayoutOffsetChange(offset: Boolean) {
        val currentLayout = layout.value
        if (currentLayout == SINGLE_PAGE) return
        this.layoutOffset.value = offset

        val pages = readerState.booksState.value?.currentBookPages ?: return
        pageSpreads.value = buildSpreadMap(pages, currentLayout)

        val currentPage = currentSpread.value.pages.first().metadata
        loadPage(spreadIndexOf(currentPage))
    }

    fun onScaleTypeChange(scale: LayoutScaleType) {
        this.scaleType.value = scale
        val currentPage = currentSpread.value.pages.first().metadata
        loadPage(spreadIndexOf(currentPage))
        stateScope.launch { settingsRepository.putPagedReaderScaleType(scale) }
    }

    fun onScaleTypeCycle() {
        val options = LayoutScaleType.entries
        val newScale = options[(scaleType.value.ordinal + 1) % options.size]
        onScaleTypeChange(newScale)
    }

    fun onReadingDirectionChange(readingDirection: ReadingDirection) {
        this.readingDirection.value = readingDirection
        stateScope.launch { settingsRepository.putPagedReaderReadingDirection(readingDirection) }
    }

    private suspend fun calculateScreenScale(
        pages: List<Page>,
        areaSize: IntSize,
        maxPageSize: IntSize,
        scaleType: LayoutScaleType,
        displayLayout: PageDisplayLayout,
        stretchToFit: Boolean,
    ): ScreenScaleState {
        val scaleState = ScreenScaleState()
        scaleState.setAreaSize(areaSize)

        val fitToScreenSize = fitToScreenZoom(pages, maxPageSize, displayLayout)
        scaleState.setTargetSize(fitToScreenSize.toSize())

        val actualSpreadSize = pages.map {
            when (val result = it.imageResult) {
                is ReaderImageResult.Error, null -> maxPageSize
                is ReaderImageResult.Success -> result.image.getOriginalImageSize()
            }
        }.fold(IntSize.Zero) { total, current ->
            IntSize(
                (total.width + current.width),
                max(total.height, current.height)
            )
        }

        when (scaleType) {
            LayoutScaleType.SCREEN -> scaleState.setZoom(0f)
            LayoutScaleType.FIT_WIDTH -> {
                if (!stretchToFit && areaSize.width > actualSpreadSize.width) {
                    val newZoom = zoomForOriginalSize(
                        actualSpreadSize,
                        fitToScreenSize,
                        scaleState.scaleFor100PercentZoom()
                    )
                    scaleState.setZoom(newZoom.coerceAtMost(1.0f))
                } else if (fitToScreenSize.width < areaSize.width) scaleState.setZoom(1f)
                else scaleState.setZoom(0f)
            }

            LayoutScaleType.FIT_HEIGHT -> {
                if (!stretchToFit && areaSize.height > actualSpreadSize.height) {
                    val newZoom = zoomForOriginalSize(
                        actualSpreadSize,
                        fitToScreenSize,
                        scaleState.scaleFor100PercentZoom()
                    )
                    scaleState.setZoom(newZoom.coerceAtMost(1.0f))

                } else if (fitToScreenSize.height < areaSize.height) scaleState.setZoom(1f)
                else scaleState.setZoom(0f)
            }

            LayoutScaleType.ORIGINAL -> {
                if (actualSpreadSize.width > areaSize.width || actualSpreadSize.height > areaSize.height) {
                    val newZoom = zoomForOriginalSize(
                        actualSpreadSize,
                        fitToScreenSize,
                        scaleState.scaleFor100PercentZoom()
                    )
                    scaleState.setZoom(newZoom)

                } else scaleState.setZoom(0f)
            }
        }

        return scaleState
    }

    private suspend fun fitToScreenZoom(
        pages: List<Page>,
        maxPageSize: IntSize,
        displayLayout: PageDisplayLayout,
    ): IntSize {
        return when (displayLayout) {
            SINGLE_PAGE -> {
                check(pages.size == 1)
                val imageResult = pages.first().imageResult
                when (imageResult) {
                    is ReaderImageResult.Error, null -> maxPageSize
                    is ReaderImageResult.Success -> imageResult.image.calculateSizeForArea(maxPageSize, true)
                }
            }

            DOUBLE_PAGES, DOUBLE_PAGES_NO_COVER -> {
                if (pages.size == 1 && !pages.first().metadata.isLandscape()) {
                    val imageResult = pages.first().imageResult
                    val singlePageSize = when (imageResult) {
                        is ReaderImageResult.Error, null -> maxPageSize
                        is ReaderImageResult.Success -> imageResult.image.calculateSizeForArea(maxPageSize, true)
                    }
                    IntSize(singlePageSize.width * 2, singlePageSize.height)
                } else {
                    pages
                        .map {
                            when (it.imageResult) {
                                is ReaderImageResult.Error, null -> maxPageSize
                                is ReaderImageResult.Success -> {
                                    it.imageResult.image.calculateSizeForArea(maxPageSize, true)
                                }
                            }
                        }
                        .reduce { total, current ->
                            IntSize(
                                width = (total.width + current.width),
                                height = max(total.height, current.height)
                            )
                        }
                }
            }
        }

    }

    private fun zoomForOriginalSize(originalSize: IntSize, targetSize: IntSize, scaleFor100Percent: Float): Float {
        return max(
            originalSize.width.toFloat() / targetSize.width,
            originalSize.height.toFloat() / targetSize.height
        ) / scaleFor100Percent
    }

    enum class PageDisplayLayout {
        SINGLE_PAGE,
        DOUBLE_PAGES,
        DOUBLE_PAGES_NO_COVER,
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

    data class PageSpread(val pages: List<Page>)

    data class Page(
        val metadata: PageMetadata,
        val imageResult: ReaderImageResult?,
    )


    sealed interface TransitionPage {
        data class BookEnd(
            val currentBook: KomgaBook,
            val nextBook: KomgaBook?,
        ) : TransitionPage

        data class BookStart(
            val currentBook: KomgaBook,
            val previousBook: KomgaBook?,
        ) : TransitionPage
    }
}
