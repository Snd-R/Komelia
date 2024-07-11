package io.github.snd_r.komelia.image

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.ImageRect
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsImage

actual typealias Bitmap = Bitmap
actual typealias PlatformImage = VipsImage

class AndroidTilingReaderImage(encoded: ByteArray) : TilingReaderImage(encoded) {

    override fun getDimensions(encoded: ByteArray): IntSize {
        val vipsDimensions = VipsImage.Companion.getDimensions(encoded)
        return IntSize(vipsDimensions.width, vipsDimensions.height)
    }

    override fun decode(encoded: ByteArray): PlatformImage {
        return VipsImage.decode(encoded)
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.bitmap?.recycle() }
    }

    override fun createTilePainter(tiles: List<ReaderImageTile>, displaySize: IntSize): Painter {
        return TiledImagePainter(tiles, displaySize)
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        return PlaceholderPainter(displaySize)
    }

    override suspend fun resizeImage(image: VipsImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        val resized = image.resize(scaleWidth, scaleHeight, false)
        val bitmap = VipsBitmapFactory.createSoftwareBitmap(resized)
        val imageData = ReaderImageData(resized.width, resized.height, bitmap)
        resized.close()
        return imageData
    }

    override suspend fun getImageRegion(
        image: VipsImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        val region = image.getRegion(imageRegion.toImageRect())
        val resized = region.resize(scaleWidth, scaleHeight, false)

        val bitmap = VipsBitmapFactory.createHardwareBitmap(resized)
        val imageData = ReaderImageData(resized.width, resized.height, bitmap)
        region.close()
        resized.close()
        return imageData
    }

    override fun closeImage(image: PlatformImage) {
        image.close()
    }

    private fun IntRect.toImageRect() =
        ImageRect(left = left, top = top, right = right, bottom = bottom)

    private class PlaceholderPainter(
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()

        override fun DrawScope.onDraw() {
            drawContext.canvas.nativeCanvas.drawText(
                "Loading",
                drawContext.size.width / 2 - 50f, drawContext.size.height / 2,
                android.graphics.Paint().apply { textSize = 50f }
            )
        }
    }

    private class TiledImagePainter(
        private val tiles: List<ReaderImageTile>,
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()

        override fun DrawScope.onDraw() {
            val isUpscale = tiles.firstOrNull()
                ?.let { tile -> tile.size.width < tile.displayRegion.width || tile.size.height < tile.displayRegion.height }
                ?: false
            val currentSamplingMode = if (isUpscale) FilterQuality.High else FilterQuality.None
            tiles.forEach { tile ->
                if (tile.bitmap != null && !tile.bitmap.isRecycled && tile.isVisible) {
                    val bitmap: Bitmap = tile.bitmap
                    drawContext.canvas.nativeCanvas.drawBitmap(
                        bitmap,
                        null,
                        tile.displayRegion.toAndroidRectF(),
                        null,
                    )

                    drawContext.canvas.drawRect(
                        tile.displayRegion,
                        Paint().apply {
                            style = PaintingStyle.Stroke
                            color = Color.Green
                            filterQuality = currentSamplingMode
                        }
                    )
                }

            }
        }
    }
}