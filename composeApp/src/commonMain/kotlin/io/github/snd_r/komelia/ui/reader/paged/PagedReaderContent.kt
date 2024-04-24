package io.github.snd_r.komelia.ui.reader.paged

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun PagedReaderContent(
    pageState: PagedReaderPageState,
) {

    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    val readingDirection = pageState.readingDirection.collectAsState()
    LaunchedEffect(readingDirection.value, pageState.layoutOffset.value) {
        registerPagedReaderKeyboardEvents(
            keyEvents = keyEvents,
            pageState = pageState,
        )
    }
    ReaderPages(
        pageState.currentSpread.collectAsState().value.pages,
        readingDirection.value
    )
}

private suspend fun registerPagedReaderKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    pageState: PagedReaderPageState,
) {
    val readingDirection = pageState.readingDirection.value
    val layoutOffset = pageState.layoutOffset.value
    val previousPage = {
        if (readingDirection == LEFT_TO_RIGHT) pageState.previousPage()
        else pageState.nextPage()
    }
    val nextPage = {
        if (readingDirection == LEFT_TO_RIGHT) pageState.nextPage()
        else pageState.previousPage()
    }
    keyEvents.collect { event ->
        if (event.type != KeyUp) return@collect

        when (event.key) {
            Key.DirectionLeft -> previousPage()
            Key.DirectionRight -> nextPage()
            Key.MoveHome -> pageState.onPageChange(0)
            Key.MoveEnd -> pageState.onPageChange(pageState.pageSpreads.value.size - 1)
            Key.L -> pageState.onReadingDirectionChange(LEFT_TO_RIGHT)
            Key.R -> pageState.onReadingDirectionChange(RIGHT_TO_LEFT)
            Key.C -> pageState.onScaleTypeCycle()
            Key.D -> pageState.onLayoutCycle()
            Key.O -> pageState.onLayoutOffsetChange(!layoutOffset)
            else -> {}
        }
    }
}
