package io.github.snd_r.komelia.curves

import androidx.compose.ui.graphics.ImageBitmap
import snd.komelia.image.KomeliaImage

actual fun getImage(): KomeliaImage {
    TODO("Not yet implemented")
}

actual fun transformImage(
    image: KomeliaImage,
    targetHeight: Int,
    colorLut: LookupTable?,
    redLut: LookupTable?,
    greenLut: LookupTable?,
    blueLut: LookupTable?
): KomeliaImage {
    TODO("Not yet implemented")
}

actual fun toImageBitmap(image: KomeliaImage): ImageBitmap {
    TODO("Not yet implemented")
}

actual fun getHistogram(image: KomeliaImage): Histogram {
    TODO("Not yet implemented")
}