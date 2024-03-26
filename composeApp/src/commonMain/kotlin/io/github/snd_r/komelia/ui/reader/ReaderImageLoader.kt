package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.unit.IntSize
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.*
import kotlin.math.roundToInt

typealias SpreadHash = Int

private val logger = KotlinLogging.logger {}

class ReaderImageLoader(
    private val imageLoader: ImageLoader,
    private val imageLoaderContext: PlatformContext,
) {
    private val imageLoadJobs = Cache.Builder<SpreadHash, SpreadImageLoadJob>()
        .maximumCacheSize(6)
        .build()

    fun launchImageLoadJob(
        scope: CoroutineScope,
        loadSpread: List<PageMetadata>,
        containerSize: IntSize,
        layout: PageDisplayLayout,
        scaleType: LayoutScaleType,
        allowUpsample: Boolean
    ): SpreadImageLoadJob {
        val currentHash = getLoadJobHash(loadSpread, containerSize, layout, scaleType, allowUpsample)
        val cachedJob = imageLoadJobs.get(currentHash)
        if (cachedJob != null && !cachedJob.pageJob.isCancelled) {
            return cachedJob
        }

        val job = scope.async {

            val spread = if (loadSpread.any { it.size == null }) {
                logger.warn { "Page spread ${loadSpread.map { it.pageNumber }} doesn't have calculated dimensions. Will have to decode at original image size" }
                getOriginalImageSizes(loadSpread)
            } else loadSpread

            val spreadScale = PageSpreadScaleState()
            spreadScale.limitPagesInsideArea(
                pages = spread,
                areaSize = containerSize,
                maxPageSize = getMaxPageSize(spread, containerSize),
                scaleType = scaleType
            )
            val spreadZoomFactor = spreadScale.transformation.value.scale

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
            imageJobs.map { (page, image) -> Page(page, image.await()) }
        }
        val spreadLoadJob = SpreadImageLoadJob(job, currentHash)
        imageLoadJobs.put(currentHash, spreadLoadJob)
        return spreadLoadJob
    }

    private fun getLoadJobHash(
        pages: List<PageMetadata>,
        containerSize: IntSize,
        layout: PageDisplayLayout,
        scaleType: LayoutScaleType,
        allowUpsample: Boolean
    ): Int {
        return Objects.hash(pages, containerSize, layout, scaleType, allowUpsample)
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun getOriginalImageSizes(
        spread: List<PageMetadata>,
    ): List<PageMetadata> {
        val spreadWithSizes = mutableListOf<PageMetadata>()
        for (page in spread) {
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
    ): List<Page> {
        val resampledPages = mutableListOf<Page>()
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
            resampledPages.add(Page(page, newImage))
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
