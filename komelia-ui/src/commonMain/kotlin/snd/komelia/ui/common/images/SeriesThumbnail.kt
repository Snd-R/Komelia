package snd.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import snd.komelia.image.coil.SeriesDefaultThumbnailRequest
import snd.komelia.ui.LocalKomgaEvents
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent.ThumbnailBookEvent
import snd.komga.client.sse.KomgaEvent.ThumbnailSeriesEvent

@Composable
fun SeriesThumbnail(
    seriesId: KomgaSeriesId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(seriesId) { mutableStateOf(SeriesDefaultThumbnailRequest(seriesId)) }

    LaunchedEffect(seriesId) {
        komgaEvents.collect {
            val eventSeriesId = when (it) {
                is ThumbnailSeriesEvent -> it.seriesId
                is ThumbnailBookEvent -> it.seriesId
                else -> null
            }
            if (eventSeriesId == seriesId) {
                requestData = SeriesDefaultThumbnailRequest(seriesId)
            }
        }
    }
    ThumbnailImage(
        data = requestData,
        cacheKey = seriesId.value,
        contentScale = contentScale,
        modifier = modifier
    )
}

