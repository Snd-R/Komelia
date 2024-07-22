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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.TextLine
import kotlin.time.measureTimedValue

actual typealias RenderImage = Image
actual typealias PlatformImage = VipsImage

class DesktopTilingReaderImage(
    encoded: ByteArray,
    pageId: PageId,
    upscaleOption: StateFlow<UpscaleOption>,
    private val upscaler: ManagedOnnxUpscaler?,
) : TilingReaderImage(encoded, pageId) {

    @Volatile
    private var upsamplingMode: SamplingMode = when (upscaleOption.value) {
        skiaSamplerMitchell -> SamplingMode.MITCHELL
        skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
        skiaSamplerNearest -> SamplingMode.DEFAULT
        else -> SamplingMode.MITCHELL
    }

    init {
        upscaleOption.onEach { option ->
            val samplingMode = when (option) {
                skiaSamplerMitchell -> SamplingMode.MITCHELL
                skiaSamplerCatmullRom -> SamplingMode.CATMULL_ROM
                skiaSamplerNearest -> SamplingMode.DEFAULT
                else -> SamplingMode.MITCHELL
            }
            this.upsamplingMode = samplingMode

            if (option in upsamplingFilters) {
                lastUpdateRequest?.let { lastRequest ->
                    lastUsedScaleFactor = null
                    jobFlow.emit(lastRequest)
                }
            }
        }.launchIn(imageScope)

        upscaler?.upscaleMode?.onEach {
            lastUpdateRequest?.let { lastRequest ->
                this.painter.value = PlaceholderPainter(lastRequest.displaySize)
                lastUsedScaleFactor = null
                jobFlow.emit(lastRequest)
            }
        }?.launchIn(imageScope)
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
            return upscaleImage(image, scaleWidth, scaleHeight)
        }

        val downscaled = image.resize(scaleWidth, scaleHeight, false)
        val imageData = downscaled.toReaderImageData()
        downscaled.close()
        return imageData

    }

    private suspend fun upscaleImage(
        image: VipsImage,
        scaleWidth: Int,
        scaleHeight: Int,
    ): ReaderImageData {
        val upscaled = upscaler?.upscale(pageId, image)

        if (upscaled != null) {
            if (upscaled.width > scaleWidth && upscaled.height > scaleHeight) {
                val resized = upscaled.resize(scaleWidth, scaleHeight, false)
                val imageData = resized.toReaderImageData()
                resized.close()
                return imageData
            } else {
                val imageData = upscaled.toReaderImageData()
                return imageData
            }
        } else {
            val imageData = image.toReaderImageData()
            return imageData
        }
    }

    override suspend fun getImageRegion(
        image: VipsImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        if (scaleWidth > imageRegion.width || scaleHeight > imageRegion.height) {
            return upscaleRegion(image, imageRegion, scaleWidth, scaleHeight)
        }

        val region = image.getRegion(imageRegion.toImageRect())
        val downscaled = region.resize(scaleWidth, scaleHeight, false)
        val imageData = downscaled.toReaderImageData()
        downscaled.close()
        region.close()
        return imageData
    }

    private suspend fun upscaleRegion(
        image: VipsImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        // try to reuse full resized image instead of upscaling individual regions
        val upscaled = upscaler?.upscale(pageId, image)

        if (upscaled != null) {
            // assume upscaling is done by integer fraction (2x, 4x etc.)
            val scaleRatio = upscaled.width / image.width
            val targetRegion = ImageRect(
                left = imageRegion.left * scaleRatio,
                right = imageRegion.right * scaleRatio,
                top = imageRegion.top * scaleRatio,
                bottom = imageRegion.bottom * scaleRatio
            )
            val regionMeasured = measureTimedValue { upscaled.getRegion(targetRegion) }
            val region = regionMeasured.value

            // downscale if region is bigger than requested scale
            if (region.width > scaleWidth || region.height > scaleHeight) {
                val resized = measureTimedValue {
                    region.resize(scaleWidth, scaleHeight, false)
                }
                val imageData = measureTimedValue { resized.value.toReaderImageData() }
                resized.value.close()
                region.close()
                return imageData.value
                // otherwise do not upsample and return original region size
            } else {
                val imageData = measureTimedValue {
                    val imageData = region.toReaderImageData()
                    region.close()
                    imageData
                }
                return imageData.value
            }

            // if onnxruntime upscaling wasn't performed return original region size
        } else {
            val region = image.getRegion(imageRegion.toImageRect())
            val imageData = region.toReaderImageData()
            region.close()
            return imageData
        }
    }

    override fun closeImage(image: VipsImage) {
        image.close()
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
