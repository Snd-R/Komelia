package snd.komelia.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import snd.komelia.image.ImageFormat.GRAYSCALE_8
import snd.komelia.image.ImageFormat.HISTOGRAM
import snd.komelia.image.ImageFormat.RGBA_8888
import java.util.concurrent.atomic.AtomicBoolean

object SkiaBitmap {
    private val loaded = AtomicBoolean(false)

    fun load() {
        if (!loaded.compareAndSet(false, true)) return
    }

    fun KomeliaImage.toSkiaBitmap(): Bitmap = this.toVipsImage().toSkiaBitmap()
    fun VipsImage.toSkiaBitmap(): Bitmap {
        val colorInfo = when (this.type) {
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

            HISTOGRAM -> error("Unsupported image format")
        }

        val imageInfo = ImageInfo(colorInfo, this.width, this.height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)

        // copy vips image bytes to jvm
        val bytes = this.getBytes()
        // copy jvm bytes to skia bitmap
        bitmap.installPixels(bytes)
        bitmap.setImmutable()
        return bitmap
    }
}