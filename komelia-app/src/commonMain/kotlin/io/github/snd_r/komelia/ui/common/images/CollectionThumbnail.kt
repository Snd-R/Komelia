package io.github.snd_r.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.snd_r.komelia.ui.LocalKomgaEvents
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.sse.KomgaEvent
import kotlin.random.Random

@Composable
fun CollectionThumbnail(
    collectionId: KomgaCollectionId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(collectionId) { mutableStateOf(CollectionThumbnailRequest(collectionId)) }

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

    ThumbnailImage(
        data = requestData,
        cacheKey = collectionId.value,
        contentScale = contentScale,
        modifier = modifier
    )
}

data class CollectionThumbnailRequest(
    val collectionId: KomgaCollectionId,
    val cache: Int = Random.nextInt()
)