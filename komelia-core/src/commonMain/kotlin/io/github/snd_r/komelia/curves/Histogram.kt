package io.github.snd_r.komelia.curves

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

@OptIn(ExperimentalUnsignedTypes::class)
class Histogram(val channels: List<LookupTable>) {
    private val normalizedPath: Path
    private val channelSize = channels.first().values.size

    private var previousCanvasSize: Size = Size.Zero
    private var cachedCanvasPath: Path = Path()

    init {
        normalizedPath = buildHistogramPath()
    }

    private fun buildHistogramPath(): Path {
        val path = Path()
        path.moveTo(0f, 0f)

        var x = 0f
        val xStep = 1.0f / channelSize
        for (value in channels.first().values) {
            if (value != UByte.MIN_VALUE) {
                val canvasY = value.toFloat() / channelSize
                val canvasX = x
                path.lineTo(canvasX, canvasY)
            }
            x += xStep
            path.moveTo(x, 0f)
        }
        return path
    }

    fun getCanvasPath(canvasSize: Size): Path {
        return if (previousCanvasSize == canvasSize) {
            cachedCanvasPath
        } else {
            normalizedPath.denormalize(canvasSize).also {
                previousCanvasSize = canvasSize
                cachedCanvasPath = it
            }
        }
    }
}