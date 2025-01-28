package io.github.snd_r.komelia.ui.reader.image.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalWindowState
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.image.ReaderState
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderContent
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderContent
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.settings.SettingsOverlay
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ReaderContent(
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    screenScaleState: ScreenScaleState,

    onSeriesBackPress: () -> Unit,
    onBookBackPress: () -> Unit,
    isColorCorrectionActive: Boolean,
    onColorCorrectionClick: () -> Unit,
    onExit: () -> Unit,
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    BackPressHandler { if (showSettingsMenu) showSettingsMenu = false else onExit() }

    if (LocalPlatform.current == MOBILE) {
        val windowState = LocalWindowState.current
        val barsColor = MaterialTheme.colorScheme.surfaceVariant
        DisposableEffect(showSettingsMenu) {
            if (showSettingsMenu) {
                windowState.setSystemBarsColor(barsColor)
                windowState.setFullscreen(false)
            } else {
                windowState.setFullscreen(true)
            }
            onDispose {
                windowState.setSystemBarsColor(Color.Transparent)
                windowState.setFullscreen(false)
            }
        }
    }

    LaunchedEffect(Unit) {
        registerCommonKeyboardEvents(
            keyEvents = keyEvents,
            showSettingsMenu = showSettingsMenu,
            setShowSettingsDialog = { showSettingsMenu = it },
            onShowHelpDialog = { showHelpDialog = !showHelpDialog },
            onClose = onSeriesBackPress
        )
    }
    Box(Modifier.fillMaxSize().onSizeChanged { screenScaleState.setAreaSize(it) }) {
        val areaSize = screenScaleState.areaSize.collectAsState()
        if (areaSize.value == IntSize.Zero) {
            LoadingMaxSizeIndicator()
            return
        }

        when (commonReaderState.readerType.collectAsState().value) {
            PAGED -> {
                PagedReaderContent(
                    showHelpDialog = showHelpDialog,
                    onShowHelpDialogChange = { showHelpDialog = it },
                    showSettingsMenu = showSettingsMenu,
                    onShowSettingsMenuChange = { showSettingsMenu = it },
                    screenScaleState = screenScaleState,
                    pagedReaderState = pagedReaderState,
                )
            }

            CONTINUOUS -> {
                ContinuousReaderContent(
                    showHelpDialog = showHelpDialog,
                    onShowHelpDialogChange = { showHelpDialog = it },
                    showSettingsMenu = showSettingsMenu,
                    onShowSettingsMenuChange = { showSettingsMenu = it },
                    screenScaleState = screenScaleState,
                    continuousReaderState = continuousReaderState,
                )
            }
        }

        SettingsOverlay(
            show = showSettingsMenu,
            onDismiss = { showSettingsMenu = false },
            commonReaderState = commonReaderState,
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,
            screenScaleState = screenScaleState,
            isColorCorrectionsActive = isColorCorrectionActive,
            onColorCorrectionClick = onColorCorrectionClick,
            onSeriesPress = onSeriesBackPress,
            onBookPress = onBookBackPress,
            ohShowHelpDialogChange = { showHelpDialog = it }
        )
    }
}

@Composable
fun ReaderControlsOverlay(
    readingDirection: LayoutDirection,
    onNexPageClick: () -> Unit,
    onPrevPageClick: () -> Unit,
    isSettingsMenuOpen: Boolean,
    onSettingsMenuToggle: () -> Unit,
    contentAreaSize: IntSize,
    content: @Composable () -> Unit,
) {
    val leftAction = {
        if (isSettingsMenuOpen) onSettingsMenuToggle()
        else if (readingDirection == LayoutDirection.Ltr) onPrevPageClick()
        else onNexPageClick()
    }
    val centerAction = { onSettingsMenuToggle() }
    val rightAction = {
        if (isSettingsMenuOpen) onSettingsMenuToggle()
        else if (readingDirection == LayoutDirection.Ltr) onNexPageClick()
        else onPrevPageClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(
                contentAreaSize,
                readingDirection,
                onSettingsMenuToggle,
                isSettingsMenuOpen
            ) {
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
