package io.github.snd_r.komelia.ui.reader.image.common

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
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
import io.github.snd_r.komelia.ui.settings.imagereader.OnnxRuntimeSettingsState

@Composable
fun ReaderContent(
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    onnxRuntimeSettingsState: OnnxRuntimeSettingsState?,
    screenScaleState: ScreenScaleState,

    isColorCorrectionActive: Boolean,
    onColorCorrectionClick: () -> Unit,
    onExit: () -> Unit,
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    BackPressHandler { if (showSettingsMenu) showSettingsMenu = false else onExit() }

    if (LocalPlatform.current == MOBILE) {
        val windowState = LocalWindowState.current
        DisposableEffect(showSettingsMenu) {
            if (showSettingsMenu) {
                windowState.setFullscreen(false)
            } else {
                windowState.setFullscreen(true)
            }
            onDispose {
                windowState.setFullscreen(false)
            }
        }
    }

    val topLevelFocus = remember { FocusRequester() }
    val volumeKeysNavigation = commonReaderState.volumeKeysNavigation.collectAsState().value
    var hasFocus by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { screenScaleState.setAreaSize(it) }
            .focusable()
            .focusRequester(topLevelFocus)
            .onFocusChanged { hasFocus = it.hasFocus }
            .onKeyEvent { event ->
                commonKeyboardEventsHandler(
                    event = event,
                    showSettingsMenu = showSettingsMenu,
                    setShowSettingsDialog = { showSettingsMenu = it },
                    onShowHelpDialog = { showHelpDialog = !showHelpDialog },
                    onExit = onExit
                )
                false
            }
    ) {
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
                    volumeKeysNavigation = volumeKeysNavigation
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
                    volumeKeysNavigation = volumeKeysNavigation
                )
            }
        }

        SettingsOverlay(
            show = showSettingsMenu,
            commonReaderState = commonReaderState,
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,
            onnxRuntimeSettingsState = onnxRuntimeSettingsState,
            screenScaleState = screenScaleState,
            isColorCorrectionsActive = isColorCorrectionActive,
            onColorCorrectionClick = onColorCorrectionClick,
            onBackPress = onExit,
            ohShowHelpDialogChange = { showHelpDialog = it },
        )

        EInkFlashOverlay(
            enabled = commonReaderState.flashOnPageChange.collectAsState().value,
            pageChangeFlow = commonReaderState.pageChangeFlow,
            flashEveryNPages = commonReaderState.flashEveryNPages.collectAsState().value,
            flashWith = commonReaderState.flashWith.collectAsState().value,
            flashDuration = commonReaderState.flashDuration.collectAsState().value
        )
    }
    LaunchedEffect(hasFocus) {
        if (!hasFocus) topLevelFocus.requestFocus()
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
    modifier: Modifier,
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
        modifier = modifier
            .fillMaxSize()
            .focusable()
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


private fun commonKeyboardEventsHandler(
    event: KeyEvent,
    showSettingsMenu: Boolean,
    setShowSettingsDialog: (Boolean) -> Unit,
    onShowHelpDialog: () -> Unit,
    onExit: () -> Unit,
): Boolean {
    if (event.type != KeyUp) return false

    when (event.key) {
        Key.M -> setShowSettingsDialog(!showSettingsMenu)
        Key.Escape -> setShowSettingsDialog(false)
        Key.H -> onShowHelpDialog()
        Key.DirectionLeft -> if (event.isAltPressed) onExit()
        else -> return false
    }
    return true
}
