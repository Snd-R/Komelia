package io.github.snd_r.komelia.image

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Paint.FILTER_BITMAP_FLAG
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.UpsamplingMode.NEAREST
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.AndroidBitmap.toBitmap
import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage
import snd.komelia.image.ReduceKernel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias RenderImage = Bitmap

class AndroidTilingReaderImage(
    originalImage: KomeliaImage,
    processingPipeline: ImageProcessingPipeline,
    stretchImages: StateFlow<Boolean>,
    pageId: PageId,
    upsamplingMode: StateFlow<UpsamplingMode>,
    downSamplingKernel: StateFlow<ReduceKernel>,
    linearLightDownSampling: StateFlow<Boolean>,
) : TilingReaderImage(
    originalImage = originalImage,
    processingPipeline = processingPipeline,
    stretchImages = stretchImages,
    upsamplingMode = upsamplingMode,
    downSamplingKernel = downSamplingKernel,
    linearLightDownSampling = linearLightDownSampling,
    pageId = pageId
) {

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.renderImage?.recycle() }
    }

    override fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): TiledPainter {
        return AndroidTiledPainter(
            tiles = tiles,
            upsamplingMode = upsamplingMode.value,
            scaleFactor = scaleFactor,
            displaySize = displaySize
        )
    }

    override suspend fun resizeImage(image: KomeliaImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        val resized = image.resize(
            scaleWidth = scaleWidth,
            scaleHeight = scaleHeight,
            linear = linearLightDownSampling.value,
            kernel = downSamplingKernel.value
        )
        val bitmap = resized.toBitmap()
        val imageData = ReaderImageData(resized.width, resized.height, bitmap)
        resized.close()
        return imageData
    }

    override suspend fun getImageRegion(
        image: KomeliaImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        var region: KomeliaImage? = null
        var resized: KomeliaImage? = null
        try {
            region = image.extractArea(imageRegion.toImageRect())
            if (scaleWidth > imageRegion.width || scaleHeight > imageRegion.height) {
                val regionData = region.toReaderImageData()
                return regionData
            }
            resized = region.resize(
                scaleWidth = scaleWidth,
                scaleHeight = scaleHeight,
                linear = linearLightDownSampling.value,
                kernel = downSamplingKernel.value
            )
            return resized.toReaderImageData()
        } finally {
            region?.close()
            resized?.close()
        }
    }

    private fun KomeliaImage.toReaderImageData(): ReaderImageData {
        val bitmap = this.toBitmap()
        return ReaderImageData(this.width, this.height, bitmap)
    }

    private fun IntRect.toImageRect() =
        ImageRect(left = left, top = top, right = right, bottom = bottom)


    private class AndroidTiledPainter(
        private val tiles: List<ReaderImageTile>,
        private val upsamplingMode: UpsamplingMode,
        private val scaleFactor: Double,
        private val displaySize: IntSize,
    ) : TiledPainter() {
        override val intrinsicSize: Size = displaySize.toSize()
        private val paintFlags = when {
            scaleFactor > 1.0 && upsamplingMode != NEAREST -> FILTER_BITMAP_FLAG
            else -> 0
        }

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

        override fun withSamplingMode(upsamplingMode: UpsamplingMode): TiledPainter {
            return AndroidTiledPainter(
                tiles = tiles,
                upsamplingMode = upsamplingMode,
                scaleFactor = scaleFactor,
                displaySize = displaySize,
            )
        }
    }
}