package io.github.snd_r.komelia.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun ReaderImage(image: Image) {
    Image(
        bitmap = image.toBitmap().asComposeImageBitmap(),
        contentDescription = null,
        filterQuality = FilterQuality.None
    )
}