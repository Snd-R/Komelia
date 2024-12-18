package io.github.snd_r.komelia.image

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.komelia.worker.ImageWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.SamplingMode
import kotlin.math.roundToInt

class WasmSimpleReaderImage(
    private val encoded: ByteArray,
    private val width: Int,
    private val height: Int,
    private val worker: ImageWorker,
    private val stretchImages: StateFlow<Boolean>,
    override val pageId: ReaderImage.PageId,
) : ReaderImage {
    override val painter by lazy { MutableStateFlow(noopPainter) }
    override val error: StateFlow<Exception?> = MutableStateFlow<Exception?>(null)
    override val currentSize = MutableStateFlow<IntSize?>(null)
    override val originalSize = MutableStateFlow<IntSize?>(IntSize(width, height))
    override val displaySize = MutableStateFlow<IntSize?>(null)

    private var currentImage: Image? = null
    private var lastUsedScaleFactor: Double? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun requestUpdate(
        visibleDisplaySize: IntRect,
        zoomFactor: Float,
        maxDisplaySize: IntSize,
    ) {
        coroutineScope.launch {
            val displaySize = calculateSizeForArea(maxDisplaySize, stretchImages.value)
            val widthRatio = displaySize.width.toDouble() / width
            val heightRatio = displaySize.height.toDouble() / height
            val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)
            val actualScaleFactor = displayScaleFactor * zoomFactor
            if (lastUsedScaleFactor == actualScaleFactor) return@launch

            val dstWidth = (width * actualScaleFactor).roundToInt()
            val dstHeight = (height * actualScaleFactor).roundToInt()
            val bitmap = worker.decodeAndGet(
                bytes = encoded,
                dstWidth = dstWidth,
                dstHeight = dstHeight,
                crop = false
            ).toBitmap()
            val image = Image.makeFromBitmap(bitmap)
            currentImage = image
            bitmap.close()

            this@WasmSimpleReaderImage.currentSize.value = IntSize(dstWidth, dstHeight)
            this@WasmSimpleReaderImage.displaySize.value = displaySize
            painter.value = ImagePainter(image, SamplingMode.CATMULL_ROM, actualScaleFactor, displaySize)
            lastUsedScaleFactor = actualScaleFactor
        }
    }


    override fun close() {
        currentImage?.close()
    }

    override suspend fun getOriginalSize(): IntSize {
        return IntSize(width, height)
    }
}

private class ImagePainter(
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
