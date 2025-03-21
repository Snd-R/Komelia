package io.github.snd_r.komelia.ui.reader.image.paged

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.reader.image.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.image.common.PagedReaderHelpDialog
import io.github.snd_r.komelia.ui.reader.image.common.ReaderControlsOverlay
import io.github.snd_r.komelia.ui.reader.image.common.ReaderImageContent
import io.github.snd_r.komelia.ui.reader.image.common.ScalableContainer
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.Page
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.DOUBLE_PAGES
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.DOUBLE_PAGES_NO_COVER
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout.SINGLE_PAGE
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage.BookEnd
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.TransitionPage.BookStart

@Composable
fun BoxScope.PagedReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,
    screenScaleState: ScreenScaleState,
    pagedReaderState: PagedReaderState,
    volumeKeysNavigation: Boolean
) {
    if (showHelpDialog) {
        PagedReaderHelpDialog(onDismissRequest = { onShowHelpDialogChange(false) })
    }

    val readingDirection = pagedReaderState.readingDirection.collectAsState().value
    val layoutDirection = when (readingDirection) {
        LEFT_TO_RIGHT -> LayoutDirection.Ltr
        RIGHT_TO_LEFT -> LayoutDirection.Rtl
    }
    val pages = pagedReaderState.currentSpread.collectAsState().value.pages
    val layout = pagedReaderState.layout.collectAsState().value
    val layoutOffset = pagedReaderState.layoutOffset.collectAsState().value

    val currentContainerSize = screenScaleState.areaSize.collectAsState().value
    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = pagedReaderState::nextPage,
        onPrevPageClick = pagedReaderState::previousPage,
        contentAreaSize = currentContainerSize,
        isSettingsMenuOpen = showSettingsMenu,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
        modifier = Modifier.onKeyEvent { event ->
            pagedReaderOnKeyEvents(
                event = event,
                readingDirection = readingDirection,
                layoutOffset = layoutOffset,
                onReadingDirectionChange = pagedReaderState::onReadingDirectionChange,
                onScaleTypeCycle = pagedReaderState::onScaleTypeCycle,
                onLayoutCycle = pagedReaderState::onLayoutCycle,
                onChangeLayoutOffset = pagedReaderState::onLayoutOffsetChange,
                onPageChange = pagedReaderState::onPageChange,
                onMoveToLastPage = pagedReaderState::moveToLastPage,
                onMoveToNextPage = pagedReaderState::nextPage,
                onMoveToPrevPage = pagedReaderState::previousPage,
                volumeKeysNavigation = volumeKeysNavigation
            )
        }
    ) {
        ScalableContainer(scaleState = screenScaleState) {
            val transitionPage = pagedReaderState.transitionPage.collectAsState().value
            if (transitionPage != null) {
                TransitionPage(transitionPage)
            } else {
                when (layout) {
                    SINGLE_PAGE -> pages.firstOrNull()?.let { SinglePageLayout(it) }
                    DOUBLE_PAGES, DOUBLE_PAGES_NO_COVER -> DoublePageLayout(pages, readingDirection)
                }
            }
        }
    }
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
private fun SinglePageLayout(page: Page) {
    Layout(content = { ReaderImageContent(page.imageResult) }) { measurable, constraints ->
        val placeable = measurable.first().measure(constraints)
        val startPadding = (constraints.maxWidth - placeable.width) / 2
        val topPadding = ((constraints.maxHeight - placeable.height) / 2).coerceAtLeast(0)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(startPadding, topPadding)
        }
    }
}

@Composable
private fun DoublePageLayout(
    pages: List<Page>,
    readingDirection: ReadingDirection,
) {
    Layout(content = {
        when (pages.size) {
            0 -> {}
            1 -> ReaderImageContent(pages.first().imageResult)
            2 -> {
                ReaderImageContent(pages[0].imageResult)
                ReaderImageContent(pages[1].imageResult)
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
        val startPadding: Int
        if (measured.size == 1 && !pages.first().metadata.isLandscape()) {
            startPadding = when (readingDirection) {
                LEFT_TO_RIGHT -> (constraints.maxWidth - (measured.first().width * 2)) / 2
                RIGHT_TO_LEFT -> ((constraints.maxWidth - (measured.first().width * 2)) / 2) + measured.first().width
            }
        } else {
            val totalWidth = measured.fold(0) { acc, placeable -> acc + placeable.width }
            startPadding = (constraints.maxWidth - totalWidth) / 2
        }

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

private fun pagedReaderOnKeyEvents(
    event: KeyEvent,
    readingDirection: ReadingDirection,
    layoutOffset: Boolean,
    onReadingDirectionChange: (ReadingDirection) -> Unit,
    onScaleTypeCycle: () -> Unit,
    onLayoutCycle: () -> Unit,
    onChangeLayoutOffset: (Boolean) -> Unit,
    onPageChange: (Int) -> Unit,
    onMoveToLastPage: () -> Unit,
    onMoveToNextPage: () -> Unit,
    onMoveToPrevPage: () -> Unit,
    volumeKeysNavigation: Boolean,
): Boolean {
    if (event.type != KeyUp) {
        return volumeKeysNavigation && (event.key == Key.VolumeUp || event.key == Key.VolumeDown)
    }

    val previousPage = {
        if (readingDirection == LEFT_TO_RIGHT) onMoveToPrevPage()
        else onMoveToNextPage()
    }
    val nextPage = {
        if (readingDirection == LEFT_TO_RIGHT) onMoveToNextPage()
        else onMoveToPrevPage()
    }

    var consumed = true
    when (event.key) {
        Key.DirectionLeft -> {
            previousPage()
            if (event.isAltPressed) consumed = false
        }

        Key.DirectionRight -> nextPage()
        Key.MoveHome -> onPageChange(0)
        Key.MoveEnd -> onMoveToLastPage()
        Key.L -> onReadingDirectionChange(LEFT_TO_RIGHT)
        Key.R -> onReadingDirectionChange(RIGHT_TO_LEFT)
        Key.C -> if (event.isAltPressed) consumed = false else onScaleTypeCycle()
        Key.D -> onLayoutCycle()
        Key.O -> onChangeLayoutOffset(!layoutOffset)
        Key.VolumeUp -> if (volumeKeysNavigation) previousPage() else consumed = false
        Key.VolumeDown -> if (volumeKeysNavigation) nextPage() else consumed = false
        else -> consumed = false
    }
    return consumed
}
