package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toRect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val tileThreshold = 2048 * 2048
private const val tileSize = 1024

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Bitmap

abstract class TilingReaderImage(private val encoded: ByteArray) : ReaderImage {
    private val dimensions by lazy { getDimensions(encoded) }
    final override val width by lazy { dimensions.width }
    final override val height by lazy { dimensions.height }
    final override val painter by lazy { MutableStateFlow(createPlaceholderPainter(IntSize(width, height))) }

    private val tiles = MutableStateFlow<List<ReaderImageTile>>(emptyList())
    private var currentScaleFactor: Double? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val tileScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + SupervisorJob())
    private val jobFlow = MutableSharedFlow<UpdateRequest>(1, 0, SUSPEND)

    init {
        jobFlow.conflate()
            .onEach {
                doUpdate(
                    it.displayArea,
                    it.visibleDisplayArea,
                    it.zoomFactor
                )
                delay(100)
            }.launchIn(tileScope)
    }

    data class UpdateRequest(
        val displayArea: IntSize,
        val visibleDisplayArea: IntRect,
        val zoomFactor: Float,
    )

    override fun updateState(
        viewportSize: IntSize,
        visibleViewportArea: IntRect,
        zoomFactor: Float,
    ) {
        tileScope.launch { jobFlow.emit(UpdateRequest(viewportSize, visibleViewportArea, zoomFactor)) }
    }

    private fun doUpdate(
        displaySize: IntSize,
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
    ) {
        val widthRatio = displaySize.width.toDouble() / width
        val heightRatio = displaySize.height.toDouble() / height
        val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)
        val newScaleFactor = displayScaleFactor * zoomFactor

        val displayPixCount = (width * height * newScaleFactor).roundToInt()
        if (displayPixCount > tileThreshold) {
            doTile(
                visibleDisplaySize.toRect(),
                displayScaleFactor,
                newScaleFactor,
                displaySize
            )
        } else {
            doFullResize(newScaleFactor, displayScaleFactor, displaySize)
        }
    }

    private fun doFullResize(scaleFactor: Double, displayScaleFactor: Double, displayArea: IntSize) {
        if (currentScaleFactor == scaleFactor) return
        currentScaleFactor = scaleFactor

        val image = decode(encoded)
        val resizedImage = image.resize(
            (width * scaleFactor).roundToInt(),
            (height * scaleFactor).roundToInt(),
        )
        val previousTiles = tiles.value
        tiles.value = listOf(
            ReaderImageTile(
                originalSize = IntSize(resizedImage.width, resizedImage.height),
                displayRegion = Rect(
                    0f,
                    0f,
                    (width * displayScaleFactor).toFloat(),
                    (height * displayScaleFactor).toFloat()
                ),
                isVisible = true,
                bitmap = resizedImage.bitmap
            )
        )
        closeTileBitmaps(previousTiles)
        painter.value = createTilePainter(tiles.value, displayArea)
        image.close()
    }

    private fun doTile(
        displayRegion: Rect,
        displayScaleFactor: Double,
        scaleFactor: Double,
        displayArea: IntSize
    ) {
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
                    if (scaleFactor == currentScaleFactor && existingTile.bitmap != null) {
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
                val scaledTileData = image.getRegion(
                    tileRegion,
                    ((tileWidth) * scaleFactor).roundToInt(),
                    ((tileHeight) * scaleFactor).roundToInt()
                )

                val tile = ReaderImageTile(
                    originalSize = IntSize(scaledTileData.width, scaledTileData.height),
                    displayRegion = tileDisplayRegion,
                    isVisible = true,
                    bitmap = scaledTileData.bitmap
                )

                newTiles.add(tile)
                addedNewTiles = true
                xTaken = (xTaken + tileSize).coerceAtMost(width)
            }
            yTaken = (yTaken + tileSize).coerceAtMost(height)
        }

        if (addedNewTiles) {
            tiles.value = newTiles
            painter.value = createTilePainter(tiles.value, displayArea)
            closeTileBitmaps(unusedTiles)
        }
        currentScaleFactor = scaleFactor
        image?.close()
    }

    override fun close() {
        closeTileBitmaps(tiles.value)
    }

    protected abstract fun getDimensions(encoded: ByteArray): IntSize
    protected abstract fun decode(encoded: ByteArray): PlatformImage

    protected abstract fun closeTileBitmaps(tiles: List<ReaderImageTile>)
    protected abstract fun createTilePainter(tiles: List<ReaderImageTile>, displaySize: IntSize): Painter
    protected abstract fun createPlaceholderPainter(displaySize: IntSize): Painter

    interface PlatformImage : AutoCloseable {
        fun resize(scaleWidth: Int, scaleHeight: Int): ReaderImageData
        fun getRegion(rect: IntRect, scaleWidth: Int, scaleHeight: Int): ReaderImageData
    }

    data class ReaderImageTile(
        val originalSize: IntSize,
        val displayRegion: Rect,
        val isVisible: Boolean,
        val bitmap: Bitmap?,
    )
}
