package io.github.snd_r.komelia.image

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import org.jetbrains.skia.SamplingMode
import snd.komelia.image.KomeliaImage
import kotlin.math.roundToInt

class WasmSimpleReaderImage(
    private val image: KomeliaImage,
    private val stretchImages: StateFlow<Boolean>,
    override val pageId: ReaderImage.PageId,
) : ReaderImage {
    override val painter by lazy { MutableStateFlow(noopPainter) }
    override val error: StateFlow<Exception?> = MutableStateFlow<Exception?>(null)
    override val currentSize = MutableStateFlow<IntSize?>(null)
    override val originalSize = MutableStateFlow<IntSize?>(IntSize(image.width, image.height))
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
            val widthRatio = displaySize.width.toDouble() / image.width
            val heightRatio = displaySize.height.toDouble() / image.height
            val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)
            val actualScaleFactor = displayScaleFactor * zoomFactor
            if (lastUsedScaleFactor == actualScaleFactor) return@launch

            val dstWidth = (image.width * actualScaleFactor).roundToInt()
            val dstHeight = (image.height * actualScaleFactor).roundToInt()
            val bitmap = image.resize(
                scaleWidth = dstWidth,
                scaleHeight = dstHeight,
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
        return IntSize(image.width, image.height)
    }
}
