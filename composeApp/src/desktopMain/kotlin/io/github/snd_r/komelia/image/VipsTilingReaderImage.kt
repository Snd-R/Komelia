package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.ImageFormat
import io.github.snd_r.ImageFormat.GRAYSCALE_8
import io.github.snd_r.ImageFormat.RGBA_8888
import io.github.snd_r.ImageRect
import io.github.snd_r.VipsImage
import io.github.snd_r.VipsImageData
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Font
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.TextLine

actual typealias Bitmap = org.jetbrains.skia.Bitmap

class VipsTilingReaderImage(
    encoded: ByteArray,
    private val onnxModelPath: String?
) : TilingReaderImage(encoded) {

    override fun getDimensions(encoded: ByteArray): IntSize {
        val vipsDimensions = VipsImage.getDimensions(encoded)
        return IntSize(vipsDimensions.width, vipsDimensions.height)
    }

    override fun decode(encoded: ByteArray): PlatformImage {
        return VipsPlatformImage(VipsImage.decode(encoded))
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.bitmap?.close() }
    }

    override fun createTilePainter(tiles: List<ReaderImageTile>, displaySize: IntSize): Painter {
        return TiledImagePainter(tiles, displaySize)
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        return PlaceholderPainter(displaySize)
    }

    private class VipsPlatformImage(private val image: VipsImage) : PlatformImage {

        override fun resize(scaleWidth: Int, scaleHeight: Int): ReaderImageData {
            return image.resize(scaleWidth, scaleHeight, false).toReaderImageData()
        }

        override fun getRegion(rect: IntRect, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
            return image.getRegion(rect.toImageRect(), scaleWidth, scaleHeight).toReaderImageData()
        }

        override fun close() {
            image.close()
        }

        private fun VipsImageData.toReaderImageData(): ReaderImageData {
            val skiaBitmap = createSkiaBitmap(type, width, height, data)
            return ReaderImageData(width, height, skiaBitmap)
        }

        private fun createSkiaBitmap(
            type: ImageFormat,
            width: Int,
            height: Int,
            bytes: ByteArray
        ): Bitmap {
            val colorInfo = when (type) {
                GRAYSCALE_8 -> ColorInfo(
                    ColorType.GRAY_8,
                    ColorAlphaType.UNPREMUL,
                    ColorSpace.sRGB
                )

                RGBA_8888 -> ColorInfo(
                    ColorType.RGBA_8888,
                    ColorAlphaType.UNPREMUL,
                    ColorSpace.sRGB
                )
            }

            val imageInfo = ImageInfo(colorInfo, width, height)
            val bitmap = Bitmap()
            bitmap.allocPixels(imageInfo)
            bitmap.installPixels(bytes)
            bitmap.setImmutable()
            return bitmap
        }

        private fun IntRect.toImageRect() = ImageRect(left = left, top = top, right = right, bottom = bottom)
    }

    private class PlaceholderPainter(
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()

        override fun DrawScope.onDraw() {
            val textLine = TextLine.Companion.make("Loading", Font(null, 50f))
            drawContext.canvas.nativeCanvas.drawTextLine(
                textLine,
                drawContext.size.width / 2 - textLine.width / 2, drawContext.size.height / 2,
                org.jetbrains.skia.Paint()
            )
        }
    }

    private class TiledImagePainter(
        private val tiles: List<ReaderImageTile>,
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()

        override fun DrawScope.onDraw() {
            println("tiled painter on draw; tiles size: ${tiles.size}")
            tiles.forEach { tile ->
                if (tile.bitmap != null && tile.isVisible) {
                    drawContext.canvas.nativeCanvas.drawImageRect(
                        image = org.jetbrains.skia.Image.makeFromBitmap(tile.bitmap),
                        src = org.jetbrains.skia.Rect.makeWH(
                            tile.originalSize.width.toFloat(),
                            tile.originalSize.height.toFloat()
                        ),
                        dst = tile.displayRegion.toSkiaRect(),
                        samplingMode = SamplingMode.DEFAULT,
                        paint = null,
                        strict = true
                    )

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
}



