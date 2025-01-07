package io.github.snd_r.komelia.color

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalUnsignedTypes::class)
class Histogram(interleaved: ByteArray) {
    val red: UByteArray?
    val green: UByteArray?
    val blue: UByteArray?
    val color: UByteArray?

    private val normalizedColorPath: Path?
    private val normalizedRedPath: Path?
    private val normalizedGreenPath: Path?
    private val normalizedBluePath: Path?

    init {
        var red: UByteArray? = null
        var green: UByteArray? = null
        var blue: UByteArray? = null
        var color: UByteArray? = null

        if (interleaved.size == 256) {
            color = interleaved.toUByteArray()
        } else if (interleaved.size == 1024) {
            red = UByteArray(256)
            green = UByteArray(256)
            blue = UByteArray(256)
            for (i in 0 until 256) {
                val index = 4 * i
                red[i] = interleaved[index].toUByte()
                green[i] = interleaved[index + 1].toUByte()
                blue[i] = interleaved[index + 2].toUByte()
            }

        }
        this.red = red
        this.green = green
        this.blue = blue
        this.color = color

        normalizedColorPath = color?.let { buildHistogramPath(it) }
        normalizedRedPath = red?.let { buildHistogramPath(it) }
        normalizedGreenPath = green?.let { buildHistogramPath(it) }
        normalizedBluePath = blue?.let { buildHistogramPath(it) }
    }

    fun getDrawPathFlow(canvasSize: StateFlow<Size>) = canvasSize.map {
        HistogramPaths(
            color = normalizedColorPath?.denormalizeToCanvas(it),
            red = normalizedRedPath?.denormalizeToCanvas(it),
            green = normalizedGreenPath?.denormalizeToCanvas(it),
            blue = normalizedBluePath?.denormalizeToCanvas(it),
        )
    }

    private fun buildHistogramPath(channel: UByteArray): Path {
        val path = Path()
        path.moveTo(0f, 0f)

        var x = 0f
        val xStep = 1.0f / 256
        for (value in channel) {
            if (value != UByte.MIN_VALUE) {
                val canvasY = value.toFloat() / 256
                val canvasX = x
                path.lineTo(canvasX, canvasY)
            }
            x += xStep
            path.moveTo(x, 0f)
        }
        return path
    }
}

data class HistogramPaths(
    val color: Path?,
    val red: Path?,
    val green: Path?,
    val blue: Path?
)