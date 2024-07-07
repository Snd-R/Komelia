package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.ReaderImage
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.ImageCacheKey
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ImageResult
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger("ContinuousReaderState")

class ContinuousReaderState(
    private val imageLoader: ReaderImageLoader,
    private val readerState: ReaderState,
    private val settingsRepository: ReaderSettingsRepository,
    private val notifications: AppNotifications,
    val screenScaleState: ScreenScaleState,
) {
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val imageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))

    val lazyListState = LazyListState(0, 0)

    val readingDirection = MutableStateFlow(TOP_TO_BOTTOM)
    val sidePaddingFraction = MutableStateFlow(.3f)
    val sidePaddingPx = MutableStateFlow(0)
    val pageSpacing = MutableStateFlow(0)
    val imageStretchToFit = readerState.imageStretchToFit.asStateFlow()

    val pageIntervals = MutableStateFlow<List<BookPagesInterval>>(emptyList())
    private val currentIntervalIndex = MutableStateFlow(0)

    val currentBookPages = readerState.booksState.filterNotNull().map { it.currentBookPages }
    val currentBookPageIndex = readerState.readProgressPage.map { it - 1 }

    private val nextBook = readerState.booksState.filterNotNull().map { it.nextBook }
    private val previousBook = readerState.booksState.filterNotNull().map { it.previousBook }

    private val imageCache = Cache.Builder<ImageCacheKey, Deferred<ImageResult>>().maximumCacheSize(10).build()
    private val imagesInUse: MutableMap<ImageCacheKey, ReaderImage> = HashMap()

    suspend fun initialize() {
        imagesInUse.clear()

        readingDirection.value = settingsRepository.getContinuousReaderReadingDirection().first()
        sidePaddingFraction.value = settingsRepository.getContinuousReaderPadding().first()
        pageSpacing.value = settingsRepository.getContinuousReaderPageSpacing().first()

        screenScaleState.setScrollState(lazyListState)
        when (readingDirection.value) {
            TOP_TO_BOTTOM -> screenScaleState.setScrollOrientation(Orientation.Vertical, false)
            LEFT_TO_RIGHT -> screenScaleState.setScrollOrientation(Orientation.Horizontal, false)
            RIGHT_TO_LEFT -> screenScaleState.setScrollOrientation(Orientation.Horizontal, true)
        }

        readerState.booksState.filterNotNull()
            .onEach { newState ->
                val currentIntervals = pageIntervals.value
                val currentBook = currentIntervals.getOrNull(currentIntervalIndex.value)?.book
                val wasPreviousBookLoaded = currentBook?.id == newState.nextBook?.id

                when {
                    currentIntervals.isEmpty() -> {
                        pageIntervals.value = listOfNotNull(
                            newState.previousBook?.let { BookPagesInterval(it, newState.previousBookPages) },
                            BookPagesInterval(newState.currentBook, newState.currentBookPages),
                            newState.nextBook?.let { BookPagesInterval(it, newState.nextBookPages) }
                        )

                        if (newState.previousBook == null) {
                            currentIntervalIndex.value = 0
                            lazyListState.scrollToItem(currentBookPageIndex.first() + 1)
                        } else {
                            currentIntervalIndex.value = 1
                            val bookStartIndex = pageIntervals.value.first().pages.size
                            val readProgress = currentBookPageIndex.first()
                            lazyListState.scrollToItem(bookStartIndex + readProgress + 2)
                        }
                    }

                    wasPreviousBookLoaded -> {
                        val isNew = newState.previousBook != null
                                && currentIntervals.none { it.book.id == newState.previousBook.id }

                        // on new interval prepend interval index stays the same because old previous interval becomes current one
                        // int4 -> int5 -> int6
                        //          ^current
                        // becomes
                        //  int3 -> int4 -> int5 -> int6
                        //           ^current
                        if (isNew) {
                            withContext(Dispatchers.Default) {
                                pageIntervals.value = listOf(
                                    BookPagesInterval(
                                        newState.previousBook,
                                        // https://issuetracker.google.com/issues/273025639
                                        // can only prepend 130 elements without losing current items position
                                        newState.previousBookPages.takeLast(100)
                                    )
                                ).plus(currentIntervals)
                            }

                        }
                        // decrement interval index if previous interval already exists
                        // int4 -> int5 -> int6 -> int7
                        //                  ^current
                        // becomes
                        // int4 -> int5 -> int6 -> int7
                        //          ^current
                        else {
                            currentIntervalIndex.update { it - 1 }
                        }
                    }

                    !wasPreviousBookLoaded -> {
                        val isNew = newState.nextBook != null
                                && currentIntervals.none { it.book.id == newState.nextBook.id }

                        if (isNew) {
                            withContext(Dispatchers.Default) {
                                pageIntervals.value = currentIntervals.plus(
                                    BookPagesInterval(
                                        newState.nextBook,
                                        newState.nextBookPages
                                    )
                                )
                            }
                        }
                        currentIntervalIndex.update { it + 1 }
                    }

                }

            }.launchIn(stateScope)

        screenScaleState.areaSize
            .filter { it != IntSize.Zero }
            .onEach {
                applyPadding()
                screenScaleState.setZoom(0f)
            }
            .launchIn(stateScope)

        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            .combine(screenScaleState.targetSize) { _, _ -> }
            .combine(sidePaddingFraction) { _, _ -> }
            .combine(imageStretchToFit) { _, _ -> }
            .combine(screenScaleState.transformation.map { it.scale }.distinctUntilChanged()) { _, _ -> }
            .conflate()
            .onEach {
                updateVisibleImages()
                delay(100)
            }.launchIn(stateScope)

        notifications.add(AppNotification.Normal("Continuous ${readingDirection.value}"))
    }

    fun stop() {
        stateScope.coroutineContext.cancelChildren()
        imageLoadScope.coroutineContext.cancelChildren()
        pageIntervals.value = emptyList()
        currentIntervalIndex.value = 0
        imageCache.invalidateAll()
    }

    suspend fun onCurrentPageChange(page: PageMetadata) {
        when (page.bookId) {
            nextBook.first()?.id -> {
                readerState.loadNextBook()
                readerState.onProgressChange(page.pageNumber)
            }

            previousBook.first()?.id -> {
                readerState.loadPreviousBook()
                readerState.onProgressChange(page.pageNumber)
            }

            else -> {
                checkAndLoadMissingIntervalPagesAt(page)
                readerState.onProgressChange(page.pageNumber)
            }
        }
    }

    private suspend fun checkAndLoadMissingIntervalPagesAt(page: PageMetadata) {
        val currentIntervalIndex = currentIntervalIndex.value
        val currentInterval = pageIntervals.value[currentIntervalIndex]

        // only check for backwards scrolling and page prepending
        if (currentInterval.pages.first().pageNumber == 1) return

        val fullBookPages = currentBookPages.first()
        if (currentInterval.pages.size == fullBookPages.size) return

        val missingPagesSize = fullBookPages.size - currentInterval.pages.size

        if (page.pageNumber == missingPagesSize + 1) {
            withContext(Dispatchers.Default) {
                val updatedPages = fullBookPages.subList(
                    (missingPagesSize - 100).coerceAtLeast(0),
                    missingPagesSize
                ).plus(currentInterval.pages)
                val updatedInterval = currentInterval.copy(pages = updatedPages)

                pageIntervals.update { current ->
                    current.toMutableList().apply { set(currentIntervalIndex, updatedInterval) }
                }
            }
        }
    }

    suspend fun scrollToBookPage(pageNumber: Int) {
        val currentIntervalIndex = currentIntervalIndex.value
        val currentInterval = pageIntervals.value[currentIntervalIndex]

        val fullBookPages = currentBookPages.first()
        val hasMissingPages = currentInterval.pages.size != fullBookPages.size
        if (hasMissingPages) {
            withContext(Dispatchers.Default) {
                val updatedInterval = currentInterval.copy(pages = fullBookPages)
                pageIntervals.update { current ->
                    current.toMutableList().apply { set(currentIntervalIndex, updatedInterval) }
                }
            }
        }

        val intervals = pageIntervals.value
        val bookStartIndex = intervals.subList(0, currentIntervalIndex)
            .fold(0) { acc, value -> acc + value.pages.size } - 1

        if (hasMissingPages)
        //FIXME can't properly change scroll position before newly added items are recomposed and registered in layout
        // as a workaround use animated scroll that will cause recomposition and will keep scrolling until item index is reached
        // requestScrollToItem() should solve this issue: https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/LazyListState#requestScrollToItem(kotlin.Int,kotlin.Int)
        // waiting until compose multiplatform is updated to use 1.7.0 foundation dependency
            lazyListState.animateScrollToItem(bookStartIndex + pageNumber + currentIntervalIndex + 1)
        else lazyListState.scrollToItem(bookStartIndex + pageNumber + currentIntervalIndex + 1)
    }

    suspend fun scrollScreenForward() {
        val containerSize = screenScaleState.areaSize.value
        when (readingDirection.value) {
            TOP_TO_BOTTOM -> animateScrollBy(containerSize.height.toFloat())
            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> animateScrollBy(containerSize.width.toFloat())
        }
    }

    suspend fun scrollScreenBackward() {
        val containerSize = screenScaleState.areaSize.value
        when (readingDirection.value) {
            TOP_TO_BOTTOM -> animateScrollBy(-containerSize.height.toFloat())
            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> animateScrollBy(-containerSize.width.toFloat())
        }
    }

    private suspend fun animateScrollBy(amount: Float) {
        when {
            amount > 0 && lazyListState.canScrollForward -> {
                lazyListState.animateScrollBy(amount, spring(stiffness = Spring.StiffnessLow))
            }

            amount < 0 && lazyListState.canScrollBackward -> {
                lazyListState.animateScrollBy(amount, spring(stiffness = Spring.StiffnessLow))
            }

            else -> scrollBy(amount)
        }
    }

    fun scrollBy(amount: Float) {
        when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                screenScaleState.addPan(Offset(0f, amount))
            }

            LEFT_TO_RIGHT -> {
                screenScaleState.addPan(Offset(amount, 0f))
            }

            RIGHT_TO_LEFT -> {
                screenScaleState.addPan(Offset(amount, 0f))
            }
        }
    }

    private fun getPagesFor(bookId: KomgaBookId): List<PageMetadata>? {
        val intervals = pageIntervals.value
        val currentIntervalIndex = currentIntervalIndex.value

        val currentInterval = intervals[currentIntervalIndex]
        val nextInterval = intervals.getOrNull(currentIntervalIndex + 1)
        val previousInterval = intervals.getOrNull(currentIntervalIndex - 1)

        return when (bookId) {
            currentInterval.book.id -> currentInterval.pages
            nextInterval?.book?.id -> nextInterval.pages
            previousInterval?.book?.id -> previousInterval.pages

            else -> intervals.firstOrNull { it.book.id == bookId }?.pages
        }
    }

    suspend fun getImage(requestPage: PageMetadata): ImageResult {
        val requestedPageJob = launchImageJob(requestPage)

        val nextPage = getPagesFor(requestPage.bookId)?.getOrNull(requestPage.pageNumber)
        if (nextPage != null) {
            if (!imagesInUse.containsKey(nextPage.toCacheKey())) {
                imageLoadScope.launch {
                    val result = launchImageJob(nextPage).await()
                    result.image?.let {
                        it.requestUpdate(
                            getImageDisplaySize(it),
                            screenScaleState.areaSize.value.toIntRect(),
                            screenScaleState.transformation.value.scale
                        )
                    }

                }
            }
        }

        return requestedPageJob.await()
    }

    private fun launchImageJob(requestPage: PageMetadata): Deferred<ImageResult> {
        val cacheKey = ImageCacheKey(requestPage.bookId, requestPage.pageNumber)
        val cached = imageCache.get(cacheKey)

        val job = if (cached != null && !cached.isCancelled) cached
        else imageLoadScope.async {
            imageLoader.load(requestPage.bookId, requestPage.pageNumber)
        }.also { imageCache.put(cacheKey, it) }


        return job
    }

    private fun updateVisibleImages() {
        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return

        val firstItem = visibleItems.first()

        val visiblePages = visibleItems.filter { it.key is PageMetadata }.map { it.key as PageMetadata }
        val visibleImages = visiblePages.associateWith { page -> imagesInUse[page.toCacheKey()] }

        val scale = screenScaleState.transformation.value.scale
        val firstItemOffset = lazyListState.firstVisibleItemScrollOffset

        visibleImages.values.first()?.let { image ->
            val firstImageSize = getImageDisplaySize(image)

            if (firstItem.key is PageMetadata) {
                val visibleArea = when (readingDirection.value) {
                    TOP_TO_BOTTOM -> IntRect(
                        left = 0,
                        top = firstItemOffset,
                        right = firstImageSize.width,
                        bottom = firstImageSize.height
                    )

                    LEFT_TO_RIGHT -> IntRect(
                        left = firstItemOffset,
                        top = 0,
                        right = firstImageSize.width,
                        bottom = firstImageSize.height
                    )

                    RIGHT_TO_LEFT -> IntRect(
                        left = firstItemOffset,
                        top = 0,
                        right = firstImageSize.width,
                        bottom = firstImageSize.height
                    )
                }
                image.requestUpdate(
                    firstImageSize,
                    visibleArea,
                    scale
                )
            } else {
                image.requestUpdate(
                    firstImageSize,
                    firstImageSize.toIntRect(),
                    scale
                )
            }
        }
        if (visibleImages.size == 1) return

        if (visibleImages.size > 2) {
            visibleImages.values.drop(1).dropLast(1).filterNotNull()
                .forEach { image ->
                    val size = getImageDisplaySize(image)
                    image.requestUpdate(size, size.toIntRect(), scale)
                }
        }

        val containerSize = screenScaleState.areaSize.value
        val lastItem = visibleItems.last()
        val lastImage = visibleImages.values.last() ?: return
        val lastImageSize = getImageDisplaySize(lastImage)
        val lastImageVisibleArea = when (readingDirection.value) {
            TOP_TO_BOTTOM -> IntRect(
                left = 0,
                top = 0,
                right = lastImageSize.width,
                bottom = containerSize.height - lastItem.offset
            )

            LEFT_TO_RIGHT -> IntRect(
                left = 0,
                top = 0,
                right = containerSize.width - lastItem.offset,
                bottom = containerSize.height
            )

            RIGHT_TO_LEFT -> IntRect(
                left = 0,
                top = 0,
                right = containerSize.width - lastItem.offset,
                bottom = containerSize.height
            )
        }
        lastImage.requestUpdate(
            lastImageSize,
            lastImageVisibleArea,
            scale
        )
    }

    private fun getImageDisplaySize(image: ReaderImage): IntSize {
        val containerSize = screenScaleState.areaSize.value
        return when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                val width = containerSize.width - (sidePaddingPx.value * 2)
                val displayWidth =
                    if (!readerState.imageStretchToFit.value) width.coerceAtMost(image.width)
                    else width

                image.getDisplaySizeFor(
                    IntSize(
                        displayWidth,
                        Int.MAX_VALUE
                    )
                )
            }

            else -> {
                val height = containerSize.height - (sidePaddingPx.value * 2)
                val displayHeight =
                    if (!readerState.imageStretchToFit.value) height.coerceAtMost(image.height)
                    else height

                image.getDisplaySizeFor(
                    IntSize(
                        Int.MAX_VALUE,
                        displayHeight
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPageDisplaySize(page: PageMetadata): IntSize {
        val containerSize = screenScaleState.areaSize.value

        val cached = imageCache.get(ImageCacheKey(page.bookId, page.pageNumber))
        if (cached != null && cached.isCompleted) {
            val image = cached.getCompleted().image
            if (image != null) return getImageDisplaySize(image)
        }

        val displaySize = when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                val constrainedWidth = containerSize.width - (sidePaddingPx.value * 2)
                when {
                    page.size == null -> {
                        val previousPage = getPagesFor(page.bookId)?.getOrNull(page.pageNumber - 2)
                        val nextPage = getPagesFor(page.bookId)?.getOrNull(page.pageNumber)
                        val otherPageSize = previousPage?.size ?: nextPage?.size
                        if (otherPageSize != null) {
                            contentSizeForArea(
                                contentSize = otherPageSize,
                                maxPageSize = IntSize(constrainedWidth, Int.MAX_VALUE)
                            )
                        } else {
                            IntSize(containerSize.width, (containerSize.height / 2))
                        }
                    }

                    !readerState.imageStretchToFit.value -> contentSizeForArea(
                        contentSize = page.size,
                        maxPageSize = IntSize(constrainedWidth.coerceAtMost(page.size.width), page.size.height)
                    )

                    else -> contentSizeForArea(
                        contentSize = page.size,
                        maxPageSize = IntSize(constrainedWidth, Int.MAX_VALUE)
                    )
                }
            }

            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> {
                val constrainedHeight = containerSize.height - (sidePaddingPx.value * 2)
                when {
                    page.size == null -> {
                        val previousPage = getPagesFor(page.bookId)?.getOrNull(page.pageNumber - 2)
                        val nextPage = getPagesFor(page.bookId)?.getOrNull(page.pageNumber)
                        val otherPageSize = previousPage?.size ?: nextPage?.size
                        if (otherPageSize != null) {
                            contentSizeForArea(
                                contentSize = otherPageSize,
                                maxPageSize = IntSize(Int.MAX_VALUE, constrainedHeight)
                            )
                        } else {
                            IntSize((containerSize.width / 2), containerSize.height)
                        }
                    }

                    !readerState.imageStretchToFit.value -> contentSizeForArea(
                        contentSize = page.size,
                        maxPageSize = IntSize(page.size.width, constrainedHeight.coerceAtMost(page.size.height))
                    )

                    else -> contentSizeForArea(
                        contentSize = page.size,
                        maxPageSize = IntSize(Int.MAX_VALUE, constrainedHeight)
                    )
                }

            }
        }

        return displaySize
    }

    private fun updatePageSize(page: PageMetadata, image: ReaderImage) {
        val updated = page.copy(size = IntSize(image.width, image.height))

        pageIntervals.update { current ->
            val intervals = pageIntervals.value
            val currentIntervalIndex = currentIntervalIndex.value

            val intervalIndex = when (updated.bookId) {
                intervals[currentIntervalIndex].book.id -> currentIntervalIndex
                intervals.getOrNull(currentIntervalIndex + 1)?.book?.id -> currentIntervalIndex + 1
                intervals.getOrNull(currentIntervalIndex - 1)?.book?.id -> currentIntervalIndex - 1

                else -> intervals.indexOfFirst { it.book.id == updated.bookId }
            }
            if (intervalIndex == -1) return@update current

            val interval = current[intervalIndex]
            val updatedPages = interval.pages.toMutableList()
            updatedPages[updated.pageNumber - 1] = updated

            val updatedIntervals = current.toMutableList()
            updatedIntervals[intervalIndex] = interval.copy(pages = updatedPages)

            updatedIntervals
        }
    }

    fun onReadingDirectionChange(direction: ReadingDirection) {
        this.readingDirection.value = direction
        when (direction) {
            TOP_TO_BOTTOM -> screenScaleState.setScrollOrientation(Orientation.Vertical, false)
            LEFT_TO_RIGHT -> screenScaleState.setScrollOrientation(Orientation.Horizontal, false)
            RIGHT_TO_LEFT -> screenScaleState.setScrollOrientation(Orientation.Horizontal, true)
        }

        applyPadding()
        stateScope.launch { settingsRepository.putContinuousReaderReadingDirection(direction) }
    }

    fun onSidePaddingChange(fraction: Float) {
        this.sidePaddingFraction.value = fraction
        applyPadding()
        screenScaleState.setZoom(0f)
        stateScope.launch { settingsRepository.putContinuousReaderPadding(fraction) }
    }

    fun onPageSpacingChange(distance: Int) {
        this.pageSpacing.value = distance
        stateScope.launch { settingsRepository.putContinuousReaderPageSpacing(distance) }
    }

    fun onPageDisplay(page: PageMetadata, image: ReaderImage) {
        imagesInUse[page.toCacheKey()] = image
        if (page.size == null) updatePageSize(page, image)
        updateVisibleImages()
    }

    fun onPageDispose(page: PageMetadata) {
        imagesInUse.remove(page.toCacheKey())
    }

    private fun applyPadding() {
        val contentSize = screenScaleState.areaSize.value
        val (padding, targetSize) = when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                val padding = (contentSize.width * sidePaddingFraction.value).roundToInt()
                val size = Size(
                    width = contentSize.width.toFloat() - padding * 2,
                    height = contentSize.height.toFloat()
                )
                padding to size
            }

            else -> {
                val padding = (contentSize.height * sidePaddingFraction.value).roundToInt()
                val size = Size(
                    width = contentSize.width.toFloat(),
                    height = contentSize.height.toFloat() - padding * 2
                )
                padding to size
            }
        }

        sidePaddingPx.value = padding
        screenScaleState.setTargetSize(targetSize)
    }

    private fun contentSizeForArea(contentSize: IntSize, maxPageSize: IntSize): IntSize {
        val bestRatio = (maxPageSize.width.toDouble() / contentSize.width)
            .coerceAtMost(maxPageSize.height.toDouble() / contentSize.height)

        val scaledSize = IntSize(
            width = (contentSize.width * bestRatio).roundToInt(),
            height = (contentSize.height * bestRatio).roundToInt()
        )

        return scaledSize
    }

    enum class ReadingDirection {
        TOP_TO_BOTTOM,
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    data class BookPagesInterval(
        val book: KomgaBook,
        val pages: List<PageMetadata>
    )
}