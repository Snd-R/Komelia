package io.github.snd_r.komelia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowPlacement.Floating
import androidx.compose.ui.window.WindowPlacement.Fullscreen
import androidx.compose.ui.window.WindowPlacement.Maximized
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ch.qos.logback.classic.LoggerContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.ui.MainView
import io.github.snd_r.komelia.ui.error.ErrorView
import io.github.snd_r.komelia.ui.log.LogView
import io.github.snd_r.komelia.ui.log.LogbackFlowAppender
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.event.WindowEvent
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}
private val os: String = System.getProperty("os.name")


@Volatile
private var shouldRestart = true

@Volatile
private var lastError: Throwable? = null

@Volatile
private var windowLastState: WindowState? = null

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    while (shouldRestart) {
        application(exitProcessOnExit = false) {
            val windowState = windowLastState ?: rememberWindowState(
                placement = Maximized,
                size = DpSize(1280.dp, 720.dp),
            )

            CompositionLocalProvider(
                LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { window ->
                    WindowExceptionHandler {
                        logger.error(it) { it.message }
                        lastError = it
                        windowLastState = windowState
                        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    }
                }
            ) {
                MainAppContent(windowState = windowState, onCloseRequest = { shouldRestart = false })
            }
        }

        val error = lastError
        if (error != null) {
            errorApp(
                initialWindowState = windowLastState,
                error = error,
                onRestart = { shouldRestart = true },
                onExit = { shouldRestart = false }
            )
        }
    }

    exitProcess(0)
}


@Composable
private fun ApplicationScope.MainAppContent(
    windowState: WindowState,
    onCloseRequest: () -> Unit,
) {
    var showLogWindow by remember { mutableStateOf(false) }
    var beforeFullScreenPlacement by remember { mutableStateOf(windowState.placement) }
    val keyEvents = remember { MutableSharedFlow<KeyEvent>() }
    val coroutineScope = rememberCoroutineScope()
    Window(
        title = "Komelia",
        onCloseRequest = {
            onCloseRequest()
            exitApplication()
        },
        state = windowState,
        icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        onPreviewKeyEvent = {
            coroutineScope.launch { keyEvents.emit(it) }

            if (it.key == Key.F11 && it.type == KeyUp) {

                if (windowState.placement == Fullscreen) {
                    // FIXME https://github.com/JetBrains/compose-multiplatform/issues/4006
                    if (os == "Linux") windowState.placement = Floating
                    else windowState.placement = beforeFullScreenPlacement
                } else {
                    beforeFullScreenPlacement = windowState.placement
                    windowState.placement = Fullscreen
                }
            }
            if (it.key == Key.F12 && it.type == KeyUp) {
                showLogWindow = true
            }

            false
        }
    ) {
        window.minimumSize = Dimension(540, 540)
        val horizontalInsets = window.insets.top + window.insets.bottom
        MainView(
            windowHeight = windowState.size.height - horizontalInsets.dp,
            windowWidth = windowState.size.width,
            keyEvents = keyEvents
        )
    }

    if (showLogWindow) {
        val logWindowState = rememberWindowState(placement = Floating, size = DpSize(1280.dp, 720.dp))

        val logFlowAppender = remember { LogbackFlowAppender() }
        DisposableEffect(Unit) {
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
            logFlowAppender.context = loggerContext
            logFlowAppender.start()
            val logbackLogger = loggerContext.getLogger(ROOT_LOGGER_NAME)
            logbackLogger.addAppender(logFlowAppender)

            onDispose { logbackLogger.detachAppender(logFlowAppender) }
        }

        Window(
            title = "Komelia Logs",
            onCloseRequest = { showLogWindow = false },
            state = logWindowState,
            icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        ) {
            window.minimumSize = Dimension(540, 540)
            val horizontalInsets = window.insets.top + window.insets.bottom
            LogView(
                windowHeight = windowState.size.height - horizontalInsets.dp,
                windowWidth = windowState.size.width,
                logsFlow = logFlowAppender.logEventsFlow
            )
        }
    }

}

private fun errorApp(
    initialWindowState: WindowState?,
    error: Throwable,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    application(exitProcessOnExit = false) {
        val windowState = initialWindowState ?: rememberWindowState(
            placement = Maximized,
            size = DpSize(1280.dp, 720.dp),
        )

        Window(
            title = "Komelia Error",
            onCloseRequest = ::exitApplication,
            state = windowState,
            icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        ) {

            val horizontalInsets = window.insets.top + window.insets.bottom
            ErrorView(
                exception = error,
                windowHeight = windowState.size.height - horizontalInsets.dp,
                windowWidth = windowState.size.width,
                onRestart = {
                    onRestart()
                    exitApplication()
                },
                onExit = {
                    onExit()
                    exitApplication()
                }
            )
        }

    }
}