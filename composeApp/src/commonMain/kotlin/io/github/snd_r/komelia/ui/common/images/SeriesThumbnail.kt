package io.github.snd_r.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import io.github.snd_r.komelia.ui.LocalKomgaEvents
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailBookEvent
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailSeriesEvent
import kotlin.random.Random

@Composable
fun SeriesThumbnail(
    seriesId: KomgaSeriesId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(seriesId) { mutableStateOf(SeriesThumbnailRequest(seriesId)) }

    val context = LocalPlatformContext.current
    val request = remember(requestData) {
        ImageRequest.Builder(context)
            .data(requestData)
            .memoryCacheKey(seriesId.value)
            .diskCacheKey(seriesId.value)
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    }

    LaunchedEffect(seriesId) {
        komgaEvents.collect {
            val eventSeriesId = when (it) {
                is ThumbnailSeriesEvent -> it.seriesId
                is ThumbnailBookEvent -> it.seriesId
                else -> null
            }
            if (eventSeriesId == seriesId) {
                requestData = SeriesThumbnailRequest(seriesId)
            }
        }
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier,
        placeholder = NoopPainter,
        contentScale = contentScale,
        filterQuality = FilterQuality.None
    )

}

data class SeriesThumbnailRequest(
    val seriesId: KomgaSeriesId,
    val cache: Int = Random.nextInt()
)

object NoopPainter : Painter() {
    override val intrinsicSize: Size = Size.Zero
    override fun DrawScope.onDraw() {}
}