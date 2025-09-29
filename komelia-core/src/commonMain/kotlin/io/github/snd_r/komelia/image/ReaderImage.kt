package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage
import kotlin.math.roundToInt

interface ReaderImage : AutoCloseable {
    val pageId: PageId

    val originalSize: StateFlow<IntSize?>
    val displaySize: StateFlow<IntSize?>
    val currentSize: StateFlow<IntSize?>
    val painter: StateFlow<Painter?>
    val error: StateFlow<Throwable?>

    /**
     * request an asynchronous update.
     * Should drop requests if unable to process them fast enough while only processing the most recent request
     *
     * @param maxDisplaySize - area available for image display (e.g. screen size 1920x1080 for full screen display)
     * @param zoomFactor - factor used to calculate max display size and target dimensions of the image.
     * (1920x1080 maxDisplaySize with scale factor of 1.5 will turn into 2880x1620 max size)
     * @param visibleDisplaySize - visible viewport region,
     * this enables optimizations for partial loading and will only load visible portion of the image
     */
    fun requestUpdate(
        maxDisplaySize: IntSize,
        zoomFactor: Float,
        visibleDisplaySize: IntRect,
    )

    /**
     * use this to get non-null size without filtering originalSize StateFlow
     * this function avoids infinite value await when image fails to decode during initialization
     *
     * @throws kotlin.coroutines.cancellation.CancellationException if there's an error during decoding
     */
    suspend fun getOriginalImageSize(): IntSize

    suspend fun getOriginalImage(): KomeliaImage

    /**
     * calculate image dimensions that can be displayed on maxDisplaySize area
     * scales image up or down while preserving original image aspect ratio
     *
     * @param maxDisplaySize - area available for image display (e.g. screen size 1920x1080 for full screen display)
     * @param allowUpscale - limit max image dimensions to original image size
     */
    suspend fun calculateSizeForArea(maxDisplaySize: IntSize, allowUpscale: Boolean): IntSize {
        val imageSize = getOriginalImageSize()
        if (imageSize == IntSize.Zero) return maxDisplaySize
        val widthRatio = maxDisplaySize.width.toDouble() / imageSize.width
        val heightRatio = maxDisplaySize.height.toDouble() / imageSize.height
        val displayScaleFactor = widthRatio.coerceAtMost(heightRatio)

        val displayWidth = (imageSize.width * displayScaleFactor).roundToInt()
        val displayHeight = (imageSize.height * displayScaleFactor).roundToInt()

        val size = if (allowUpscale) IntSize(displayWidth, displayHeight)
        else IntSize(
            displayWidth.coerceAtMost(imageSize.width),
            displayHeight.coerceAtMost(imageSize.height)
        )
        return size
    }

    data class PageId(
        val bookId: String,
        val pageNumber: Int
    ) {
        override fun toString(): String {
            return "${bookId}_$pageNumber"
        }
    }
}
