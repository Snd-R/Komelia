package snd.komelia.image

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun KomeliaImage.toImageBitmap(): ImageBitmap
