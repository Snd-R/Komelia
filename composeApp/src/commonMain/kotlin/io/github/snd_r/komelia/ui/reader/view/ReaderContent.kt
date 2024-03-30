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
import io.github.snd_r.komelia.ui.reader.ReaderPageState
import io.github.snd_r.komelia.ui.reader.ReaderSettingsState
import io.github.snd_r.komelia.ui.reader.ReaderZoomState
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komelia.ui.reader.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komga.book.KomgaBook
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun PagedReaderContent(
    book: KomgaBook,
    pageState: ReaderPageState,
    zoomState: ReaderZoomState,
    settingsState: ReaderSettingsState,

    onSeriesBackClick: () -> Unit,
    onBookBackClick: () -> Unit,
) {
    var showNavMenu by remember { mutableStateOf(false) }
    var showNavHelpDialog by remember { mutableStateOf(false) }

    var isCtrlPressed by remember { mutableStateOf(false) }

    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(settingsState.readingDirection) {
        registerKeyboardEvents(
            keyEvents = keyEvents,
            isCtrlPressed = { isCtrlPressed = it },
            settingsState = settingsState,
            pageState = pageState,
            onNavMenuToggle = { showNavMenu = !showNavMenu },
            onShowHelpDialog = { showNavHelpDialog = !showNavHelpDialog },
            onClose = onSeriesBackClick
        )
    }

    Box(Modifier.onSizeChanged { zoomState.onContentSizeChange(it) }) {
        ControlsOverlay(
            readingDirection = settingsState.readingDirection,
            onNexPageClick = pageState::nextPage,
            onPrevPageClick = pageState::previousPage,
            contentAreaSize = zoomState.containerSize.collectAsState().value ?: IntSize.Zero,
            onNavMenuToggle = { showNavMenu = !showNavMenu },
        ) {
            ScalableContainer(
                zoomState = zoomState,
                isCtrlPressed = isCtrlPressed,
            ) {
                ReaderPages(
                    pageState.currentSpread.collectAsState().value.pages,
                    settingsState.readingDirection
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
            readingDirection = settingsState.readingDirection,
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
    settingsState: ReaderSettingsState,
    pageState: ReaderPageState,
    onNavMenuToggle: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onClose: () -> Unit,
) {
    val readingDirection = settingsState.readingDirection
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
            Key.L -> settingsState.onReadingDirectionChange(LEFT_TO_RIGHT)
            Key.R -> settingsState.onReadingDirectionChange(RIGHT_TO_LEFT)
            Key.C -> settingsState.onScaleTypeCycle()
            Key.D -> settingsState.onLayoutCycle()
            Key.O -> settingsState.onLayoutOffsetChange(!settingsState.layoutOffset)
            Key.M -> onNavMenuToggle()
            Key.H -> onShowHelpDialog()
            Key.Backspace -> onClose()
            else -> {}
        }
    }
}