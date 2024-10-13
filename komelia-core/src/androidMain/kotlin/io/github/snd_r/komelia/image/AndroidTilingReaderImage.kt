package io.github.snd_r.komelia.image

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Paint.FILTER_BITMAP_FLAG
import androidx.compose.ui.geometry.Size
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
import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.flow.StateFlow

actual typealias RenderImage = Bitmap

class AndroidTilingReaderImage(
    encoded: ByteArray,
    processingPipeline: ImageProcessingPipeline,
    stretchImages: StateFlow<Boolean>,
    pageId: PageId,
) : TilingReaderImage(
    encoded,
    processingPipeline,
    stretchImages,
    pageId
) {

    override fun getDimensions(encoded: ByteArray): IntSize {
        val vipsDimensions = VipsImage.Companion.getDimensions(encoded)
        return IntSize(vipsDimensions.width, vipsDimensions.height)
    }

    override fun decode(encoded: ByteArray): PlatformImage {
        return VipsImage.decode(encoded)
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.renderImage?.recycle() }
    }

    override fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): Painter {
        return TiledImagePainter(tiles, scaleFactor, displaySize)
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        return PlaceholderPainter(displaySize)
    }

    override suspend fun resizeImage(image: PlatformImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        val resized = image.resize(scaleWidth, scaleHeight, false)
        val bitmap = VipsBitmapFactory.createHardwareBitmap(resized)
        val imageData = ReaderImageData(resized.width, resized.height, bitmap)
        resized.close()
        return imageData
    }

    override suspend fun getImageRegion(
        image: PlatformImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        var region: VipsImage? = null
        var resized: VipsImage? = null
        try {
            region = image.extractArea(imageRegion.toImageRect())
            if (scaleWidth > imageRegion.width || scaleHeight > imageRegion.height) {
                val regionData = region.toReaderImageData()
                return regionData
            }
            resized = region.resize(scaleWidth, scaleHeight, false)
            return resized.toReaderImageData()
        } finally {
            region?.close()
            resized?.close()
        }
    }

    private fun VipsImage.toReaderImageData(): ReaderImageData {
        val bitmap = VipsBitmapFactory.createHardwareBitmap(this)
        return ReaderImageData(this.width, this.height, bitmap)
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
                Paint().apply { textSize = 50f }
            )
        }
    }

    private class TiledImagePainter(
        private val tiles: List<ReaderImageTile>,
        scaleFactor: Double,
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()
        private val paintFlags = if (scaleFactor > 1.0) FILTER_BITMAP_FLAG else 0

        override fun DrawScope.onDraw() {
            tiles.forEach { tile ->
                if (tile.renderImage != null && !tile.renderImage.isRecycled && tile.isVisible) {
                    val bitmap: Bitmap = tile.renderImage
                    drawContext.canvas.nativeCanvas.drawBitmap(
                        bitmap,
                        null,
                        tile.displayRegion.toAndroidRectF(),
                        Paint().apply { flags = paintFlags },
                    )

//                    drawContext.canvas.drawRect(
//                        tile.displayRegion,
//                        Paint().apply {
//                            style = PaintingStyle.Stroke
//                            color = Color.Green
//                        }
//                    )
                }

            }
        }
    }
}