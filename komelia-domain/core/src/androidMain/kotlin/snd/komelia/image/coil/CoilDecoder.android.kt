package snd.komelia.image.coil

import coil3.Image
import coil3.asImage
import snd.komelia.image.AndroidBitmap.toBitmap
import snd.komelia.image.KomeliaImage

actual suspend fun KomeliaImage.toCoilImage(): Image {
    return this.toBitmap().asImage()
}