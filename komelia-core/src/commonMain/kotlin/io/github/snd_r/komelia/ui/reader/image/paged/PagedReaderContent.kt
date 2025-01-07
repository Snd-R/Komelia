package io.github.snd_r.komelia.ui.reader.image.paged

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.image.ReaderImage
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.image.ReaderImageResult
import io.github.snd_r.komelia.ui.reader.image.ReaderState
import io.github.snd_r.komelia.ui.reader.image.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.image.common.PageSpreadProgressSlider
import io.github.snd_r.komelia.ui.reader.image.common.PagedReaderHelpDialog
import io.github.snd_r.komelia.ui.reader.image.common.ReaderControlsOverlay
import io.github.snd_r.komelia.ui.reader.image.common.ScalableContainer
import io.github.snd_r.komelia.ui.reader.image.common.SettingsMenu
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.Page
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage.BookEnd
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage.BookStart
import kotlinx.coroutines.flow.SharedFlow
import snd.komga.client.book.KomgaBook

@Composable
fun BoxScope.PagedReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,
    expandImageSettings: Boolean,
    onExpandImageSettingsChange: (Boolean) -> Unit,

    screenScaleState: ScreenScaleState,
    pagedReaderState: PagedReaderState,
    readerState: ReaderState,

    book: KomgaBook?,
    onBookBackClick: () -> Unit,
    onSeriesBackClick: () -> Unit,
    isColorCurvesActive: Boolean,
    onColorCurvesClick: () -> Unit,
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
            val transitionPage = pagedReaderState.transitionPage.collectAsState().value
            if (transitionPage != null) {
                TransitionPage(transitionPage)
            } else {
                ReaderPages(
                    currentPages = pagedReaderState.currentSpread.collectAsState().value.pages,
                    readingDirection = readingDirection,
                )
            }
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
        expandImageSettings = expandImageSettings,
        onExpandImageSettingsChange = onExpandImageSettingsChange,
        isColorCorrectionsActive = isColorCurvesActive,
        onColorCorrectionClick = onColorCurvesClick,
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
private fun TransitionPage(page: TransitionPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (page) {
            is BookEnd -> {
                Column {
                    Text("Finished:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        page.currentBook.metadata.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(Modifier.size(50.dp))

                if (page.nextBook != null) {
                    Column {
                        Text("Next:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            page.nextBook.metadata.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                } else {
                    Text("There's no next book")
                }

            }

            is BookStart -> {
                if (page.previousBook != null) {
                    Column {
                        Text("Previous:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            page.previousBook.metadata.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                } else {
                    Text("There's no previous book")

                }
                Spacer(Modifier.size(50.dp))
                Column {
                    Text("Current:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        page.currentBook.metadata.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

            }
        }
    }
}

@Composable
fun ReaderPages(
    currentPages: List<Page>,
    readingDirection: ReadingDirection,
) {
    Layout(content = {
        when (currentPages.size) {
            0 -> {}
            1 -> PageContent(currentPages[0].imageResult)
            2 -> {
                PageContent(currentPages[0].imageResult)
                PageContent(currentPages[1].imageResult)
            }

            else -> error("can't display more than 2 images")
        }
    }) { measurables, constraints ->
        val measured = measurables
            .map { it.measure(constraints.copy(maxWidth = constraints.maxWidth / measurables.size)) }
            .let {
                when (readingDirection) {
                    LEFT_TO_RIGHT -> it
                    RIGHT_TO_LEFT -> it.reversed()
                }
            }

        val totalWidth = measured.fold(0) { acc, placeable -> acc + placeable.width }
        val startPadding = (constraints.maxWidth - totalWidth) / 2

        var widthTaken = startPadding
        layout(constraints.maxWidth, constraints.maxHeight) {
            measured.forEach {
                val topPadding = ((constraints.maxHeight - it.height) / 2).coerceAtLeast(0)
                it.placeRelative(widthTaken, topPadding)
                widthTaken += it.width
            }
        }
    }
}

@Composable
private fun PageContent(imageResult: ReaderImageResult?) {
    when (imageResult) {
        is ReaderImageResult.Success -> PageImageContent(imageResult.image)
        is ReaderImageResult.Error -> Text(
            "${imageResult.throwable::class.simpleName}: ${imageResult.throwable.message}",
            color = MaterialTheme.colorScheme.error
        )

        null -> Box(
            modifier = Modifier.widthIn(min = 200.dp).fillMaxHeight(),
            contentAlignment = Alignment.Center,
            content = { CircularProgressIndicator() }
        )
    }
}

@Composable
private fun PageImageContent(image: ReaderImage) {
    val painter = image.painter.collectAsState().value
    val error = image.error.collectAsState().value

    if (error != null) {
        Text(
            "${error::class.simpleName}: ${error.message}",
            color = MaterialTheme.colorScheme.error
        )
    } else {
        Image(
            modifier = Modifier.background(Color.White),
            painter = painter,
            contentDescription = null,
        )
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
