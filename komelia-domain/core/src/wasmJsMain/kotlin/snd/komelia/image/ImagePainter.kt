package snd.komelia.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import snd.komelia.image.TilingReaderImage.ReaderImageTile
import snd.komelia.image.TilingReaderImage.TiledPainter

class ImagePainter(
    private val image: Image,
    samplingMode: SamplingMode,
    scaleFactor: Double,
    displaySize: IntSize,
) : Painter() {
    override val intrinsicSize: Size = displaySize.toSize()
    private val samplingMode = if (scaleFactor > 1.0) samplingMode else SamplingMode.DEFAULT

    override fun DrawScope.onDraw() {
        drawContext.canvas.nativeCanvas.drawImageRect(
            image = image,
            src = Rect.makeWH(
                image.width.toFloat(),
                image.height.toFloat()
            ),
            dst = Rect(
                left = 0f,
                top = 0f,
                right = intrinsicSize.width,
                bottom = intrinsicSize.height
            ),
            samplingMode = samplingMode,
            paint = null,
            strict = true
        )
    }
}

class SkiaTiledPainter(
    private val tiles: List<ReaderImageTile>,
    private val upsamplingMode: UpsamplingMode,
    private val scaleFactor: Double,
    private val displaySize: IntSize,
    private val showDebugGrid: Boolean,
) : TiledPainter() {
    override val intrinsicSize: Size = displaySize.toSize()
    private val samplingMode = if (scaleFactor > 1.0) when (upsamplingMode) {
        UpsamplingMode.NEAREST -> SamplingMode.DEFAULT
        UpsamplingMode.BILINEAR -> SamplingMode.LINEAR
        UpsamplingMode.MITCHELL -> SamplingMode.MITCHELL
        UpsamplingMode.CATMULL_ROM -> SamplingMode.CATMULL_ROM
    } else SamplingMode.DEFAULT

    override fun DrawScope.onDraw() {
        tiles.forEach { tile ->
            if (tile.renderImage != null && !tile.renderImage.isClosed && tile.isVisible) {
                val bitmap = tile.renderImage
                drawContext.canvas.nativeCanvas.drawImageRect(
                    image = bitmap,
                    src = Rect.makeWH(
                        tile.size.width.toFloat(),
                        tile.size.height.toFloat()
                    ),
                    dst = tile.displayRegion.toSkiaRect(),
                    samplingMode = samplingMode,
                    paint = null,
                    strict = true
                )

                if (showDebugGrid) {
                    drawContext.canvas.drawRect(
                        tile.displayRegion,
                        Paint().apply {
                            style = PaintingStyle.Stroke
                            color = Color.Green

                        }
                    )
                }
            }
        }
    }

    override fun withSamplingMode(upsamplingMode: UpsamplingMode): TiledPainter {
        return SkiaTiledPainter(
            tiles = tiles,
            upsamplingMode = upsamplingMode,
            scaleFactor = scaleFactor,
            displaySize = displaySize,
            showDebugGrid = showDebugGrid
        )
    }
}
