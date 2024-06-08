package io.github.snd_r.komelia.ui.reader.paged

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.size.Precision
import coil3.size.Size
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.reactivecircus.cache4k.Cache
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.Page
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.SpreadImageLoadJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.math.max
import kotlin.math.roundToInt

typealias SpreadHash = Int

private val logger = KotlinLogging.logger("PagedReaderImageLoader")

class PagedReaderImageLoader(
    private val imageLoader: ImageLoader,
    private val imageLoaderContext: PlatformContext,
) {
    private val imageLoadJobs = Cache.Builder<SpreadHash, Deferred<SpreadImageLoadJob>>()
        .maximumCacheSize(3)
        .build()

    fun launchImageLoadJob(
        scope: CoroutineScope,
        loadPages: List<PageMetadata>,
        containerSize: IntSize,
        layout: PageDisplayLayout,
        scaleType: LayoutScaleType,
        stretchToFit: Boolean
    ): Deferred<SpreadImageLoadJob> {
        val currentHash = getLoadJobHash(loadPages, containerSize, layout, scaleType, stretchToFit)
        val cachedJob = imageLoadJobs.get(currentHash)
        if (cachedJob != null && !cachedJob.isCancelled) {
            return cachedJob
        }

        val job = scope.async {

            val spread = if (loadPages.any { it.size == null }) {
                logger.warn { "Page spread ${loadPages.map { it.pageNumber }} doesn't have calculated dimensions. Will have to decode at original image size" }
                getOriginalImageSizes(loadPages)
            } else loadPages

            val scale = calculateScreenScale(
                pages = spread,
                areaSize = containerSize,
                maxPageSize = getMaxPageSize(spread, containerSize),
                scaleType = scaleType,
                stretchToFit = stretchToFit
            )
            val spreadScaleFactor = scale.transformation.value.scale

            val scaledPageSizes = spread.map {
                scaledContentSizeForPage(
                    page = it,
                    scaleType = scaleType,
                    allowStretch = stretchToFit,
                    spread = spread,
                    containerSize = containerSize,
                    scaleFactor = spreadScaleFactor
                )
            }

            val imageJobs = mutableListOf<Pair<PageMetadata, Deferred<ImageResult>>>()
            for ((index, page) in spread.withIndex()) {
                val maxPageSize = scaledPageSizes[index]
                val request = ImageRequest.Builder(imageLoaderContext)
                    .data(page)
                    .size(
                        if (scaleType == LayoutScaleType.ORIGINAL) Size.ORIGINAL
                        else Size(width = maxPageSize.width, height = maxPageSize.height)
                    )
                    .memoryCacheKeyExtra("size_cache", maxPageSize.toString())
                    .precision(Precision.EXACT)
                    .build()

                logger.info { "Load request for page $page; zoom factor: $spreadScaleFactor; target size $maxPageSize" }
                imageJobs.add(page to scope.async { imageLoader.execute(request) })
            }
            val images = imageJobs.map { (page, image) -> Page(page, image.await(), spreadScaleFactor) }
            SpreadImageLoadJob(images, scale)
        }
        imageLoadJobs.put(currentHash, job)
        return job
    }

    private fun calculateScreenScale(
        pages: List<PageMetadata>,
        areaSize: IntSize,
        maxPageSize: IntSize,
        scaleType: LayoutScaleType,
        stretchToFit: Boolean,
    ): ScreenScaleState {
        val scaleState = ScreenScaleState()
        scaleState.setAreaSize(areaSize)
        val fitToScreenSize = pages
            .map { it.contentSizeForArea(maxPageSize) }
            .reduce { total, current ->
                IntSize(
                    width = (total.width + current.width),
                    height = max(total.height, current.height)
                )
            }
        scaleState.setTargetSize(fitToScreenSize.toSize())

        val actualSpreadSize = pages.mapNotNull { it.size }
            .fold(IntSize.Zero) { total, current ->
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

    private fun scaledContentSizeForPage(
        page: PageMetadata,
        scaleType: LayoutScaleType,
        allowStretch: Boolean,
        spread: List<PageMetadata>,
        containerSize: IntSize,
        scaleFactor: Float,
    ): IntSize {
        val availableContainerSize = getMaxPageSize(spread, containerSize)
        val constrainedSize = page.contentSizeForArea(availableContainerSize)
        val actualSize = page.size ?: constrainedSize

        val zoomedSize = IntSize(
            width = (constrainedSize.width * scaleFactor).roundToInt(),
            height = (constrainedSize.height * scaleFactor).roundToInt()
        )

        return if (scaleType == LayoutScaleType.ORIGINAL || !allowStretch) {
            zoomedSize.coerceAtMost(actualSize)
        } else {
            zoomedSize.coerceAtLeast(constrainedSize)
        }
    }

    private fun getLoadJobHash(
        pages: List<PageMetadata>,
        containerSize: IntSize,
        layout: PageDisplayLayout,
        scaleType: LayoutScaleType,
        stretchToFit: Boolean,
    ): Int {
        return arrayOf(
            pages,
            containerSize.width,
            containerSize.height,
            layout,
            scaleType,
            stretchToFit
        ).contentHashCode()
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun getOriginalImageSizes(
        pages: List<PageMetadata>,
    ): List<PageMetadata> {
        val spreadWithSizes = mutableListOf<PageMetadata>()
        for (page in pages) {
            if (page.size != null) {
                spreadWithSizes.add(page)
                continue
            }

            val request = ImageRequest.Builder(imageLoaderContext)
                .data(page)
                .size(Size.ORIGINAL)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .precision(Precision.EXACT)
                .build()

            val originalSize = when (val image = imageLoader.execute(request)) {
                is SuccessResult -> IntSize(image.image.width, image.image.height)
                is ErrorResult -> {
                    logger.error { "Failed to retrieve original image" }
                    null
                }
            }

            spreadWithSizes.add(page.copy(size = originalSize))
        }

        return spreadWithSizes
    }

    private fun getMaxPageSize(pages: List<PageMetadata>, containerSize: IntSize): IntSize {
        return IntSize(
            width = containerSize.width / pages.size,
            height = containerSize.height
        )
    }


    suspend fun loadScaledPages(
        pages: List<PageMetadata>,
        containerSize: IntSize,
        zoomFactor: Float,
        scaleType: LayoutScaleType,
        stretchToFit: Boolean
    ): List<PageImage> {
        val resampledPages = mutableListOf<PageImage>()
        for (page in pages) {
            val maxSize = scaledContentSizeForPage(
                page = page,
                scaleType = scaleType,
                allowStretch = stretchToFit,
                spread = pages,
                containerSize = containerSize,
                scaleFactor = zoomFactor
            )

            logger.info { "Resample request: container size: $containerSize; page size ${page.size}; zoom factor: $zoomFactor target size $maxSize" }

            val request = ImageRequest.Builder(imageLoaderContext)
                .data(page)
                .size(width = maxSize.width, height = maxSize.height)
                .memoryCacheKeyExtra("size_cache", maxSize.toString())
                .precision(Precision.EXACT)
                .build()

            val newImage = imageLoader.execute(request)
            resampledPages.add(PageImage(page, newImage))
        }

        return resampledPages
    }

    fun clearCache() {
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
        imageLoadJobs.invalidateAll()
    }
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


data class PageImage(
    val page: PageMetadata,
    val image: ImageResult
)