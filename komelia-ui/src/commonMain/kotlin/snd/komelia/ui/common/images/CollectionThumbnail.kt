package snd.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.flow.filterIsInstance
import snd.komelia.image.coil.CollectionDefaultThumbnailRequest
import snd.komelia.ui.LocalKomgaEvents
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.sse.KomgaEvent.ThumbnailCollectionEvent

@Composable
fun CollectionThumbnail(
    collectionId: KomgaCollectionId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(collectionId) { mutableStateOf(CollectionDefaultThumbnailRequest(collectionId)) }

    LaunchedEffect(collectionId) {
        komgaEvents.filterIsInstance<ThumbnailCollectionEvent>().collect {
            if (it.collectionId == collectionId) requestData = CollectionDefaultThumbnailRequest(collectionId)
        }
    }

    ThumbnailImage(
        data = requestData,
        cacheKey = collectionId.value,
        contentScale = contentScale,
        modifier = modifier
    )
}
