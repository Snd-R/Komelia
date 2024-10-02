package io.github.snd_r.komelia.image

import io.github.snd_r.komelia.worker.ImageData
import io.github.snd_r.komelia.worker.ImageWorker.Interpretation
import io.github.snd_r.komelia.worker.util.asByteArray
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

fun ImageData.toBitmap(): Bitmap {
    val colorInfo = when (interpretation) {
        Interpretation.BW -> {
            require(bands == 1) { "Unexpected number of bands  for grayscale image \"${bands}\"" }
            ColorInfo(
                ColorType.GRAY_8,
                ColorAlphaType.UNPREMUL,
                ColorSpace.sRGB
            )
        }

        Interpretation.SRGB -> {
            require(bands == 4) { "Unexpected number of bands  for sRGB image  \"${bands}\"" }
            ColorInfo(
                ColorType.RGBA_8888,
                ColorAlphaType.UNPREMUL,
                ColorSpace.sRGB
            )
        }
    }

    val imageInfo = ImageInfo(colorInfo, width, height)
    val bitmap = Bitmap()
    bitmap.allocPixels(imageInfo)
    // FIXME? blocking js array to kotlin array copy
    //  blocking kotlin array to skia pixels copy
    bitmap.installPixels(buffer.asByteArray())
    bitmap.setImmutable()
    return bitmap
}
