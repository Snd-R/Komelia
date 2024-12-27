package snd.komelia.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import snd.jni.DesktopPlatform
import snd.jni.DesktopPlatform.Linux
import snd.jni.DesktopPlatform.MacOS
import snd.jni.DesktopPlatform.Unknown
import snd.jni.DesktopPlatform.Windows
import snd.jni.SharedLibrariesLoader
import snd.komelia.image.ImageFormat.GRAYSCALE_8
import snd.komelia.image.ImageFormat.HISTOGRAM
import snd.komelia.image.ImageFormat.RGBA_8888
import java.util.concurrent.atomic.AtomicBoolean

object SkiaBitmap {
    private val loaded = AtomicBoolean(false)

    @Volatile
    private var skiaIntegrationLibIsLoaded = false

    fun load() {
        if (!loaded.compareAndSet(false, true)) return

        when (DesktopPlatform.Current) {
            Linux -> try {
                SharedLibrariesLoader.loadLibrary("komelia_skia")
                skiaIntegrationLibIsLoaded = true
            } catch (e: UnsatisfiedLinkError) {
                skiaIntegrationLibIsLoaded = false
            }

            Windows -> {}
            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    fun KomeliaImage.toSkiaBitmap(): Bitmap = this.toVipsImage().toSkiaBitmap()
    fun VipsImage.toSkiaBitmap(): Bitmap {
        return when (DesktopPlatform.Current) {
            Linux -> {
                if (skiaIntegrationLibIsLoaded) directCopyToSkiaBitmap(this)
                else jvmDoubleCopyToSkiaBitmap(this)
            }

            Windows -> jvmDoubleCopyToSkiaBitmap(this)
            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    private fun jvmDoubleCopyToSkiaBitmap(image: VipsImage): Bitmap {
        val colorInfo = when (image.type) {
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

        val imageInfo = ImageInfo(colorInfo, image.width, image.height)
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)

        // copy vips image bytes to jvm
        val bytes = image.getBytes()
        // copy jvm bytes to skia bitmap
        bitmap.installPixels(bytes)
        bitmap.setImmutable()
        return bitmap
    }

    private external fun directCopyToSkiaBitmap(image: VipsImage): Bitmap
}