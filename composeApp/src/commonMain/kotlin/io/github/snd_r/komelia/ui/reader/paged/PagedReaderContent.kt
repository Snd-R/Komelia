package io.github.snd_r.komelia.ui.reader.paged

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.LayoutDirection
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.common.PageSpreadProgressSlider
import io.github.snd_r.komelia.ui.reader.common.PagedReaderHelpDialog
import io.github.snd_r.komelia.ui.reader.common.ReaderControlsOverlay
import io.github.snd_r.komelia.ui.reader.common.ScalableContainer
import io.github.snd_r.komelia.ui.reader.common.SettingsMenu
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.Page
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komga.book.KomgaBook
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun BoxScope.PagedReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,

    screenScaleState: ScreenScaleState,
    pagedReaderState: PagedReaderState,
    readerState: ReaderState,

    book: KomgaBook?,
    onBookBackClick: () -> Unit,
    onSeriesBackClick: () -> Unit
) {
    if (showHelpDialog)
        PagedReaderHelpDialog(onDismissRequest = { onShowHelpDialogChange(false) })

    val readingDirection = pagedReaderState.readingDirection.collectAsState().value
    val layoutDirection = when (readingDirection) {
        LEFT_TO_RIGHT -> LayoutDirection.Ltr
        RIGHT_TO_LEFT -> LayoutDirection.Rtl
    }

    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(readingDirection, pagedReaderState.layoutOffset.value) {
        registerPagedReaderKeyboardEvents(
            keyEvents = keyEvents,
            pageState = pagedReaderState,
        )
    }

    val currentContainerSize = screenScaleState.areaSize.collectAsState().value
    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = pagedReaderState::nextPage,
        onPrevPageClick = pagedReaderState::previousPage,
        contentAreaSize = currentContainerSize,
        isSettingsMenuOpen = showSettingsMenu,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
    ) {
        ScalableContainer(scaleState = screenScaleState) {
            ReaderPages(
                pagedReaderState.currentSpread.collectAsState().value.pages,
                readingDirection
            )
        }
    }

    SettingsMenu(
        book = book,
        onMenuDismiss = { onShowSettingsMenuChange(!showSettingsMenu) },
        onShowHelpMenu = { onShowHelpDialogChange(true) },
        show = showSettingsMenu,
        settingsState = readerState,
        screenScaleState = screenScaleState,
        onSeriesPress = onSeriesBackClick,
        onBookClick = onBookBackClick,
        readerSettingsContent = { PagedReaderSettingsContent(pagedReaderState) }
    )


    PageSpreadProgressSlider(
        pageSpreads = pagedReaderState.pageSpreads.collectAsState().value,
        currentSpreadIndex = pagedReaderState.currentSpreadIndex.collectAsState().value,
        onPageNumberChange = pagedReaderState::onPageChange,
        show = showSettingsMenu,
        layoutDirection = layoutDirection,
        modifier = Modifier.align(Alignment.BottomStart),
    )
}

@Composable
fun ReaderPages(
    currentPages: List<Page>,
    readingDirection: PagedReaderState.ReadingDirection,
) {
    val pages = when (readingDirection) {
        LEFT_TO_RIGHT -> currentPages
        RIGHT_TO_LEFT -> currentPages.reversed()
    }

    Box(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            pages.forEach { page ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f, false)
                ) {
                    when (val result = page.imageResult) {
                        is PagedReaderState.ImageResult.Success -> Image(
                            modifier = Modifier.background(Color.White),
                            painter = result.image.painter.collectAsState().value,
                            contentDescription = null,
                        )

                        is PagedReaderState.ImageResult.Error -> Text(
                            "Error :${result.throwable.message}",
                            color = MaterialTheme.colorScheme.error
                        )

                        null -> Box(
                            modifier = Modifier.fillMaxSize().background(Color.White),
                            contentAlignment = Alignment.TopCenter,
                            content = { CircularProgressIndicator(color = Color.Black) }
                        )

                    }
                }

            }
        }
    }
}

private suspend fun registerPagedReaderKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    pageState: PagedReaderState,
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
