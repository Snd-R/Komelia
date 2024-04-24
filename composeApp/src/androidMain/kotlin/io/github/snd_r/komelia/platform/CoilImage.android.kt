package io.github.snd_r.komelia.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun ReaderImage(
    image: Image,
    contentScale: ContentScale,
    modifier: Modifier
) {
    val context = LocalContext.current
    Image(
        modifier = modifier,
        bitmap = image.asDrawable(context.resources).toBitmap().asImageBitmap(),
        contentDescription = null,
        filterQuality = FilterQuality.None,
        contentScale = contentScale
    )
}
