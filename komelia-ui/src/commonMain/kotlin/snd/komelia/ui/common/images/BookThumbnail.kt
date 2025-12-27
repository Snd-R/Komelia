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
import snd.komelia.image.coil.BookDefaultThumbnailRequest
import snd.komelia.ui.LocalKomgaEvents
import snd.komga.client.book.KomgaBookId
import snd.komga.client.sse.KomgaEvent.ThumbnailBookEvent

@Composable
fun BookThumbnail(
    bookId: KomgaBookId,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val komgaEvents = LocalKomgaEvents.current
    var requestData by remember(bookId) { mutableStateOf(BookDefaultThumbnailRequest(bookId)) }

    LaunchedEffect(bookId) {
        komgaEvents.filterIsInstance<ThumbnailBookEvent>().collect {
            if (bookId == it.bookId) {
                requestData = BookDefaultThumbnailRequest(bookId)
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

