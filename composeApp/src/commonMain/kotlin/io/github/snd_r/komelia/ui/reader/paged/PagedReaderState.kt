package io.github.snd_r.komelia.ui.reader.paged

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
import io.github.snd_r.komelia.image.ReaderImage
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.BookState
import io.github.snd_r.komelia.ui.reader.ImageCacheKey
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.SpreadIndex
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.paged.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ImageResult.Error
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ImageResult.Success
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

class PagedReaderState(
    private val settingsRepository: ReaderSettingsRepository,
    private val appNotifications: AppNotifications,
    private val readerState: ReaderState,
    private val imageLoader: ReaderImageLoader,
    val screenScaleState: ScreenScaleState,
) {
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var bookSwitchConfirmed = false
    private val bookSwitchNotificationJobs = mutableMapOf<AppNotification, Job>()
    private val imageCache = Cache.Builder<ImageCacheKey, Deferred<Page>>()
        .maximumCacheSize(10)
        .eventListener {
            val value = when (it) {
                is Evicted -> it.value
                is Expired -> it.value
                is Removed -> it.value
                else -> null
            } ?: return@eventListener

            stateScope.launch { value.await().imageResult?.image?.close() }
        }
        .build()

    val pageSpreads = MutableStateFlow<List<List<PageMetadata>>>(emptyList())
    val currentSpread: MutableStateFlow<PageSpread> = MutableStateFlow(PageSpread(emptyList()))
    val currentSpreadIndex = MutableStateFlow(0)

    val layout = MutableStateFlow(SINGLE_PAGE)
    val layoutOffset = MutableStateFlow(false)
    val scaleType = MutableStateFlow(LayoutScaleType.SCREEN)
    val readingDirection = MutableStateFlow(LEFT_TO_RIGHT)

    suspend fun initialize() {
        layout.value = settingsRepository.getPagedReaderDisplayLayout().first()
        scaleType.value = settingsRepository.getPagedReaderScaleType().first()
        readingDirection.value = settingsRepository.getPagedReaderReadingDirection().first()

        screenScaleState.setScrollState(null)
        screenScaleState.setScrollOrientation(Orientation.Vertical, false)

        readerState.imageStretchToFit
            .drop(1)
            .onEach { loadPage(spreadIndexOf(currentSpread.value.pages.first().metadata)) }
            .launchIn(stateScope)

        screenScaleState.areaSize
            .drop(1)
            .onEach { loadPage(currentSpreadIndex.value) }
            .launchIn(stateScope)

        screenScaleState.transformation
            .drop(1)
            .conflate()
            .onEach {
                updateSpreadImageState(
                    currentSpread.value,
                    screenScaleState,
                    readingDirection.value
                )
                delay(100)
            }
            .launchIn(stateScope)

        readerState.booksState
            .filterNotNull()
            .onEach { newBook -> onNewBookLoaded(newBook) }
            .launchIn(stateScope)

        appNotifications.add(AppNotification.Normal("Paged ${readingDirection.value}"))
    }

    fun stop() {
        stateScope.coroutineContext.cancelChildren()
        imageCache.invalidateAll()
    }

    private fun updateSpreadImageState(
        spread: PageSpread,
        screenScaleState: ScreenScaleState,
        readingDirection: ReadingDirection
    ) {
        val maxPageSize = getMaxPageSize(spread.pages.map { it.metadata }, screenScaleState.areaSize.value)
        val zoomFactor = screenScaleState.transformation.value.scale
        val offset = screenScaleState.transformation.value.offset
        val areaSize = screenScaleState.areaSize.value.toSize()
        val stretchToFit = readerState.imageStretchToFit.value


        val pages = spread.pages
        var xOffset = offset.x
        pages.forEachIndexed { index, result ->
            if (result.imageResult is Success) {
                val image = result.imageResult.image
                val imageDisplaySize =
                    if (stretchToFit) image.getDisplaySizeFor(maxPageSize)
                    else image.getDisplaySizeFor(maxPageSize).coerceAtMost(IntSize(image.width, image.height))

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
                    displaySize = imageDisplaySize,
                    visibleDisplaySize = visibleArea,
                    zoomFactor = zoomFactor,
                )
                xOffset += maxPageSize.width

            }
        }
    }

    private fun onNewBookLoaded(bookState: BookState) {
        val pageSpreads = buildSpreadMap(bookState.currentBookPages, layout.value)
        this.pageSpreads.value = pageSpreads

        val lastReadSpreadIndex = pageSpreads.indexOfFirst { spread ->
            spread.any { it.pageNumber == readerState.readProgressPage.value }
        }

        currentSpread.value = PageSpread(
            pages = pageSpreads[lastReadSpreadIndex].map { Page(it, null) },
        )
        currentSpreadIndex.value = lastReadSpreadIndex

        loadPage(lastReadSpreadIndex)
    }

    fun nextPage() {
        val currentSpreadIndex = currentSpreadIndex.value
        when {
            currentSpreadIndex != pageSpreads.value.size - 1 -> onPageChange(currentSpreadIndex + 1)

            !bookSwitchConfirmed -> {
                val notification = if (readerState.booksState.value?.nextBook == null) {
                    AppNotification.Normal("You've reached the end of the book\nClick or press \"Next\" again to exit the reader")
                } else {
                    AppNotification.Normal("You've reached the end of the book\nClick or press next to move to the next book")
                }

                launchBookSwitchNotification(notification)
            }

            else -> {
                bookSwitchNotificationJobs.forEach { (notification, job) ->
                    job.cancel()
                    appNotifications.remove(notification.id)
                    bookSwitchConfirmed = false

                }
                stateScope.launch { readerState.loadNextBook() }

            }

        }

    }

    fun previousPage() {
        val currentSpreadIndex = currentSpreadIndex.value
        when {
            currentSpreadIndex != 0 -> onPageChange(currentSpreadIndex - 1)
            readerState.booksState.value?.previousBook == null -> appNotifications.add(AppNotification.Normal("You're at the beginning of the book"))
            !bookSwitchConfirmed -> launchBookSwitchNotification(
                AppNotification.Normal("You're at the beginning of the book\nClick or press \"Previous\" again to move to the previous book")
            )

            else -> {
                bookSwitchNotificationJobs.forEach { (notification, job) ->
                    job.cancel()
                    appNotifications.remove(notification.id)
                    bookSwitchConfirmed = false

                }
                stateScope.launch { readerState.loadPreviousBook() }
            }
        }
    }

    private fun launchBookSwitchNotification(notification: AppNotification) {
        appNotifications.add(notification)
        bookSwitchConfirmed = true
        bookSwitchNotificationJobs[notification] = stateScope.launch {
            delay(3000)
            bookSwitchConfirmed = false
            appNotifications.remove(notification.id)
        }
    }

    fun onPageChange(page: Int) {
        if (currentSpreadIndex.value == page) return
        loadPage(page)
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

    private fun IntSize.coerceAtMost(other: IntSize): IntSize {
        return IntSize(
            width.coerceAtMost(other.width),
            height.coerceAtMost(other.height)
        )
    }

    private fun IntSize.coerceAtLeast(other: IntSize): IntSize {
        return IntSize(
            width.coerceAtLeast(other.width),
            height.coerceAtLeast(other.height)
        )
    }

    private suspend fun loadSpread(loadSpreadIndex: Int) {
        val loadRange = getSpreadLoadRange(loadSpreadIndex)
        val currentSpreadMetadata = pageSpreads.value[loadSpreadIndex]
        val currentSpreadJob = launchSpreadLoadJob(currentSpreadMetadata)

        loadRange.filter { it != loadSpreadIndex }.forEach { spreadIndex ->
            enqueueSpreadLoadJob(pageSpreads.value[spreadIndex])
        }

        if (currentSpreadJob.isActive) {
            currentSpread.value = PageSpread(currentSpreadMetadata.map { Page(it, null) })
            currentSpreadIndex.value = loadSpreadIndex
        }

        val completedPagesJob = currentSpreadJob.await()
        currentSpread.value = completedPagesJob.spread
        currentSpreadIndex.value = loadSpreadIndex

        val newScale = completedPagesJob.scale
        screenScaleState.apply(newScale)
    }

    data class PagesLoadJob(
        val spread: PageSpread,
        val scale: ScreenScaleState
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun launchSpreadLoadJob(pagesMeta: List<PageMetadata>): Deferred<PagesLoadJob> {
        val pages = pagesMeta.map { meta ->
            val cacheKey = ImageCacheKey(meta.bookId, meta.pageNumber)
            val cached = imageCache.get(cacheKey)

            if (cached != null && !cached.isCancelled) cached
            else pageLoadScope.async {
                Page(meta, imageLoader.load(meta.bookId, meta.pageNumber))
            }.also { imageCache.put(cacheKey, it) }
        }

        if (pages.all { it.isCompleted }) {
            val completed = pages.map { it.getCompleted() }
            return CompletableDeferred(completeLoadJob(completed))
        } else {
            return pageLoadScope.async {
                val completed = pages.map { it.await() }
                completeLoadJob(completed)
            }
        }
    }

    private fun completeLoadJob(pages: List<Page>): PagesLoadJob {
        val containerSize = screenScaleState.areaSize.value
        val maxPageSize = getMaxPageSize(pages.map { it.metadata }, containerSize)
        val newScale = calculateScreenScale(
            pages,
            areaSize = containerSize,
            maxPageSize = maxPageSize,
            scaleType = scaleType.value,
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
        if (currentLayout != DOUBLE_PAGES) return
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

    enum class ReadingDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    data class PageSpread(val pages: List<Page>)

    data class Page(
        val metadata: PageMetadata,
        val imageResult: ImageResult?,
    )

    data class SpreadImageLoadJob(
        val pages: List<Page>,
        val scale: ScreenScaleState,
    )

    sealed interface ImageResult {
        val image: ReaderImage?

        data class Success(
            override val image: ReaderImage,
        ) : ImageResult

        data class Error(
            val throwable: Throwable,
        ) : ImageResult {

            override val image: ReaderImage? = null
        }

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

data class ImageLoadParams(
    val zoomScaleFactor: Float,
    val displayScaleFactor: Float,
    val targetSize: IntSize
)

private fun calculateScreenScale(
    pages: List<PagedReaderState.Page>,
    areaSize: IntSize,
    maxPageSize: IntSize,
    scaleType: LayoutScaleType,
    stretchToFit: Boolean,
): ScreenScaleState {
    val scaleState = ScreenScaleState()
    scaleState.setAreaSize(areaSize)
    val fitToScreenSize = pages
        .map {
            when (it.imageResult) {
                is Error, null -> maxPageSize
                is Success -> it.imageResult.image.getDisplaySizeFor(maxPageSize)
            }
        }
        .reduce { total, current ->
            IntSize(
                width = (total.width + current.width),
                height = max(total.height, current.height)
            )
        }
    scaleState.setTargetSize(fitToScreenSize.toSize())

    val actualSpreadSize = pages.map {
        when (val result = it.imageResult) {
            is Error, null -> maxPageSize
            is Success -> IntSize(result.image.width, result.image.height)
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

private fun zoomForOriginalSize(originalSize: IntSize, targetSize: IntSize, scaleFor100Percent: Float): Float {
    return max(
        originalSize.width.toFloat() / targetSize.width,
        originalSize.height.toFloat() / targetSize.height
    ) / scaleFor100Percent
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

