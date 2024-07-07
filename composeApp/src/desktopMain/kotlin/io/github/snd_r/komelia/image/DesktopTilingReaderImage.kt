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
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.ImageFormat
import io.github.snd_r.ImageFormat.GRAYSCALE_8
import io.github.snd_r.ImageFormat.RGBA_8888
import io.github.snd_r.ImageRect
import io.github.snd_r.OnnxRuntimeUpscaler
import io.github.snd_r.VipsImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Font
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.TextLine
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.math.roundToInt
import kotlin.time.measureTimedValue

actual typealias Bitmap = Bitmap
actual typealias PlatformImage = VipsImage

private val logger = KotlinLogging.logger {}

class DesktopTilingReaderImage(
    encoded: ByteArray,
    private val upsamplingMode: StateFlow<SamplingMode>,
    private val onnxModelPath: StateFlow<Path?>,
    private val onnxRuntimeCacheDir: Path,
) : TilingReaderImage(encoded) {

    private var onnxRuntimeFullImage: Path? = null
    private val onnxRuntimeMutex = Mutex()

    init {
        onnxModelPath
            .drop(1)
            .onEach {
                try {
                    onnxRuntimeMutex.withLock {
                        onnxRuntimeFullImage?.deleteIfExists()
                        onnxRuntimeFullImage = null
                    }

                    this.painter.value = PlaceholderPainter(lastUpdateRequest?.displaySize ?: IntSize(width, height))

                    val oldTiles = tiles.value
                    this.tiles.value = emptyList()
                    closeTileBitmaps(oldTiles)

                    lastUsedScaleFactor = null

                    lastUpdateRequest?.let { jobFlow.emit(it) }
                } catch (e: Exception) {
                    logger.catching(e)
                    this.error.value = e
                }
            }.launchIn(imageScope)

        upsamplingMode
            .drop(1)
            .onEach { samplingMode ->
                painter.update { current ->
                    TiledImagePainter(
                        tiles.value,
                        samplingMode,
                        lastUpdateRequest?.displaySize
                            ?: IntSize(
                                current.intrinsicSize.width.roundToInt(),
                                current.intrinsicSize.height.roundToInt()
                            )
                    )
                }
            }.launchIn(imageScope)
    }

    override fun getDimensions(encoded: ByteArray): IntSize {
        val vipsDimensions = VipsImage.getDimensions(encoded)
        return IntSize(vipsDimensions.width, vipsDimensions.height)
    }

    override fun decode(encoded: ByteArray): PlatformImage {
        return VipsImage.decode(encoded)
    }

    override fun closeTileBitmaps(tiles: List<ReaderImageTile>) {
        tiles.forEach { it.bitmap?.close() }
    }

    override fun createTilePainter(tiles: List<ReaderImageTile>, displaySize: IntSize): Painter {
        return TiledImagePainter(tiles, upsamplingMode.value, displaySize)
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
        val upscaled = onnxRuntimeUpscale(image)

        if (upscaled != null) {
            if (upscaled.width > scaleWidth && upscaled.height > scaleHeight) {
                val resized = upscaled.resize(scaleWidth, scaleHeight, false)
                val imageData = resized.toReaderImageData()
                resized.close()
                upscaled.close()
                return imageData
            } else {
                val imageData = upscaled.toReaderImageData()
                upscaled.close()
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
        val upscaled = onnxRuntimeUpscale(image)

        if (upscaled != null) {
            // assume upscaling is done by integer fraction (2x, 4x etc.)
            val scaleRatio = upscaled.width / image.width

            val region = upscaled.getRegion(
                ImageRect(
                    left = imageRegion.left * scaleRatio,
                    right = imageRegion.right * scaleRatio,
                    top = imageRegion.top * scaleRatio,
                    bottom = imageRegion.bottom * scaleRatio
                )
            )

            // downscale if region is bigger than requested scale
            if (region.width > scaleWidth || region.height > scaleHeight) {
                val resized = upscaled.resize(scaleWidth, scaleHeight, false)
                val imageData = resized.toReaderImageData()
                resized.close()
                upscaled.close()
                region.close()
                return imageData
                // otherwise do not upsample and return original region size
            } else {
                val imageData = region.toReaderImageData()
                region.close()
                return imageData
            }

            // if onnxruntime upscaling wasn't performed return original region size
        } else {
            val region = image.getRegion(imageRegion.toImageRect())
            val imageData = region.toReaderImageData()
            region.close()
            return imageData
        }
    }

    private suspend fun onnxRuntimeUpscale(image: VipsImage): VipsImage? {
        val onnxModelPath = this.onnxModelPath.value ?: return null

        return withContext(Dispatchers.IO) {
            onnxRuntimeMutex.withLock {
                val savedPath = onnxRuntimeFullImage

                val upscaledImage =
                    if (savedPath != null) {
                        VipsImage.decodeFromFile(savedPath.toString())
                    } else {
                        logger.info { "launching onnx runtime upscaling for image size: ${image.width} x ${image.height}" }
                        val onnxRuntimeImage = measureTimedValue {
                            OnnxRuntimeUpscaler.upscale(image, onnxModelPath.absolutePathString())
                        }.also { logger.info { "finished onnxruntime upscaling in ${it.duration}" } }

                        val writePath = kotlin.io.path.createTempFile(onnxRuntimeCacheDir, suffix = "_onnxruntime.png")
                        onnxRuntimeImage.value.encodeToFile(writePath.absolutePathString())
                        onnxRuntimeFullImage = writePath
                        onnxRuntimeImage.value
                    }
                upscaledImage
            }
        }
    }

    override fun closeImage(image: VipsImage) {
        image.close()
    }

    override fun close() {
        super.close()
        imageScope.launch { onnxRuntimeMutex.withLock { onnxRuntimeFullImage?.deleteIfExists() } }
    }

    private fun VipsImage.toReaderImageData(): ReaderImageData {
        val skiaBitmap = createSkiaBitmap(type, width, height, getBytes())
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
        private val samplingMode: SamplingMode,
        displaySize: IntSize,
    ) : Painter() {
        override val intrinsicSize: Size = displaySize.toSize()

        override fun DrawScope.onDraw() {
            tiles.forEach { tile ->
                if (tile.bitmap != null && tile.isVisible) {
                    val bitmap = tile.bitmap
                    drawContext.canvas.nativeCanvas.drawImageRect(
                        image = org.jetbrains.skia.Image.makeFromBitmap(bitmap),
                        src = org.jetbrains.skia.Rect.makeWH(
                            tile.size.width.toFloat(),
                            tile.size.height.toFloat()
                        ),
                        dst = tile.displayRegion.toSkiaRect(),
                        samplingMode = samplingMode,
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



