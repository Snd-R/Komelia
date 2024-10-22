package io.github.snd_r

import snd.jni.DesktopPlatform.Linux
import snd.jni.DesktopPlatform.MacOS
import snd.jni.DesktopPlatform.Unknown
import snd.jni.DesktopPlatform.Windows
import io.github.snd_r.ImageFormat.GRAYSCALE_8
import io.github.snd_r.ImageFormat.RGBA_8888
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import snd.jni.DesktopPlatform
import snd.jni.SharedLibrariesLoader
import java.util.concurrent.atomic.AtomicBoolean

object VipsBitmapFactory {
    private val loaded = AtomicBoolean(false)

    fun load() {
        if (!loaded.compareAndSet(false, true)) return

        when (DesktopPlatform.Current) {
            Linux -> SharedLibrariesLoader.loadLibrary("komelia_skia")
            Windows -> {}
            MacOS, Unknown -> error("Unsupported OS")
        }
    }

    fun toSkiaBitmap(image: VipsImage): Bitmap {
        return when (DesktopPlatform.Current) {
            Linux -> directCopyToSkiaBitmap(image)
            Windows -> jvmDoubleCopyToSkiaBitmap(image)
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