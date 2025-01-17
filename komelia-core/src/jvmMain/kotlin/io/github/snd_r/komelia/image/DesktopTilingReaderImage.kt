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
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
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
import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage
import snd.komelia.image.SkiaBitmap.toSkiaBitmap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias RenderImage = Image

class DesktopTilingReaderImage(
    originalImage: KomeliaImage,
    processingPipeline: ImageProcessingPipeline,
    upscaleOption: StateFlow<UpscaleOption>,
    stretchImages: StateFlow<Boolean>,
    pageId: PageId,
    private val upscaler: ManagedOnnxUpscaler?,
    private val showDebugGrid: StateFlow<Boolean>,
) : TilingReaderImage(
    originalImage,
    processingPipeline,
    stretchImages,
    pageId,
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

        upscaler?.upscaleMode?.drop(1)?.onEach {
            lastUpdateRequest?.let { lastRequest ->
                val displaySize = calculateSizeForArea(lastRequest.maxDisplaySize, stretchImages.value)
                this.painter.value = PlaceholderPainter(displaySize)
                reloadLastRequest()
            }
        }?.launchIn(processingScope)
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { runCatching { it.renderImage?.close() } }
    }

    override fun createTilePainter(
        tiles: List<ReaderImageTile>,
        displaySize: IntSize,
        scaleFactor: Double
    ): Painter {
        return TiledImagePainter(
            tiles = tiles,
            showDebugGrid = showDebugGrid.value,
            samplingMode = upsamplingMode,
            scaleFactor = scaleFactor,
            displaySize = displaySize
        )
    }

    override fun createPlaceholderPainter(displaySize: IntSize): Painter {
        return PlaceholderPainter(displaySize)
    }

    override suspend fun resizeImage(
        image: KomeliaImage,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        if (scaleWidth > image.width || scaleHeight > image.height) {
            return upscaleImage(image, scaleWidth, scaleHeight)
        }

        val downscaled = image.resize(scaleWidth, scaleHeight, false)
        val imageData = downscaled.toReaderImageData()
        downscaled.close()
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
                return upscaleRegion(image, imageRegion, scaleWidth, scaleHeight)
            }
            resized = region.resize(scaleWidth, scaleHeight, false)
            return resized.toReaderImageData()
        } finally {
            region?.close()
            resized?.close()
        }
    }

    private suspend fun upscaleImage(
        image: KomeliaImage,
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


    private suspend fun upscaleRegion(
        image: KomeliaImage,
        imageRegion: IntRect,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        // try to reuse full resized image instead of upscaling individual regions
        val upscaled = upscaler?.upscale(pageId, image)
        var region: KomeliaImage? = null
        var resized: KomeliaImage? = null

        try {
            if (upscaled != null) {
                // assume upscaling is done by integer fraction (2x, 4x etc.)
                val scaleRatio = upscaled.width / image.width
                val targetRegion = ImageRect(
                    left = imageRegion.left * scaleRatio,
                    right = imageRegion.right * scaleRatio,
                    top = imageRegion.top * scaleRatio,
                    bottom = imageRegion.bottom * scaleRatio
                )
                region = upscaled.extractArea(targetRegion)

                // downscale if region is bigger than requested scale
                if (region.width > scaleWidth || region.height > scaleHeight) {
                    resized = region.resize(scaleWidth, scaleHeight, false)
                    return resized.toReaderImageData()
                    // otherwise do not upsample and return original region size
                } else {
                    return region.toReaderImageData()
                }

                // if onnxruntime upscaling wasn't performed return original region size
            } else {
                region = image.extractArea(imageRegion.toImageRect())
                return region.toReaderImageData()
            }
        } finally {
            resized?.close()
            region?.close()
        }
    }

    private fun KomeliaImage.toReaderImageData(): ReaderImageData {
        val skiaBitmap = this.toSkiaBitmap()

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
        private val showDebugGrid: Boolean,
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
    }
}
