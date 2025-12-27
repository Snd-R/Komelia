package snd.komelia.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import snd.komelia.image.AndroidBitmap.toBitmap

actual suspend fun KomeliaImage.toImageBitmap(): ImageBitmap {
    return this.toBitmap().asImageBitmap()
}