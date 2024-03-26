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
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.sse.KomgaEvent.ThumbnailBookEvent
import kotlin.random.Random

@Composable
fun BookThumbnail(
    bookId: KomgaBookId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(bookId) { mutableStateOf(BookThumbnailRequest(bookId)) }

    val context = LocalPlatformContext.current
    val request = remember(requestData) {
        ImageRequest.Builder(context)
            .data(requestData)
            .memoryCacheKey(bookId.value)
            .diskCacheKey(bookId.value)
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    }

    LaunchedEffect(bookId) {
        komgaEvents.collect {
            val eventBookId = when (it) {
                is ThumbnailBookEvent -> it.bookId
                else -> null
            }
            if (eventBookId == bookId) {
                requestData = BookThumbnailRequest(bookId)
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

data class BookThumbnailRequest(
    val bookId: KomgaBookId,
    val cache: Int = Random.nextInt()
)
