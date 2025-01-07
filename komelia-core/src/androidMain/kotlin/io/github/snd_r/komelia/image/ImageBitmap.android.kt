package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import snd.komelia.image.AndroidBitmap.toBitmap
import snd.komelia.image.KomeliaImage

actual suspend fun KomeliaImage.toImageBitmap(): ImageBitmap {
    return this.toBitmap().asImageBitmap()
}