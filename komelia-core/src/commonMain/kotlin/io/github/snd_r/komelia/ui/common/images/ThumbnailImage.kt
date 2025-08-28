package io.github.snd_r.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision

@Composable
fun ThumbnailImage(
    data: Any,
    cacheKey: String,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: Painter? = NoopPainter,
    modifier: Modifier = Modifier,
) {

    val context = LocalPlatformContext.current
    val request = remember(cacheKey) {
        ImageRequest.Builder(context)
            .data(data)
            .memoryCacheKey(cacheKey)
            .memoryCacheKeyExtra(
                "scale",
                when (contentScale) {
                    ContentScale.Fit -> "Fit"
                    ContentScale.Crop -> "Crop"
                    else -> ""
                }
            )
            .diskCacheKey(cacheKey)
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier,
        placeholder = placeholder,
        contentScale = contentScale,
        filterQuality = FilterQuality.None
    )
}

object NoopPainter : Painter() {
    override val intrinsicSize: Size = Size.Zero
    override fun DrawScope.onDraw() {}
}
