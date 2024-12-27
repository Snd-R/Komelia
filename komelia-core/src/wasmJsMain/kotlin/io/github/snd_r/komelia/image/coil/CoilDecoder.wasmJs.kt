package io.github.snd_r.komelia.image.coil

import coil3.Image
import coil3.asImage
import io.github.snd_r.komelia.image.toBitmap
import snd.komelia.image.KomeliaImage

actual suspend fun KomeliaImage.toCoilImage(): Image {
    return this.toBitmap().asImage()
}