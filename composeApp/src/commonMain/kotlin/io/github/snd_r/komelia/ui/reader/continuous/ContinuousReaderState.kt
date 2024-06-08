package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.size.Dimension
import coil3.size.Precision
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val imageLoader: ImageLoader,
    private val imageLoaderContext: PlatformContext,
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

    suspend fun initialize() {
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

    fun scrollForward(amount: Float) {
        when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                screenScaleState.addPan(Offset(0f, -amount))
            }

            LEFT_TO_RIGHT -> {
                screenScaleState.addPan(Offset(-amount, 0f))
            }

            RIGHT_TO_LEFT -> {
                screenScaleState.addPan(Offset(amount, 0f))
            }
        }
    }

    fun scrollBackward(amount: Float) {
        when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                screenScaleState.addPan(Offset(0f, amount))
            }


            LEFT_TO_RIGHT -> {
                screenScaleState.addPan(Offset(amount, 0f))

            }

            RIGHT_TO_LEFT -> {
                screenScaleState.addPan(Offset(-amount, 0f))

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

    fun getContentSizePx(page: PageMetadata): IntSize {
        val containerSize = screenScaleState.areaSize.value

        val constrained = when (readingDirection.value) {
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

        return constrained
    }

    suspend fun getImage(requestPage: PageMetadata): ImageResult {
        val requestedPageJob = launchImageJob(requestPage)

        val nextPage = getPagesFor(requestPage.bookId)?.getOrNull(requestPage.pageNumber)
        if (nextPage != null) {
            imageLoadScope.async { launchImageJob(nextPage) }
        }

        val result = requestedPageJob.await()
        val cacheKey = ImageCacheKey(
            bookId = requestPage.bookId,
            pageNumber = requestPage.pageNumber,
            size = result.request.sizeResolver.size()
        )
        imageCache.invalidate(cacheKey)

        return result
    }

    private suspend fun launchImageJob(requestPage: PageMetadata): Deferred<ImageResult> {
        return withContext(imageLoadScope.coroutineContext) {
            val page = if (requestPage.size == null) setOriginalSize(requestPage) else requestPage
            val targetSize = getImageRequestSize(page)
            val cacheKey = ImageCacheKey(
                bookId = requestPage.bookId,
                pageNumber = requestPage.pageNumber,
                size = targetSize
            )
            val cached = imageCache.get(cacheKey)
            if (cached != null) {
                return@withContext cached
            }


            val targetSizeString = buildString {
                append((targetSize.width as? Dimension.Pixels)?.px?.toString() ?: "Undefined")
                append(" x ")
                append((targetSize.height as? Dimension.Pixels)?.px?.toString() ?: "Undefined")
            }
            logger.info { "image request for page: $page; target size: $targetSizeString" }
            val request = ImageRequest.Builder(imageLoaderContext)
                .data(page)
                .size(targetSize)
                .memoryCacheKeyExtra("size_cache", targetSize.toString())
                .precision(Precision.EXACT)
                .build()

            val imageJob = imageLoader.enqueue(request)
            imageCache.put(cacheKey, imageJob.job)

            imageJob.job
        }
    }

    private fun getImageRequestSize(page: PageMetadata): coil3.size.Size {
        val containerSize = screenScaleState.areaSize.value
        val zoomScale = screenScaleState.transformation.value.scale

        return when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                val width = ((containerSize.width - (sidePaddingPx.value * 2)) * zoomScale).roundToInt()
                val constrainedWidth = when {
                    page.size == null -> width.coerceAtMost(containerSize.width)
                    !readerState.imageStretchToFit.value -> width.coerceAtMost(page.size.width)
                    else -> width
                }

                coil3.size.Size(
                    width = Dimension(constrainedWidth),
                    height = Dimension.Undefined
                )
            }

            else -> {
                val height = ((containerSize.height - (sidePaddingPx.value * 2)) * zoomScale).roundToInt()
                val constrainedHeight = when {
                    page.size == null -> height.coerceAtMost(containerSize.height)
                    !readerState.imageStretchToFit.value -> height.coerceAtMost(page.size.height)
                    else -> height
                }

                coil3.size.Size(
                    width = Dimension.Undefined,
                    height = Dimension(constrainedHeight)
                )
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun setOriginalSize(requestPage: PageMetadata): PageMetadata {
        val request = ImageRequest.Builder(imageLoaderContext)
            .data(requestPage)
            .size(coil3.size.Size.ORIGINAL)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .precision(Precision.EXACT)
            .build()

        val originalSize = when (val image = imageLoader.execute(request)) {
            is SuccessResult -> IntSize(image.image.width, image.image.height)
            is ErrorResult -> null
        }
        if (originalSize == null) return requestPage

        val updated = requestPage.copy(size = originalSize)

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

        return updated
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

        screenScaleState.setTargetSize(targetSize)
        sidePaddingPx.value = padding
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

    private data class ImageCacheKey(
        val bookId: KomgaBookId,
        val pageNumber: Int,
        val size: coil3.size.Size
    )

    data class BookPagesInterval(
        val book: KomgaBook,
        val pages: List<PageMetadata>
    )
}