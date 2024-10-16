package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toRect
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private const val tileThreshold1 = 2048 * 2048
private const val tileThreshold2 = 4096 * 4096
private const val tileThreshold3 = 5120 * 5120

expect class RenderImage

private val logger = KotlinLogging.logger {}

abstract class TilingReaderImage(
    private val encoded: ByteArray,
    private val processingPipeline: ImageProcessingPipeline,
    private val stretchImages: StateFlow<Boolean>,
    final override val pageId: PageId
) : ReaderImage {
    final override val painter = MutableStateFlow(noopPainter)
    final override val error = MutableStateFlow<Throwable?>(null)

    final override val originalSize = MutableStateFlow<IntSize?>(null)
    final override val displaySize = MutableStateFlow<IntSize?>(null)
    final override val currentSize = MutableStateFlow<IntSize?>(null)

    @Volatile
    protected var lastUpdateRequest: UpdateRequest? = null

    @Volatile
    protected var lastUsedScaleFactor: Double? = null

    private val imageAwaitScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    protected val processingScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + SupervisorJob())

    private val jobFlow = MutableSharedFlow<UpdateRequest>(1, 0, SUSPEND)
    private val tiles = MutableStateFlow<List<ReaderImageTile>>(emptyList())
    protected val image = MutableStateFlow<PlatformImage?>(null)

    data class UpdateRequest(
        val visibleDisplaySize: IntRect,
        val zoomFactor: Float,
        val maxDisplaySize: IntSize,
    )

    init {
        jobFlow.conflate()
            .onEach { request ->
                try {
                    doUpdate(request)
                    this.error.value = null
                } catch (e: Throwable) {
                    currentCoroutineContext().ensureActive()
                    logger.catching(e)
                    this.error.value = e
                }

                delay(100)
            }.launchIn(processingScope)

        processingPipeline.changeFlow.onEach {
            image.value?.close()
            image.value = null
            originalSize.value = null
            currentSize.value = null
            loadImage()
            reloadLastRequest()
        }.launchIn(processingScope)

        stretchImages.drop(1).onEach { reloadLastRequest() }.launchIn(processingScope)

        processingScope.launch { loadImage() }
    }

    protected suspend fun reloadLastRequest() {
        lastUpdateRequest?.let { lastRequest ->
            lastUsedScaleFactor = null
            jobFlow.emit(lastRequest)
        }
    }

    private suspend fun loadImage() {
        try {
            measureTime {
                val processed = processingPipeline.process(pageId, decode(encoded))
                image.value = processed
                originalSize.value = IntSize(processed.width, processed.height)
            }.also { logger.info { "page ${pageId.pageNumber} completed load in $it" } }
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            logger.catching(e)
            this.error.value = e
            originalSize.value = IntSize(100, 100)
            imageAwaitScope.coroutineContext.cancelChildren()
        }
    }

    override suspend fun getOriginalSize(): IntSize {
        return imageAwaitScope.async { originalSize.filterNotNull().first() }.await()
    }

    private suspend fun getCurrentImage(): PlatformImage {
        return imageAwaitScope.async { image.filterNotNull().first() }.await()
    }

    override fun requestUpdate(
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
        maxDisplaySize: IntSize,
    ) {
        processingScope.launch {
            jobFlow.emit(
                UpdateRequest(
                    visibleDisplaySize = visibleDisplaySize,
                    zoomFactor = zoomFactor,
                    maxDisplaySize = maxDisplaySize
                )
            )
        }
    }

    private suspend fun doUpdate(request: UpdateRequest) {
        lastUpdateRequest = request

        val image = getCurrentImage()
        val displaySize = calculateSizeForArea(request.maxDisplaySize, stretchImages.value)
        val widthRatio = displaySize.width.toDouble() / image.width
        val heightRatio = displaySize.height.toDouble() / image.height
        val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)

        val zoomFactor = request.zoomFactor
        val visibleDisplaySize = request.visibleDisplaySize

        val actualScaleFactor = displayScaleFactor * zoomFactor

        val dstWidth = displaySize.width * zoomFactor
        val dstHeight = displaySize.height * zoomFactor

        this.displaySize.value = displaySize
        this.currentSize.value = IntSize(dstWidth.roundToInt(), dstHeight.roundToInt())

        val displayPixCount = (dstWidth * dstHeight).roundToInt()
        val tileSize = when (displayPixCount) {
            in 0..tileThreshold1 -> null
            in tileThreshold1..tileThreshold2 -> 1024
            in tileThreshold2..tileThreshold3 -> 512
            else -> 256
        }

        if (tileSize == null) {
            doFullResize(
                image = image,
                scaleFactor = actualScaleFactor,
                displayScaleFactor = displayScaleFactor,
                displayArea = displaySize
            )
        } else {
            doTile(
                image = image,
                displayRegion = visibleDisplaySize.toRect(),
                displayScaleFactor = displayScaleFactor,
                scaleFactor = actualScaleFactor,
                displayArea = displaySize,
                tileSize = tileSize
            )
        }
    }

    private suspend fun doFullResize(
        image: PlatformImage,
        scaleFactor: Double,
        displayScaleFactor: Double,
        displayArea: IntSize
    ) {
        if (lastUsedScaleFactor == scaleFactor) return
        if (tiles.value.isEmpty()) {
            painter.value = createPlaceholderPainter(displayArea)
        }
        lastUsedScaleFactor = scaleFactor
        val dstWidth = (image.width * scaleFactor).roundToInt()
        val dstHeight = (image.height * scaleFactor).roundToInt()

        measureTime {
            val resizedImage = resizeImage(
                image,
                dstWidth,
                dstHeight,
            )
            val previousTiles = tiles.value
            tiles.value = listOf(
                ReaderImageTile(
                    size = IntSize(resizedImage.width, resizedImage.height),
                    displayRegion = Rect(
                        0f,
                        0f,
                        round(image.width * displayScaleFactor).toFloat(),
                        round(image.height * displayScaleFactor).toFloat()
                    ),
                    isVisible = true,
                    renderImage = resizedImage.renderImage
                )
            )
            closeTileBitmaps(previousTiles)
            painter.value = createTilePainter(
                tiles = tiles.value,
                displaySize = displayArea,
                scaleFactor = scaleFactor
            )
        }.also { logger.info { "page ${pageId.pageNumber} completed full resize to $dstWidth x $dstHeight in $it" } }

    }

    private suspend fun doTile(
        image: PlatformImage,
        displayRegion: Rect,
        displayScaleFactor: Double,
        scaleFactor: Double,
        displayArea: IntSize,
        tileSize: Int,
    ) {
        val timeSource = TimeSource.Monotonic
        val start = timeSource.markNow()
        if (tiles.value.isEmpty()) {
            painter.value = createPlaceholderPainter(displayArea)
        }

        val visibilityWindow = Rect(
            left = displayRegion.left / 1.5f,
            top = displayRegion.top / 1.5f,
            right = displayRegion.right * 1.5f,
            bottom = displayRegion.bottom * 1.5f
        )

        val oldTiles = tiles.value
        val newTiles = mutableListOf<ReaderImageTile>()
        val unusedTiles = mutableListOf<ReaderImageTile>()
        var addedNewTiles = false

        var yTaken = 0
        while (yTaken != image.height) {
            var xTaken = 0
            while (xTaken != image.width) {
                val tileRegion = IntRect(
                    top = yTaken.coerceAtMost(image.height),
                    bottom = (yTaken + tileSize).coerceAtMost(image.height),
                    left = (xTaken).coerceAtMost(image.width),
                    right = (xTaken + tileSize).coerceAtMost(image.width),
                )
                val tileDisplayRegion = Rect(
                    (tileRegion.left * displayScaleFactor).toFloat(),
                    (tileRegion.top * displayScaleFactor).toFloat(),
                    (tileRegion.right * displayScaleFactor).toFloat(),
                    (tileRegion.bottom * displayScaleFactor).toFloat()
                )

                val existingTile = oldTiles.find { it.displayRegion == tileDisplayRegion }
                if (!visibilityWindow.overlaps(tileDisplayRegion)) {
                    xTaken = (xTaken + tileSize).coerceAtMost(image.width)
                    existingTile?.let { unusedTiles.add(it) }
                    continue
                }

                if (existingTile != null) {
                    if (scaleFactor == lastUsedScaleFactor && existingTile.renderImage != null) {
                        newTiles.add(existingTile)
                        xTaken = (xTaken + tileSize).coerceAtMost(image.width)
                        continue
                    } else {
                        unusedTiles.add(existingTile)
                    }
                }

                val tileWidth = tileRegion.right - tileRegion.left
                val tileHeight = tileRegion.bottom - tileRegion.top
                val scaledTileData = measureTimedValue {
                    getImageRegion(
                        image,
                        tileRegion,
                        ((tileWidth) * scaleFactor).roundToInt(),
                        ((tileHeight) * scaleFactor).roundToInt()
                    )
                }

                val tile = ReaderImageTile(
                    size = IntSize(scaledTileData.value.width, scaledTileData.value.height),
                    displayRegion = tileDisplayRegion,
                    isVisible = true,
                    renderImage = scaledTileData.value.renderImage
                )

                newTiles.add(tile)
                addedNewTiles = true
                xTaken = (xTaken + tileSize).coerceAtMost(image.width)
            }
            yTaken = (yTaken + tileSize).coerceAtMost(image.height)
        }

        if (addedNewTiles) {
            tiles.value = newTiles
            painter.value = createTilePainter(tiles.value, displayArea, scaleFactor)
            closeTileBitmaps(unusedTiles)

            val end = timeSource.markNow()
            logger.info { "page ${pageId.pageNumber} completed tiled resize in ${end - start};  ${tiles.value.size} tiles" }
        }
        lastUsedScaleFactor = scaleFactor

    }

    override fun close() {
        closeTileBitmaps(tiles.value)
        image.value?.close()
        processingScope.cancel()
        imageAwaitScope.cancel()
    }

    protected abstract fun getDimensions(encoded: ByteArray): IntSize
    protected abstract fun decode(encoded: ByteArray): PlatformImage

    protected abstract fun closeTileBitmaps(tiles: List<ReaderImageTile>)
    protected abstract fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): Painter

    protected abstract fun createPlaceholderPainter(displaySize: IntSize): Painter

    protected abstract suspend fun resizeImage(
        image: PlatformImage,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData

    protected abstract suspend fun getImageRegion(
        image: PlatformImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData

    data class ReaderImageTile(
        val size: IntSize,
        val displayRegion: Rect,
        val isVisible: Boolean,
        val renderImage: RenderImage?,
    )

    data class ReaderImageData(
        val width: Int,
        val height: Int,
        val renderImage: RenderImage,
    )
}
