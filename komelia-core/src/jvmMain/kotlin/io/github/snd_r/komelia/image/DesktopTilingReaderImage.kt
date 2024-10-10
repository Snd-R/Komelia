package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.ImageRect
import io.github.snd_r.VipsBitmapFactory
import io.github.snd_r.VipsImage
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.skiaSamplerCatmullRom
import io.github.snd_r.komelia.platform.skiaSamplerMitchell
import io.github.snd_r.komelia.platform.skiaSamplerNearest
import io.github.snd_r.komelia.platform.upsamplingFilters
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.TextLine

actual typealias RenderImage = Image

class DesktopTilingReaderImage(
    encoded: ByteArray,
    processingPipeline: ImageProcessingPipeline,
    upscaleOption: StateFlow<UpscaleOption>,
    stretchImages: StateFlow<Boolean>,
    pageId: PageId,
) : TilingReaderImage(
    encoded,
    processingPipeline,
    stretchImages,
    pageId
) {

    @Volatile
    private var upsamplingMode: SamplingMode = when (upscaleOption.value) {
        skiaSamplerMitchell -> SamplingMode.MITCHELL
        skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
        skiaSamplerNearest -> SamplingMode.DEFAULT
        else -> SamplingMode.MITCHELL
    }

    init {
        upscaleOption.drop(1).onEach { option ->
            val samplingMode = when (option) {
                skiaSamplerMitchell -> SamplingMode.MITCHELL
                skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
                skiaSamplerNearest -> SamplingMode.DEFAULT
                else -> SamplingMode.MITCHELL
            }
            this.upsamplingMode = samplingMode

            if (option in upsamplingFilters) {
                reloadLastRequest()
            }
        }.launchIn(processingScope)
    }

    override fun getDimensions(encoded: ByteArray): IntSize {
        val vipsDimensions = VipsImage.getDimensions(encoded)
        return IntSize(vipsDimensions.width, vipsDimensions.height)
    }

    override fun decode(encoded: ByteArray): PlatformImage {
        return VipsImage.decode(encoded)
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { runCatching { it.renderImage?.close() } }
    }

    override fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): Painter {
        return TiledImagePainter(tiles, upsamplingMode, scaleFactor, displaySize)
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        return PlaceholderPainter(displaySize)
    }

    override suspend fun resizeImage(image: VipsImage, scaleWidth: Int, scaleHeight: Int): ReaderImageData {
        if (scaleWidth > image.width || scaleHeight > image.height) {
            return image.toReaderImageData()
        }

        val downscaled = image.resize(scaleWidth, scaleHeight, false)
        val imageData = downscaled.toReaderImageData()
        downscaled.close()
        return imageData

    }

    override suspend fun getImageRegion(
        image: VipsImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {

        val region = image.getRegion(imageRegion.toImageRect())
        if (scaleWidth > imageRegion.width || scaleHeight > imageRegion.height) {
            return region.toReaderImageData()
        }
        val downscaled = region.resize(scaleWidth, scaleHeight, false)
        val imageData = downscaled.toReaderImageData()
        downscaled.close()
        region.close()
        return imageData
    }

    private fun VipsImage.toReaderImageData(): ReaderImageData {
        val skiaBitmap = VipsBitmapFactory.toSkiaBitmap(this)
        val image = Image.makeFromBitmap(skiaBitmap)
        skiaBitmap.close()
        return ReaderImageData(width, height, image)
    }

    private fun IntRect.toImageRect() = ImageRect(left = left, top = top, right = right, bottom = bottom)

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
        samplingMode: SamplingMode,
        scaleFactor: Double,
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()
        private val samplingMode = if (scaleFactor > 1.0) samplingMode else SamplingMode.DEFAULT

        override fun DrawScope.onDraw() {
            tiles.forEach { tile ->
                if (tile.renderImage != null && !tile.renderImage.isClosed && tile.isVisible) {
                    val bitmap = tile.renderImage
                    drawContext.canvas.nativeCanvas.drawImageRect(
                        image = bitmap,
                        src = org.jetbrains.skia.Rect.makeWH(
                            tile.size.width.toFloat(),
                            tile.size.height.toFloat()
                        ),
                        dst = tile.displayRegion.toSkiaRect(),
                        samplingMode = samplingMode,
                        paint = null,
                        strict = true
                    )

//                    drawContext.canvas.drawRect(
//                        tile.displayRegion,
//                        Paint().apply {
//                            style = PaintingStyle.Stroke
//                            color = Color.Green
//
//                        }
//                    )
                }

            }
        }
    }
}
