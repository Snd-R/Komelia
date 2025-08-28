package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.processing.ImageProcessingPipeline
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import snd.komelia.image.ImageDecoder
import snd.komelia.image.ImageRect
import snd.komelia.image.KomeliaImage
import snd.komelia.image.ReduceKernel
import snd.komelia.image.SkiaBitmap.toSkiaBitmap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias RenderImage = Image

class DesktopReaderImage(
    imageDecoder: ImageDecoder,
    imageSource: ImageSource,
    processingPipeline: ImageProcessingPipeline,
    stretchImages: StateFlow<Boolean>,
    pageId: PageId,
    upsamplingMode: StateFlow<UpsamplingMode>,
    downSamplingKernel: StateFlow<ReduceKernel>,
    linearLightDownSampling: StateFlow<Boolean>,
    private val upscaler: DesktopOnnxRuntimeUpscaler?,
    private val showDebugGrid: StateFlow<Boolean>,
) : TilingReaderImage(
    imageDecoder = imageDecoder,
    imageSource = imageSource,
    processingPipeline = processingPipeline,
    stretchImages = stretchImages,
    upsamplingMode = upsamplingMode,
    downSamplingKernel = downSamplingKernel,
    linearLightDownSampling = linearLightDownSampling,
    pageId = pageId,
) {

    init {
        upscaler?.upscaleMode?.drop(1)?.onEach {
            lastUpdateRequest?.let { lastRequest ->
                this.painter.value = null
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
    ): TiledPainter {
        return SkiaTiledPainter(
            tiles = tiles,
            showDebugGrid = showDebugGrid.value,
            upsamplingMode = upsamplingMode.value,
            scaleFactor = scaleFactor,
            displaySize = displaySize
        )
    }

    override suspend fun resizeImage(
        image: KomeliaImage,
        scaleWidth: Int,
        scaleHeight: Int
    ): ReaderImageData {
        if (scaleWidth > image.width || scaleHeight > image.pageHeight) {
            return upscaleImage(image, scaleWidth, scaleHeight)
        }

        val downscaled = image.resize(
            scaleWidth = scaleWidth,
            scaleHeight = scaleHeight,
            linear = linearLightDownSampling.value,
            kernel = downSamplingKernel.value
        )
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

    private suspend fun upscaleImage(
        image: KomeliaImage,
        scaleWidth: Int,
        scaleHeight: Int,
    ): ReaderImageData {
        val upscaled = upscaler?.upscale(image, pageId.toString())

        if (upscaled != null) {
            if (upscaled.width > scaleWidth && upscaled.pageHeight > scaleHeight) {
                val resized = upscaled.resize(
                    scaleWidth = scaleWidth,
                    scaleHeight = scaleHeight,
                    linear = linearLightDownSampling.value,
                    kernel = downSamplingKernel.value
                )
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
        val upscaled = upscaler?.upscale(image, pageId.toString())
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
                if (region.width > scaleWidth || region.pageHeight > scaleHeight) {
                    resized = region.resize(
                        scaleWidth = scaleWidth,
                        scaleHeight = scaleHeight,
                        linear = linearLightDownSampling.value,
                        kernel = downSamplingKernel.value
                    )
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

    private suspend fun KomeliaImage.toReaderImageData(): ReaderImageData {
        if (this.pagesLoaded == 1) {
            val skiaBitmap = this.toSkiaBitmap()
            val image = Image.makeFromBitmap(skiaBitmap)
            skiaBitmap.close()
            return ReaderImageData(width, height, listOf(image), null)
        }

        val frames = mutableListOf<RenderImage>()
        val delays = pageDelays?.let { mutableListOf<Long>() }
        for (i in 0 until this.pagesLoaded) {
            val skiaBitmap = this.extractArea(
                ImageRect(
                    left = 0,
                    right = width,
                    top = pageHeight * i,
                    bottom = pageHeight * (i + 1),
                )
            ).toSkiaBitmap()
            val image = Image.makeFromBitmap(skiaBitmap)
            skiaBitmap.close()

            frames.add(image)
            delays?.add(this.pageDelays?.getOrNull(i)?.toLong() ?: defaultFrameDelay)
        }
        return ReaderImageData(width, pageHeight, frames, delays)
    }

    private fun IntRect.toImageRect() = ImageRect(left = left, top = top, right = right, bottom = bottom)

    private class SkiaTiledPainter(
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

}
