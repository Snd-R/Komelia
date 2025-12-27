package snd.komelia.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import snd.komelia.image.SkiaBitmap.toSkiaBitmap

actual suspend fun KomeliaImage.toImageBitmap(): ImageBitmap =
    this.toSkiaBitmap().asComposeImageBitmap()