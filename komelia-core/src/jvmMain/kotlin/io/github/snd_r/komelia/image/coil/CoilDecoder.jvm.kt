package io.github.snd_r.komelia.image.coil

import coil3.Image
import coil3.asImage
import snd.komelia.image.KomeliaImage
import snd.komelia.image.SkiaBitmap.toSkiaBitmap

actual suspend fun KomeliaImage.toCoilImage(): Image =
    this.toSkiaBitmap().asImage()