package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.StateFlow

interface ReaderImage : AutoCloseable {
    val width: Int
    val height: Int

    val currentSize: StateFlow<IntSize?>
    val painter: StateFlow<Painter>
    val error: StateFlow<Exception?>

    fun requestUpdate(
        displaySize: IntSize,
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
    )
}

