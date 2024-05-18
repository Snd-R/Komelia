package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
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
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.*
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

private val logger = KotlinLogging.logger {}

class ContinuousReaderState(
    private val imageLoader: ImageLoader,
    private val imageLoaderContext: PlatformContext,
    private val readerState: ReaderState,
    private val settingsRepository: ReaderSettingsRepository,
    val screenScaleState: ScreenScaleState,
) {
    private val stateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val imageLoadScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    val lazyListState = LazyListState(0, 0)

    val sidePaddingFraction = MutableStateFlow(.3f)
    val sidePaddingPx = MutableStateFlow(0)
    val pageSpacing = MutableStateFlow(0)
    val pages = MutableStateFlow<List<PageMetadata>>(emptyList())
    val readingDirection = MutableStateFlow(TOP_TO_BOTTOM)

    val currentPageIndex = readerState.readProgressPage.map { it - 1 }
    val allowUpsample = readerState.allowUpsample.asStateFlow()

    private val imageCache = Cache.Builder<ImageCacheKey, Deferred<ImageResult>>()
        .maximumCacheSize(10)
        .build()

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

        readerState.bookState
            .filterNotNull()
            .onEach { book -> pages.value = book.bookPages }
            .launchIn(stateScope)

        screenScaleState.areaSize
            .filter { it != IntSize.Zero }
            .onEach {
                applyPadding()
                screenScaleState.setZoom(0f)
            }
            .launchIn(stateScope)
    }

    fun stop() {
        stateScope.coroutineContext.cancelChildren()
        imageLoadScope.coroutineContext.cancelChildren()
        imageCache.invalidateAll()
    }

    fun getContentSizePx(page: PageMetadata): IntSize {
        val containerSize = screenScaleState.areaSize.value

        val constrained = when (readingDirection.value) {
            TOP_TO_BOTTOM -> {
                val constrainedWidth = containerSize.width - (sidePaddingPx.value * 2)
                when {
                    page.size == null -> {
                        val previousPage = pages.value.getOrNull(page.pageNumber - 2)
                        val nextPage = pages.value.getOrNull(page.pageNumber)
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

                    else -> contentSizeForArea(
                        contentSize = page.size,
                        maxPageSize = IntSize(constrainedWidth, Int.MAX_VALUE)
                    )
                }
            }

            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> {
                val constrainedHeight = containerSize.height - (sidePaddingPx.value * 2)
                val contentSize = when {
                    page.size == null -> {
                        val previousPage = pages.value.getOrNull(page.pageNumber - 2)
                        val nextPage = pages.value.getOrNull(page.pageNumber)
                        previousPage?.size ?: nextPage?.size
                        ?: IntSize((containerSize.width / 2), containerSize.height)
                    }

                    else -> page.size
                }
                contentSizeForArea(
                    contentSize = contentSize,
                    maxPageSize = IntSize(Int.MAX_VALUE, constrainedHeight)
                )

                when {
                    page.size == null -> {
                        val previousPage = pages.value.getOrNull(page.pageNumber - 2)
                        val nextPage = pages.value.getOrNull(page.pageNumber)
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

        val nextPage = pages.value.getOrNull(requestPage.pageNumber)
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

            logger.info { "image request $page; target size $targetSize" }
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
                    readerState.allowUpsample.value -> width
                    else -> width.coerceAtMost(page.size.width)
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
                    readerState.allowUpsample.value -> height
                    else -> height.coerceAtMost(page.size.height)
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

        pages.update { pages ->
            pages.getOrNull(updated.pageNumber - 1) ?: return@update pages

            pages.toMutableList().apply {
                set(updated.pageNumber - 1, updated)
            }
        }

        return updated
    }

    fun onPageIndexChange(pageIndex: Int) {
        readerState.onProgressChange(pageIndex + 1)
    }

    suspend fun scrollToPage(pageNumber: Int) {
        lazyListState.scrollToItem(pageNumber - 1)
    }

    suspend fun scrollToNextPage() {
        val currentIndex = currentPageIndex.first()
        val nextPageNumber = currentIndex + 2
        if (nextPageNumber > pages.value.size) {
            readerState.loadNextBook()
        } else {

            val scrollAmount = when (readingDirection.value) {
                TOP_TO_BOTTOM -> screenScaleState.areaSize.value.height
                LEFT_TO_RIGHT, RIGHT_TO_LEFT -> screenScaleState.areaSize.value.width
            }
            lazyListState.animateScrollBy(scrollAmount.toFloat())
        }
    }

    suspend fun scrollToPreviousPage() {
        val currentIndex = currentPageIndex.first()
        if (currentIndex == 0) {
            readerState.loadPreviousBook()
        } else {
            val scrollAmount = when (readingDirection.value) {
                TOP_TO_BOTTOM -> screenScaleState.areaSize.value.height
                LEFT_TO_RIGHT, RIGHT_TO_LEFT -> screenScaleState.areaSize.value.width
            }
            lazyListState.animateScrollBy(-scrollAmount.toFloat())
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
}