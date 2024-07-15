package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toRect
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
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
expect class PlatformImage

private val logger = KotlinLogging.logger {}

abstract class TilingReaderImage(private val encoded: ByteArray) : ReaderImage {
    private val dimensions by lazy { getDimensions(encoded) }
    final override val width by lazy { dimensions.width }
    final override val height by lazy { dimensions.height }
    final override val painter by lazy { MutableStateFlow(createPlaceholderPainter(IntSize(width, height))) }
    final override val error = MutableStateFlow<Exception?>(null)
    final override val currentSize = MutableStateFlow<IntSize?>(null)

    @Volatile
    protected var lastUpdateRequest: UpdateRequest? = null

    @Volatile
    protected var lastUsedScaleFactor: Double? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val imageScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + SupervisorJob())
    protected val jobFlow = MutableSharedFlow<UpdateRequest>(1, 0, SUSPEND)
    protected val tiles = MutableStateFlow<List<ReaderImageTile>>(emptyList())

    init {
        jobFlow.conflate()
            .onEach { request ->
                this.error.value = null
                try {
                    doUpdate(request)
                } catch (e: Exception) {
                    logger.catching(e)
                    this.error.value = e
                }

                delay(100)
            }.launchIn(imageScope)
    }

    data class UpdateRequest(
        val displaySize: IntSize,
        val visibleDisplaySize: IntRect,
        val zoomFactor: Float,
    )

    override fun requestUpdate(
        displaySize: IntSize,
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
    ) {
        imageScope.launch { jobFlow.emit(UpdateRequest(displaySize, visibleDisplaySize, zoomFactor)) }
    }

    private suspend fun doUpdate(request: UpdateRequest) {
        lastUpdateRequest = request

        val displaySize = request.displaySize
        val zoomFactor = request.zoomFactor
        val visibleDisplaySize = request.visibleDisplaySize

        val widthRatio = displaySize.width.toDouble() / width
        val heightRatio = displaySize.height.toDouble() / height
        val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)
        val actualScaleFactor = displayScaleFactor * zoomFactor

        val dstWidth = displaySize.width * zoomFactor
        val dstHeight = displaySize.height * zoomFactor
        val displayPixCount = (dstWidth * dstHeight).roundToInt()
        val tileSize = when (displayPixCount) {
            in 0..tileThreshold1 -> null
            in tileThreshold1..tileThreshold2 -> 1024
            in tileThreshold2..tileThreshold3 -> 512
            else -> 256
        }

        if (tileSize == null) {
            doFullResize(
                scaleFactor = actualScaleFactor,
                displayScaleFactor = displayScaleFactor,
                displayArea = displaySize
            )
        } else {
            doTile(
                displayRegion = visibleDisplaySize.toRect(),
                displayScaleFactor = displayScaleFactor,
                scaleFactor = actualScaleFactor,
                displayArea = displaySize,
                tileSize = tileSize
            )
        }
        currentSize.value = IntSize(dstWidth.roundToInt(), dstHeight.roundToInt())
    }

    private suspend fun doFullResize(
        scaleFactor: Double,
        displayScaleFactor: Double,
        displayArea: IntSize
    ) {
        if (lastUsedScaleFactor == scaleFactor) return
        lastUsedScaleFactor = scaleFactor
        val dstWidth = (width * scaleFactor).roundToInt()
        val dstHeight = (height * scaleFactor).roundToInt()

        measureTime {
            val image = decode(encoded)
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
                        round(width * displayScaleFactor).toFloat(),
                        round(height * displayScaleFactor).toFloat()
                    ),
                    isVisible = true,
                    renderImage = resizedImage.renderImage
                )
            )
            closeTileBitmaps(previousTiles)
            painter.value = createTilePainter(tiles.value, displayArea, scaleFactor)
            closeImage(image)
        }.also { logger.info { "image $width x $height completed full resize to $dstWidth x $dstHeight in $it" } }
    }

    private suspend fun doTile(
        displayRegion: Rect,
        displayScaleFactor: Double,
        scaleFactor: Double,
        displayArea: IntSize,
        tileSize: Int,
    ) {
        val timeSource = TimeSource.Monotonic
        val start = timeSource.markNow()

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
        var image: PlatformImage? = null

        var yTaken = 0
        while (yTaken != height) {
            var xTaken = 0
            while (xTaken != width) {
                val tileRegion = IntRect(
                    top = yTaken.coerceAtMost(height),
                    bottom = (yTaken + tileSize).coerceAtMost(height),
                    left = (xTaken).coerceAtMost(width),
                    right = (xTaken + tileSize).coerceAtMost(width),
                )
                val tileDisplayRegion = Rect(
                    (tileRegion.left * displayScaleFactor).toFloat(),
                    (tileRegion.top * displayScaleFactor).toFloat(),
                    (tileRegion.right * displayScaleFactor).toFloat(),
                    (tileRegion.bottom * displayScaleFactor).toFloat()
                )

                val existingTile = oldTiles.find { it.displayRegion == tileDisplayRegion }
                if (!visibilityWindow.overlaps(tileDisplayRegion)) {
                    xTaken = (xTaken + tileSize).coerceAtMost(width)
                    existingTile?.let { unusedTiles.add(it) }
                    continue
                }

                if (existingTile != null) {
                    if (scaleFactor == lastUsedScaleFactor && existingTile.renderImage != null) {
                        newTiles.add(existingTile)
                        xTaken = (xTaken + tileSize).coerceAtMost(width)
                        continue
                    } else {
                        unusedTiles.add(existingTile)
                    }
                }

                if (image == null) {
                    image = decode(encoded)
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
                logger.info { "retrived tile data in ${scaledTileData.duration}" }

                val tile = ReaderImageTile(
                    size = IntSize(scaledTileData.value.width, scaledTileData.value.height),
                    displayRegion = tileDisplayRegion,
                    isVisible = true,
                    renderImage = scaledTileData.value.renderImage
                )

                newTiles.add(tile)
                addedNewTiles = true
                xTaken = (xTaken + tileSize).coerceAtMost(width)
            }
            yTaken = (yTaken + tileSize).coerceAtMost(height)
        }

        val end = timeSource.markNow()
        if (addedNewTiles) {
            tiles.value = newTiles
            painter.value = createTilePainter(tiles.value, displayArea, scaleFactor)
            closeTileBitmaps(unusedTiles)

            val dstWidth = (width * scaleFactor).roundToInt()
            val dstHeight = (height * scaleFactor).roundToInt()
            logger.info { "image $width x $height; tile size:$tileSize; tiles count: ${tiles.value.size}; resize to $dstWidth x $dstHeight completed in ${end - start}" }
        }
        lastUsedScaleFactor = scaleFactor
        image?.let { closeImage(it) }
    }

    override fun close() {
        closeTileBitmaps(tiles.value)
        imageScope.cancel()
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

    protected abstract fun closeImage(image: PlatformImage)

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
