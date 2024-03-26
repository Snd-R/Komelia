package io.github.snd_r.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import io.github.snd_r.komelia.ui.LocalKomgaEvents
import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.sse.KomgaEvent
import kotlin.random.Random

@Composable
fun CollectionThumbnail(
    collectionId: KomgaCollectionId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {

    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(collectionId) { mutableStateOf(CollectionThumbnailRequest(collectionId)) }

    val context = LocalPlatformContext.current
    val request = remember(requestData) {
        ImageRequest.Builder(context)
            .data(requestData)
            .memoryCacheKey(collectionId.value)
            .diskCacheKey(collectionId.value)
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    }

    LaunchedEffect(collectionId) {
        komgaEvents.collect {
            val eventCollectionId = when (it) {
                is KomgaEvent.ThumbnailCollectionEvent -> it.collectionId
                else -> null
            }
            if (eventCollectionId == collectionId) {
                requestData = CollectionThumbnailRequest(collectionId)
            }
        }
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
//        placeholder = NoopPainter
        filterQuality = FilterQuality.None
    )
}

data class CollectionThumbnailRequest(
    val collectionId: KomgaCollectionId,
    val cache: Int = Random.nextInt()
)