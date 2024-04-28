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

private val logger = KotlinLogging.logger {}

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
        allowUpsample: Boolean
    ): Deferred<SpreadImageLoadJob> {
        val currentHash = getLoadJobHash(loadPages, containerSize, layout, scaleType, allowUpsample)
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
                scaleType = scaleType
            )
            val spreadZoomFactor = scale.transformation.value.scale

            val scaledPageSizes = spread.map {
                scaledContentSizeForPage(
                    page = it,
                    scaleType = scaleType,
                    allowUpsample = allowUpsample,
                    spread = spread,
                    containerSize = containerSize,
                    zoomFactor = spreadZoomFactor
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
                    .memoryCacheKeyExtra("size_cache", coilCacheKeyFor(maxPageSize, allowUpsample))
                    .precision(Precision.EXACT)
                    .build()

                logger.info { "Load request for page $page; zoom factor: $spreadZoomFactor;  size $maxPageSize" }
                imageJobs.add(page to scope.async { imageLoader.execute(request) })
            }
            val images = imageJobs.map { (page, image) -> Page(page, image.await(), spreadZoomFactor) }
            SpreadImageLoadJob(images, scale)
        }
        imageLoadJobs.put(currentHash, job)
        return job
    }

    private fun calculateScreenScale(
        pages: List<PageMetadata>,
        areaSize: IntSize,
        maxPageSize: IntSize,
        scaleType: LayoutScaleType
    ): ScreenScaleState {
        val scaleState = ScreenScaleState()
        scaleState.setAreaSize(areaSize)
        val constrainedContentSize = pages
            .map { it.contentSizeForArea(maxPageSize) }
            .fold(IntSize.Zero) { total, current ->
                IntSize(
                    width = (total.width + current.width),
                    height = max(total.height, current.height)
                )
            }

        scaleState.setTargetSize(
            targetSize = if (constrainedContentSize == IntSize.Zero) androidx.compose.ui.geometry.Size.Unspecified else constrainedContentSize.toSize()
        )
        when (scaleType) {
            LayoutScaleType.SCREEN -> scaleState.setZoom(0f)
            LayoutScaleType.FIT_WIDTH -> {
                if (constrainedContentSize.width < areaSize.width) scaleState.setZoom(1f)
                else scaleState.setZoom(0f)
            }

            LayoutScaleType.FIT_HEIGHT -> {
                if (constrainedContentSize.height < areaSize.height) scaleState.setZoom(1f)
                else scaleState.setZoom(0f)
            }

            LayoutScaleType.ORIGINAL -> {
                val actualPageSize = pages.mapNotNull { it.size }.fold(IntSize.Zero) { total, current ->
                    IntSize((total.width + current.width), max(total.height, current.height))
                }

                if (actualPageSize.width > areaSize.width || actualPageSize.height > areaSize.height) {
                    val newZoom = max(
                        actualPageSize.width.toFloat() / constrainedContentSize.width,
                        actualPageSize.height.toFloat() / constrainedContentSize.height
                    ) / scaleState.scaleFor100PercentZoom()

                    scaleState.setZoom(newZoom)

                } else scaleState.setZoom(0f)
            }
        }

        return scaleState
    }

    private fun getLoadJobHash(
        pages: List<PageMetadata>,
        containerSize: IntSize,
        layout: PageDisplayLayout,
        scaleType: LayoutScaleType,
        allowUpsample: Boolean
    ): Int {
        return arrayOf(
            pages,
            containerSize.width,
            containerSize.height,
            layout,
            scaleType,
            allowUpsample
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

    private fun scaledContentSizeForPage(
        page: PageMetadata,
        scaleType: LayoutScaleType,
        allowUpsample: Boolean,
        spread: List<PageMetadata>,
        containerSize: IntSize,
        zoomFactor: Float,
    ): IntSize {
        val availableContainerSize = getMaxPageSize(spread, containerSize)
        val constrainedPageSize = page.contentSizeForArea(availableContainerSize)
        val actualPageSize = page.size ?: constrainedPageSize

        var zoomedSize = IntSize(
            width = (constrainedPageSize.width * zoomFactor).roundToInt(),
            height = (constrainedPageSize.height * zoomFactor).roundToInt()
        )

        if (scaleType == LayoutScaleType.ORIGINAL) {
            zoomedSize = zoomedSize.coerceAtMost(actualPageSize)
        } else {

            // do not resample if size is bigger than image original dimensions
            if (!allowUpsample && (zoomedSize.height > actualPageSize.height || zoomedSize.width > actualPageSize.width)) {
                zoomedSize = zoomedSize.coerceAtMost(actualPageSize)
            }

            // at least scale to the size of the screen
            if (constrainedPageSize.height > actualPageSize.height || constrainedPageSize.width > actualPageSize.width) {
                zoomedSize = zoomedSize.coerceAtLeast(constrainedPageSize)
            }
        }

        return zoomedSize
    }

    suspend fun loadScaledPages(
        pages: List<PageMetadata>,
        containerSize: IntSize,
        zoomFactor: Float,
        scaleType: LayoutScaleType,
        allowUpsample: Boolean
    ): List<PageImage> {
        val resampledPages = mutableListOf<PageImage>()
        for (page in pages) {
            val maxSize = scaledContentSizeForPage(
                page,
                scaleType,
                allowUpsample,
                pages,
                containerSize,
                zoomFactor
            )

            logger.info { "Resample request: container size: $containerSize; page size ${page.size}; zoom factor: $zoomFactor target size $maxSize" }

            val request = ImageRequest.Builder(imageLoaderContext)
                .data(page)
                .size(width = maxSize.width, height = maxSize.height)
                .memoryCacheKeyExtra("size_cache", coilCacheKeyFor(maxSize, allowUpsample))
                .precision(Precision.EXACT)
                .build()

            val newImage = imageLoader.execute(request)
            resampledPages.add(PageImage(page, newImage))
        }

        return resampledPages
    }

    private fun coilCacheKeyFor(pageMaxSize: IntSize, allowUpsample: Boolean): String {
        return "${pageMaxSize}_upsampled-${allowUpsample}"
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