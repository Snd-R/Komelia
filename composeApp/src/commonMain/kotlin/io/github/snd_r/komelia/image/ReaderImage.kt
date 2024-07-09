package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

interface ReaderImage : AutoCloseable {
    val width: Int
    val height: Int

    val painter: StateFlow<Painter>
    val error: StateFlow<Exception?>

    fun requestUpdate(
        displaySize: IntSize,
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
    )

    // TODO move to reader helper function
    fun getDisplaySizeFor(maxDisplaySize: IntSize): IntSize {
        val widthRatio = maxDisplaySize.width.toDouble() / width
        val heightRatio = maxDisplaySize.height.toDouble() / height
        val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)
        return IntSize(
            (width * displayScaleFactor).roundToInt(),
            (height * displayScaleFactor).roundToInt()
        )
    }
}

