package io.github.snd_r.komelia.image

import androidx.compose.ui.graphics.ImageBitmap
import snd.komelia.image.KomeliaImage

expect suspend fun KomeliaImage.toImageBitmap(): ImageBitmap
