package io.github.snd_r.komelia.ui.reader.common

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderContent
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderSettingsContent
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderContent
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderPageState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderSettingsContent
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komga.book.KomgaBook
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun ReaderContent(
    pageState: PagedReaderPageState,
    continuousReaderState: ContinuousReaderState,
    settingsState: ReaderState,
    screenScaleState: ScreenScaleState,

    onSeriesBackClick: () -> Unit,
    onBookBackClick: () -> Unit,
) {
    val book = settingsState.bookState.collectAsState().value?.book
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(Unit) {
        registerCommonKeyboardEvents(
            keyEvents = keyEvents,
            showSettingsMenu = showSettingsMenu,
            setShowSettingsDialog = { showSettingsMenu = it },
            onShowHelpDialog = { showHelpDialog = !showHelpDialog },
            onClose = onSeriesBackClick
        )
    }

    Box(Modifier.fillMaxSize().onSizeChanged { screenScaleState.setAreaSize(it) }) {
        val currentContainerSize = screenScaleState.areaSize.collectAsState()
        if (currentContainerSize.value == IntSize.Zero) return

        when (settingsState.readerType.collectAsState().value) {
            PAGED -> PagedReaderContent(
                showHelpDialog = showHelpDialog,
                onShowHelpDialogChange = { showHelpDialog = it },
                showSettingsMenu = showSettingsMenu,
                onShowSettingsMenuChange = { showSettingsMenu = it },

                screenScaleState = screenScaleState,
                pagedReaderState = pageState,
                readerState = settingsState,

                book = book,
                onBookBackClick = onBookBackClick,
                onSeriesBackClick = onSeriesBackClick,
            )


            CONTINUOUS -> ContinuousReaderContent(
                showHelpDialog = showHelpDialog,
                onShowHelpDialogChange = { showHelpDialog = it },

                showSettingsMenu = showSettingsMenu,
                onShowSettingsMenuChange = { showSettingsMenu = it },

                screenScaleState = screenScaleState,
                continuousReaderState = continuousReaderState,
                readerState = settingsState,

                book = book,
                onBookBackClick = onBookBackClick,
                onSeriesBackClick = onSeriesBackClick,
            )
        }

    }
}

