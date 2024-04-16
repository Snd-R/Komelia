package io.github.snd_r.komelia.ui.reader.view

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.PagedReaderPageState
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.reader.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.ReadingDirection.RIGHT_TO_LEFT
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun PagedReaderContent(
    pageState: PagedReaderPageState,
    settingsState: ReaderState,

    onSeriesBackClick: () -> Unit,
    onBookBackClick: () -> Unit,
) {
    val book = settingsState.bookState.collectAsState().value?.book

    var showNavMenu by remember { mutableStateOf(false) }
    var showNavHelpDialog by remember { mutableStateOf(false) }

    var isCtrlPressed by remember { mutableStateOf(false) }

    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    val readingDirection = pageState.readingDirection.collectAsState()
    LaunchedEffect(readingDirection.value) {
        registerKeyboardEvents(
            keyEvents = keyEvents,
            isCtrlPressed = { isCtrlPressed = it },
            pageState = pageState,
            onNavMenuToggle = { showNavMenu = !showNavMenu },
            onShowHelpDialog = { showNavHelpDialog = !showNavHelpDialog },
            onClose = onSeriesBackClick
        )
    }

    Box(Modifier.onSizeChanged { pageState.onContentSizeChange(it) }) {
        ControlsOverlay(
            readingDirection = readingDirection.value,
            onNexPageClick = pageState::nextPage,
            onPrevPageClick = pageState::previousPage,
            contentAreaSize = pageState.containerSize.collectAsState().value ?: IntSize.Zero,
            onNavMenuToggle = { showNavMenu = !showNavMenu },
        ) {
            ScalableContainer(
                pageState = pageState,
                isCtrlPressed = isCtrlPressed,
            ) {
                ReaderPages(
                    pageState.currentSpread.collectAsState().value.pages,
                    readingDirection.value
                )
            }
        }

        if (showNavMenu) {
            SettingsMenu(
                book = book,
                onMenuDismiss = { showNavMenu = !showNavMenu },
                onShowHelpMenu = { showNavHelpDialog = true },
                settingsState = settingsState,
                pageState = pageState,
                onSeriesPress = onSeriesBackClick,
                onBookClick = onBookBackClick,
            )
        }

        if (showNavHelpDialog) {
            NavigationHelpDialog(onDismissRequest = { showNavHelpDialog = false })
        }

        ProgressSlider(
            pageSpreads = pageState.pageSpreads.collectAsState().value,
            currentSpreadIndex = pageState.currentSpreadIndex.collectAsState().value,
            onPageNumberChange = pageState::onPageChange,
            hidden = !showNavMenu,
            readingDirection = readingDirection.value,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun ControlsOverlay(
    readingDirection: ReadingDirection,
    onNexPageClick: () -> Unit,
    onPrevPageClick: () -> Unit,
    onNavMenuToggle: () -> Unit,
    contentAreaSize: IntSize,
    content: @Composable () -> Unit,
) {
    val leftAction = { if (readingDirection == LEFT_TO_RIGHT) onPrevPageClick() else onNexPageClick() }
    val centerAction = { onNavMenuToggle() }
    val rightAction = { if (readingDirection == LEFT_TO_RIGHT) onNexPageClick() else onPrevPageClick() }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(contentAreaSize, readingDirection) {
                detectTapGestures { offset ->
                    val actionWidth = contentAreaSize.width.toFloat() / 3
                    when (offset.x) {
                        in 0f..<actionWidth -> leftAction()
                        in actionWidth..actionWidth * 2 -> centerAction()
                        else -> rightAction()
                    }
                }
            }
    ) {
        content()
    }
}

private suspend fun registerKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    isCtrlPressed: (isPressed: Boolean) -> Unit,
    pageState: PagedReaderPageState,
    onNavMenuToggle: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onClose: () -> Unit,
) {
    val readingDirection = pageState.readingDirection.value
    val layoutOffset = pageState.layoutOffset.value
    val previousPage = { if (readingDirection == LEFT_TO_RIGHT) pageState.previousPage() else pageState.nextPage() }
    val nextPage = { if (readingDirection == LEFT_TO_RIGHT) pageState.nextPage() else pageState.previousPage() }
    keyEvents.collect { event ->
        isCtrlPressed(event.isCtrlPressed)
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
            Key.M -> onNavMenuToggle()
            Key.H -> onShowHelpDialog()
            Key.Backspace -> onClose()
            else -> {}
        }
    }
}