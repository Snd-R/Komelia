package io.github.snd_r.komelia.ui.reader.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderContent
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderPageState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderSettingsContent
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
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
    val coroutineScope = rememberCoroutineScope()
    val book = settingsState.bookState.collectAsState().value?.book

    var showNavMenu by remember { mutableStateOf(false) }
    var showNavHelpDialog by remember { mutableStateOf(false) }

    if (showNavHelpDialog) {
        NavigationHelpDialog(onDismissRequest = { showNavHelpDialog = false })
    }

    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(Unit) {
        registerCommonKeyboardEvents(
            keyEvents = keyEvents,
            onNavMenuToggle = { showNavMenu = !showNavMenu },
            onShowHelpDialog = { showNavHelpDialog = !showNavHelpDialog },
            onClose = onSeriesBackClick
        )
    }

    Box(
        Modifier.onSizeChanged {
            screenScaleState.setAreaSize(it)
        }
    ) {
        val currentContainerSize = screenScaleState.areaSize.collectAsState()
        if (currentContainerSize.value == IntSize.Zero) return

        val readerType = settingsState.readerType.collectAsState()

        when (readerType.value) {
            PAGED -> {

                val layoutDirection = when (pageState.readingDirection.collectAsState().value) {
                    PagedReaderState.ReadingDirection.LEFT_TO_RIGHT -> LayoutDirection.Ltr
                    PagedReaderState.ReadingDirection.RIGHT_TO_LEFT -> LayoutDirection.Rtl
                }
                ReaderControlsOverlay(
                    readingDirection = layoutDirection,
                    onNexPageClick = pageState::nextPage,
                    onPrevPageClick = pageState::previousPage,
                    contentAreaSize = currentContainerSize.value,
                    onNavMenuToggle = { showNavMenu = !showNavMenu },
                ) {
                    ScalableContainer(scaleState = screenScaleState) {
                        PagedReaderContent(pageState = pageState)
                    }
                }

                SettingsMenu(
                    book = book,
                    onMenuDismiss = { showNavMenu = !showNavMenu },
                    onShowHelpMenu = { showNavHelpDialog = true },
                    show = showNavMenu,
                    settingsState = settingsState,
                    screenScaleState = screenScaleState,
                    onSeriesPress = onSeriesBackClick,
                    onBookClick = onBookBackClick,
                    readerSettingsContent = { PagedReaderSettingsContent(pageState) }
                )


                PageSpreadProgressSlider(
                    pageSpreads = pageState.pageSpreads.collectAsState().value,
                    currentSpreadIndex = pageState.currentSpreadIndex.collectAsState().value,
                    onPageNumberChange = pageState::onPageChange,
                    show = showNavMenu,
                    layoutDirection = layoutDirection,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }

            CONTINUOUS -> {
                val layoutDirection = when (continuousReaderState.readingDirection.collectAsState().value) {
                    ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM -> LayoutDirection.Ltr
                    ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT -> LayoutDirection.Ltr
                    ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT -> LayoutDirection.Rtl
                }
                ReaderControlsOverlay(
                    readingDirection = layoutDirection,
                    onNexPageClick = { coroutineScope.launch { continuousReaderState.scrollToNextPage() } },
                    onPrevPageClick = { coroutineScope.launch { continuousReaderState.scrollToPreviousPage() } },
                    contentAreaSize = currentContainerSize.value,
                    onNavMenuToggle = { showNavMenu = !showNavMenu },
                ) {
                    ContinuousReaderContent(state = continuousReaderState)
                }
                SettingsMenu(
                    book = book,
                    onMenuDismiss = { showNavMenu = !showNavMenu },
                    onShowHelpMenu = { showNavHelpDialog = true },
                    show = showNavMenu,
                    settingsState = settingsState,
                    screenScaleState = screenScaleState,
                    onSeriesPress = onSeriesBackClick,
                    onBookClick = onBookBackClick,
                    readerSettingsContent = { ContinuousReaderSettingsContent(continuousReaderState) }
                )

                ProgressSlider(
                    pages = continuousReaderState.pages.collectAsState().value,
                    currentPageIndex = continuousReaderState.currentPageIndex.collectAsState(0).value,
                    onPageNumberChange = { coroutineScope.launch { continuousReaderState.scrollToPage(it + 1) } },
                    show = showNavMenu,
                    layoutDirection = layoutDirection,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
        }

    }
}


@Composable
fun ReaderControlsOverlay(
    readingDirection: LayoutDirection,
    onNexPageClick: () -> Unit,
    onPrevPageClick: () -> Unit,
    onNavMenuToggle: () -> Unit,
    contentAreaSize: IntSize,
    content: @Composable () -> Unit,
) {
    val leftAction = {
        if (readingDirection == LayoutDirection.Ltr) onPrevPageClick()
        else onNexPageClick()
    }
    val centerAction = { onNavMenuToggle() }
    val rightAction = {
        if (readingDirection == LayoutDirection.Ltr) onNexPageClick()
        else onPrevPageClick()
    }

    Box(
        modifier = Modifier
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
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


private suspend fun registerCommonKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    onNavMenuToggle: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onClose: () -> Unit,
) {
    keyEvents.collect { event ->
        if (event.type != KeyUp) return@collect

        when (event.key) {
            Key.M -> onNavMenuToggle()
            Key.H -> onShowHelpDialog()
            Key.Backspace -> onClose()
            else -> {}
        }
    }
}
