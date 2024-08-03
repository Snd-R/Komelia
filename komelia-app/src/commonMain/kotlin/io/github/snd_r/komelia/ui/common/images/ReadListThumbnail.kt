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
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.sse.KomgaEvent
import kotlin.random.Random

@Composable
fun ReadListThumbnail(
    readListId: KomgaReadListId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {

    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(readListId) { mutableStateOf(ReadListThumbnailRequest(readListId)) }
    LaunchedEffect(readListId) {
        komgaEvents.collect {
            val eventReadListId = when (it) {
                is KomgaEvent.ThumbnailReadListEvent -> it.readListId
                else -> null
            }
            if (eventReadListId == readListId) {
                requestData = ReadListThumbnailRequest(readListId)
            }
        }
    }

    ThumbnailImage(
        data = requestData,
        cacheKey = readListId.value,
        contentScale = contentScale,
        modifier = modifier
    )

}

data class ReadListThumbnailRequest(
    val readListId: KomgaReadListId,
    val cache: Int = Random.nextInt()
)