@Composable
private fun BoxScope.PagedReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,

    screenScaleState: ScreenScaleState,
    pagedReaderState: PagedReaderPageState,
    readerState: ReaderState,

    book: KomgaBook?,
    onBookBackClick: () -> Unit,
    onSeriesBackClick: () -> Unit
) {
    val currentContainerSize = screenScaleState.areaSize.collectAsState().value
    if (currentContainerSize == IntSize.Zero) return

    if (showHelpDialog) {
        PagedReaderHelpDialog(onDismissRequest = { onShowHelpDialogChange(false) })
    }

    val layoutDirection = when (pagedReaderState.readingDirection.collectAsState().value) {
        PagedReaderState.ReadingDirection.LEFT_TO_RIGHT -> LayoutDirection.Ltr
        PagedReaderState.ReadingDirection.RIGHT_TO_LEFT -> LayoutDirection.Rtl
    }
    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = pagedReaderState::nextPage,
        onPrevPageClick = pagedReaderState::previousPage,
        contentAreaSize = currentContainerSize,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
    ) {
        ScalableContainer(scaleState = screenScaleState) { PagedReaderContent(pageState = pagedReaderState) }
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
private fun BoxScope.ContinuousReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,

    screenScaleState: ScreenScaleState,
    continuousReaderState: ContinuousReaderState,
    readerState: ReaderState,

    book: KomgaBook?,
    onBookBackClick: () -> Unit,
    onSeriesBackClick: () -> Unit
) {
    val areaSize = screenScaleState.areaSize.collectAsState().value
    if (areaSize == IntSize.Zero) return

    val coroutineScope = rememberCoroutineScope()
    val readingDirection = continuousReaderState.readingDirection.collectAsState().value

    val layoutDirection = remember(readingDirection) {
        when (readingDirection) {
            ReadingDirection.TOP_TO_BOTTOM -> LayoutDirection.Ltr
            ReadingDirection.LEFT_TO_RIGHT -> LayoutDirection.Ltr
            ReadingDirection.RIGHT_TO_LEFT -> LayoutDirection.Rtl
        }
    }
    val orientation = remember(readingDirection) {
        when (readingDirection) {
            ReadingDirection.TOP_TO_BOTTOM -> Orientation.Vertical
            ReadingDirection.LEFT_TO_RIGHT, ReadingDirection.RIGHT_TO_LEFT -> Orientation.Horizontal
        }
    }

    if (showHelpDialog) {
        ContinuousReaderHelpDialog(
            orientation = orientation,
            onDismissRequest = { onShowHelpDialogChange(false) }
        )
    }

    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = {
            when (orientation) {
                Orientation.Vertical -> continuousReaderState.scrollForward(areaSize.height.toFloat())
                Orientation.Horizontal -> continuousReaderState.scrollForward(areaSize.width.toFloat())
            }
        },
        onPrevPageClick = {
            when (orientation) {
                Orientation.Vertical -> continuousReaderState.scrollBackward(areaSize.height.toFloat())
                Orientation.Horizontal -> continuousReaderState.scrollBackward(areaSize.width.toFloat())
            }
        },
        contentAreaSize = areaSize,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
    ) {
        ContinuousReaderContent(state = continuousReaderState)
    }
    SettingsMenu(
        book = book,
        onMenuDismiss = { onShowSettingsMenuChange(false) },
        onShowHelpMenu = { onShowHelpDialogChange(true) },
        show = showSettingsMenu,
        settingsState = readerState,
        screenScaleState = screenScaleState,
        onSeriesPress = onSeriesBackClick,
        onBookClick = onBookBackClick,
        readerSettingsContent = { ContinuousReaderSettingsContent(continuousReaderState) }
    )

    ProgressSlider(
        pages = continuousReaderState.pages.collectAsState().value,
        currentPageIndex = continuousReaderState.currentPageIndex.collectAsState(0).value,
        onPageNumberChange = { coroutineScope.launch { continuousReaderState.scrollToPage(it + 1) } },
        show = showSettingsMenu,
        layoutDirection = layoutDirection,
        modifier = Modifier.align(Alignment.BottomStart),
    )
}

@Composable
fun ReaderControlsOverlay(
    readingDirection: LayoutDirection,
    onNexPageClick: () -> Unit,
    onPrevPageClick: () -> Unit,
    onSettingsMenuToggle: () -> Unit,
    contentAreaSize: IntSize,
    content: @Composable () -> Unit,
) {
    val leftAction = {
        if (readingDirection == LayoutDirection.Ltr) onPrevPageClick()
        else onNexPageClick()
    }
    val centerAction = { onSettingsMenuToggle() }
    val rightAction = {
        if (readingDirection == LayoutDirection.Ltr) onNexPageClick()
        else onPrevPageClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(contentAreaSize, readingDirection, onSettingsMenuToggle) {
                detectTapGestures { offset ->
                    val actionWidth = contentAreaSize.width.toFloat() / 3
                    when (offset.x) {
                        in 0f..<actionWidth -> leftAction()
                        in actionWidth..actionWidth * 2 -> centerAction()
                        else -> rightAction()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


private suspend fun registerCommonKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    showSettingsMenu: Boolean,
    setShowSettingsDialog: (Boolean) -> Unit,
    onShowHelpDialog: () -> Unit,
    onClose: () -> Unit,
) {
    keyEvents.collect { event ->
        if (event.type != KeyUp) return@collect

        when (event.key) {
            Key.M -> setShowSettingsDialog(!showSettingsMenu)
            Key.Escape -> setShowSettingsDialog(false)
            Key.H -> onShowHelpDialog()
            Key.DirectionLeft -> if (event.isAltPressed) onClose()
            else -> {}
        }
    }
}
