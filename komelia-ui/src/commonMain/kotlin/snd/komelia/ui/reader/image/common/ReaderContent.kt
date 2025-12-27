package snd.komelia.ui.reader.image.common

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
import snd.komelia.settings.model.ReaderType.CONTINUOUS
import snd.komelia.settings.model.ReaderType.PAGED
import snd.komelia.settings.model.ReaderType.PANELS
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.LocalWindowState
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.platform.PlatformType.MOBILE
import snd.komelia.ui.reader.image.ReaderState
import snd.komelia.ui.reader.image.ScreenScaleState
import snd.komelia.ui.reader.image.continuous.ContinuousReaderContent
import snd.komelia.ui.reader.image.continuous.ContinuousReaderState
import snd.komelia.ui.reader.image.paged.PagedReaderContent
import snd.komelia.ui.reader.image.paged.PagedReaderState
import snd.komelia.ui.reader.image.panels.PanelsReaderContent
import snd.komelia.ui.reader.image.panels.PanelsReaderState
import snd.komelia.ui.reader.image.settings.SettingsOverlay
import snd.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsState

@Composable
fun ReaderContent(
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    panelsReaderState: PanelsReaderState?,
    onnxRuntimeSettingsState: OnnxRuntimeSettingsState?,
    screenScaleState: ScreenScaleState,

    isColorCorrectionActive: Boolean,
    onColorCorrectionClick: () -> Unit,
    onExit: () -> Unit,
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
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
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        screenScaleState.composeScope = coroutineScope
    }
    val density = LocalDensity.current
    LaunchedEffect(density) {
        commonReaderState.pixelDensity.value = density
    }

    val topLevelFocus = remember { FocusRequester() }
    val volumeKeysNavigation = commonReaderState.volumeKeysNavigation.collectAsState().value
    var hasFocus by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                screenScaleState.setAreaSize(it)
            }
            .focusable()
            .focusRequester(topLevelFocus)
            .onFocusChanged { hasFocus = it.hasFocus }
            .onKeyEvent { event ->
                if (event.type != KeyUp) return@onKeyEvent false

                var consumed = true
                when (event.key) {
                    Key.M -> showSettingsMenu = !showSettingsMenu
                    Key.Escape -> showSettingsMenu = false
                    Key.H -> showHelpDialog = true
                    Key.DirectionLeft -> if (event.isAltPressed) onExit() else consumed = false
                    Key.Back -> if (showSettingsMenu) showSettingsMenu = false else onExit()
                    Key.U -> commonReaderState.onStretchToFitCycle()
                    Key.C -> if (event.isAltPressed) commonReaderState.onColorCorrectionDisable() else consumed = false
                    else -> consumed = false
                }
                consumed
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

            PANELS -> {
                check(panelsReaderState != null)
                PanelsReaderContent(
                    showHelpDialog = showHelpDialog,
                    onShowHelpDialogChange = { showHelpDialog = it },
                    showSettingsMenu = showSettingsMenu,
                    onShowSettingsMenuChange = { showSettingsMenu = it },
                    screenScaleState = screenScaleState,
                    panelsReaderState = panelsReaderState,
                    volumeKeysNavigation = volumeKeysNavigation
                )
            }

        }

        SettingsOverlay(
            show = showSettingsMenu,
            commonReaderState = commonReaderState,
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,
            panelsReaderState = panelsReaderState,
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
    onNexPageClick: suspend () -> Unit,
    onPrevPageClick: suspend () -> Unit,
    isSettingsMenuOpen: Boolean,
    onSettingsMenuToggle: () -> Unit,
    contentAreaSize: IntSize,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val leftAction = {
        if (isSettingsMenuOpen) onSettingsMenuToggle()
        else if (readingDirection == LayoutDirection.Ltr) coroutineScope.launch { onPrevPageClick() }
        else coroutineScope.launch { onNexPageClick() }
    }
    val centerAction = { onSettingsMenuToggle() }
    val rightAction = {
        if (isSettingsMenuOpen) onSettingsMenuToggle()
        else if (readingDirection == LayoutDirection.Ltr) coroutineScope.launch { onNexPageClick() }
        else coroutineScope.launch { onPrevPageClick() }
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
