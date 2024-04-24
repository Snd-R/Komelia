package io.github.snd_r.komelia.ui.reader.paged

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import io.github.snd_r.komelia.platform.ReaderImage
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.Page


@Composable
fun ReaderPages(
    currentPages: List<Page>,
    readingDirection: PagedReaderState.ReadingDirection,
) {
    val pages = when (readingDirection) {
        PagedReaderState.ReadingDirection.LEFT_TO_RIGHT -> currentPages
        PagedReaderState.ReadingDirection.RIGHT_TO_LEFT -> currentPages.reversed()
    }

    Box(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            pages.forEach {
                ReaderPage(it, Modifier.weight(1f, false))

            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ReaderPage(
    page: Page,
    modifier: Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    )
    {
        when (val result = page.imageResult) {
            is SuccessResult -> ReaderImage(result.image)
            is ErrorResult -> Text("Error :${result.throwable.message}", color = MaterialTheme.colorScheme.error)
            null -> {
                LoadingMaxSizeIndicator()
            }
        }
    }
}
