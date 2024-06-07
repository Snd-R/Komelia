package io.github.snd_r.komelia.ui.reader.paged

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageResult
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
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PagedReaderState(
    imageLoader: ImageLoader,
    imageLoaderContext: PlatformContext,
    private val settingsRepository: ReaderSettingsRepository,
    private val appNotifications: AppNotifications,
    private val readerState: ReaderState,
    val screenScaleState: ScreenScaleState
) {
    private val pagedReaderImageLoader = PagedReaderImageLoader(imageLoader, imageLoaderContext)
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val resampleScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var bookSwitchConfirmed = false
    private val bookSwitchNotificationJobs = mutableMapOf<AppNotification, Job>()

    val pageSpreads = MutableStateFlow<List<List<PageMetadata>>>(emptyList())
    val currentSpread: MutableStateFlow<PageSpread> = MutableStateFlow(PageSpread(emptyList()))
    val currentSpreadIndex = MutableStateFlow(0)

    val layout = MutableStateFlow(SINGLE_PAGE)
    val layoutOffset = MutableStateFlow(false)
    val scaleType = MutableStateFlow(LayoutScaleType.SCREEN)
    val readingDirection = MutableStateFlow(LEFT_TO_RIGHT)
    val imageStretchToFit = MutableStateFlow(true)

    suspend fun initialize() {
        layout.value = settingsRepository.getPagedReaderDisplayLayout().first()
        scaleType.value = settingsRepository.getPagedReaderScaleType().first()
        readingDirection.value = settingsRepository.getPagedReaderReadingDirection().first()
        imageStretchToFit.value = settingsRepository.getPagedReaderStretchToFit().first()

        screenScaleState.setScrollState(null)
        screenScaleState.setScrollOrientation(Orientation.Vertical, false)

        readerState.decoder
            .drop(1)
            .onEach {
                pagedReaderImageLoader.clearCache()
                loadPage(currentSpreadIndex.value)
            }.launchIn(stateScope)

        screenScaleState.areaSize
            .drop(1)
            .onEach { loadPage(currentSpreadIndex.value) }
            .launchIn(stateScope)

        screenScaleState.transformation
            .drop(1)
            .map { it.scale }
            .distinctUntilChanged()
            .onEach { newScaleFactor ->
                if (screenScaleState.targetSize.value != Size.Zero
                    && currentSpread.value.pages.any { it.scaleFactor != newScaleFactor }
                ) {
                    resamplePages()
                }
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
        pagedReaderImageLoader.clearCache()
    }

    private fun onNewBookLoaded(bookState: BookState) {
        val pageSpreads = buildSpreadMap(bookState.currentBookPages, layout.value)
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
            stretchToFit = imageStretchToFit.value
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
                    stretchToFit = imageStretchToFit.value
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
        when (readingDirection.value) {
            LEFT_TO_RIGHT -> screenScaleState.addPan(
                Offset(
                    screenScaleState.offsetXLimits.value.endInclusive,
                    screenScaleState.offsetYLimits.value.endInclusive
                )
            )

            RIGHT_TO_LEFT -> screenScaleState.addPan(
                Offset(
                    screenScaleState.offsetXLimits.value.start,
                    screenScaleState.offsetYLimits.value.endInclusive
                )
            )
        }
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
                stretchToFit = imageStretchToFit.value
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

    fun onStretchToFitChange(stretch: Boolean) {
        this.imageStretchToFit.value = stretch
        stateScope.launch { settingsRepository.putPagedReaderStretchToFit(stretch) }
        loadPage(spreadIndexOf(currentSpread.value.pages.first().metadata))
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

