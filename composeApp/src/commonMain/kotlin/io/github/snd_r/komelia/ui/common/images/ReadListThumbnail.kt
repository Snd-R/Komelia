package io.github.snd_r.komelia.ui.common.images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.snd_r.komelia.ui.LocalKomgaEvents
import io.github.snd_r.komga.readlist.KomgaReadListId
import io.github.snd_r.komga.sse.KomgaEvent
import kotlin.random.Random

@Composable
fun ReadListThumbnail(
    readListId: KomgaReadListId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {

    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(readListId) { mutableStateOf(ReadListThumbnailRequest(readListId)) }

    val context = LocalPlatformContext.current
    val request = remember(requestData) {
        ImageRequest.Builder(context)
            .data(requestData)
            .memoryCacheKey(readListId.value)
            .diskCacheKey(readListId.value)
            .crossfade(true)
            .build()
    }

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

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
    )
}

data class ReadListThumbnailRequest(
    val readListId: KomgaReadListId,
    val cache: Int = Random.nextInt()
)