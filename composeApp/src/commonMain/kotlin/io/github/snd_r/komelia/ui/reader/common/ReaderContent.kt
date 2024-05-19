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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
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
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderContent
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderContent
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderPageState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ReaderContent(
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderPageState,
    continuousReaderState: ContinuousReaderState,
    screenScaleState: ScreenScaleState,

    onSeriesBackClick: () -> Unit,
    onBookBackClick: () -> Unit,
) {
    val book = commonReaderState.booksState.collectAsState().value?.currentBook
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
        val areaSize = screenScaleState.areaSize.collectAsState()
        val targetSize = screenScaleState.targetSize.collectAsState()
        if (areaSize.value == IntSize.Zero || targetSize.value == Size.Zero) {
            LoadingMaxSizeIndicator()
            return
        }

        when (commonReaderState.readerType.collectAsState().value) {
            PAGED -> PagedReaderContent(
                showHelpDialog = showHelpDialog,
                onShowHelpDialogChange = { showHelpDialog = it },
                showSettingsMenu = showSettingsMenu,
                onShowSettingsMenuChange = { showSettingsMenu = it },

                screenScaleState = screenScaleState,
                pagedReaderState = pagedReaderState,
                readerState = commonReaderState,

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
                readerState = commonReaderState,

                book = book,
                onBookBackClick = onBookBackClick,
                onSeriesBackClick = onSeriesBackClick,
            )
        }

    }
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
