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
import snd.komga.client.book.KomgaBookId
import snd.komga.client.sse.KomgaEvent.ThumbnailBookEvent
import kotlin.random.Random

@Composable
fun BookThumbnail(
    bookId: KomgaBookId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(bookId) { mutableStateOf(BookThumbnailRequest(bookId)) }
    LaunchedEffect(bookId) {
        komgaEvents.collect {
            val eventBookId = when (it) {
                is ThumbnailBookEvent -> it.bookId
                else -> null
            }
            if (bookId == eventBookId) {
                requestData = BookThumbnailRequest(bookId)
            }
        }
    }

    ThumbnailImage(
        data = requestData,
        cacheKey = bookId.value,
        contentScale = contentScale,
        modifier = modifier
    )

}

data class BookThumbnailRequest(
    val bookId: KomgaBookId,
    val cache: Int = Random.nextInt()
)
