package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

interface ReaderImage : AutoCloseable {
    val pageId: PageId
    val originalSize: StateFlow<IntSize>
    val displaySize: StateFlow<IntSize?>
    val currentSize: StateFlow<IntSize?>
    val painter: StateFlow<Painter>
    val error: StateFlow<Throwable?>

    suspend fun getOriginalSize(): IntSize
    fun calculateSizeForArea(maxDisplaySize: IntSize, allowUpscale: Boolean): IntSize {
        val imageSize = this.originalSize.value
        if (imageSize == IntSize.Zero) return maxDisplaySize
        val widthRatio = maxDisplaySize.width.toDouble() / imageSize.width
        val heightRatio = maxDisplaySize.height.toDouble() / imageSize.height
        val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)

        val displayWidth = (imageSize.width * displayScaleFactor).roundToInt()
        val displayHeight = (imageSize.height * displayScaleFactor).roundToInt()

        val size = if (allowUpscale) IntSize(displayWidth, displayHeight)
        else IntSize(
            displayWidth.coerceAtMost(imageSize.width),
            displayHeight.coerceAtMost(imageSize.height)
        )
        return size
    }

    fun requestUpdate(
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
        maxDisplaySize: IntSize,
    )

    data class PageId(
        val bookId: String,
        val pageNumber: Int
    ) {
        override fun toString(): String {
            return "${bookId}_$pageNumber"
        }
    }
}

val noopPainter = object : Painter() {
    override val intrinsicSize: Size = Size.Zero
    override fun DrawScope.onDraw() {}
}
