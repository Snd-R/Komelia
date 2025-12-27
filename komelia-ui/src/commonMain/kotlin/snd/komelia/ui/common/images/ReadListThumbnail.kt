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
import snd.komelia.image.coil.ReadListDefaultThumbnailRequest
import snd.komelia.ui.LocalKomgaEvents
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.sse.KomgaEvent.ThumbnailReadListEvent

@Composable
fun ReadListThumbnail(
    readListId: KomgaReadListId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {

    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(readListId) { mutableStateOf(ReadListDefaultThumbnailRequest(readListId)) }
    LaunchedEffect(readListId) {
        komgaEvents.filterIsInstance<ThumbnailReadListEvent>().collect {
            if (it.readListId == readListId) requestData = ReadListDefaultThumbnailRequest(readListId)
        }
    }

    ThumbnailImage(
        data = requestData,
        cacheKey = readListId.value,
        contentScale = contentScale,
        modifier = modifier
    )

}
