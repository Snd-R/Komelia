package io.github.snd_r.komelia.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import snd.komelia.image.ImageFormat
import snd.komelia.image.KomeliaImage

suspend fun KomeliaImage.toBitmap(): Bitmap {
    val colorInfo = when (type) {
        ImageFormat.GRAYSCALE_8 -> {
            require(bands == 1) { "Unexpected number of bands  for grayscale image \"${bands}\"" }
            ColorInfo(
                ColorType.GRAY_8,
                ColorAlphaType.UNPREMUL,
                ColorSpace.sRGB
            )
        }

        ImageFormat.RGBA_8888 -> {
            require(bands == 4) { "Unexpected number of bands  for sRGB image  \"${bands}\"" }
            ColorInfo(
                ColorType.RGBA_8888,
                ColorAlphaType.UNPREMUL,
                ColorSpace.sRGB
            )
        }

        ImageFormat.HISTOGRAM -> error("Unsupported image type $type")
    }

    val imageInfo = ImageInfo(colorInfo, width, height)
    val bitmap = Bitmap()
    bitmap.allocPixels(imageInfo)
    // FIXME? blocking js array to kotlin array copy
    //  blocking kotlin array to skia pixels copy
    val imageBytes = getBytes()
    bitmap.installPixels(imageBytes)
    bitmap.setImmutable()
    return bitmap
}
