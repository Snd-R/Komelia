package snd.komelia

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
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
import androidx.compose.ui.window.WindowDecoration.Companion.Undecorated
import androidx.compose.ui.window.WindowDecoration.SystemDefault
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowPlacement.Floating
import androidx.compose.ui.window.WindowPlacement.Fullscreen
import androidx.compose.ui.window.WindowPlacement.Maximized
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory
import snd.komelia.AppDirectories.projectDirectories
import snd.komelia.DesktopPlatform.Linux
import snd.komelia.ui.DependencyContainer
import snd.komelia.ui.LocalAwtWindowState
import snd.komelia.ui.LocalWindow
import snd.komelia.ui.MainView
import snd.komelia.ui.error.ErrorView
import snd.komelia.ui.log.LogView
import snd.komelia.ui.log.LogbackFlowAppender
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.WindowSizeClass
import snd.komelia.ui.platform.canIntegrateWithSystemBar
import snd.komelia.ui.windowBorder
import java.awt.Dimension
import java.awt.event.WindowEvent
import java.nio.file.Path
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}
private var shouldRestart = true
private val appWindow = MutableStateFlow<ComposeWindow?>(null)
private val windowState = AwtWindowState(appWindow.filterNotNull())

private val initScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private val keyEvents = MutableSharedFlow<KeyEvent>(extraBufferCapacity = Int.MAX_VALUE)


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureLogging()
    FileKit.init(appId = "komelia")
    if (DesktopPlatform.Current == Linux) {
        // try to load system glib2 and gtk libraries by loading webkit2gtk first
        // loading in this order should prevent conflict between system gtk and bundled glib2 version
        loadWebviewLibraries()
        loadVipsLibraries()

        // use xembed enabled canvas for webview embedding
        System.setProperty("sun.awt.xembedserver", "true")
    }

    val lastError = MutableStateFlow<Throwable?>(null)
    val dependencies = MutableStateFlow<DependencyContainer?>(null)
    val initError = MutableStateFlow<Throwable?>(null)

    initScope.launch {
        try {
            val module = DesktopAppModule(windowState)
            dependencies.value = module.initDependencies()
        } catch (e: Throwable) {
            ensureActive()
            initError.value = e
        }
    }

    while (shouldRestart) {
        application(exitProcessOnExit = false) {
            LaunchedEffect(Unit) {
                initError.filterNotNull().collect {
                    lastError.value = it
                    initError.value = null
                    exitApplication()
                }
            }


            CompositionLocalProvider(
                LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { window ->
                    WindowExceptionHandler {
                        logger.error(it) { it.message }
                        lastError.value = it
                        window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                    }
                },
            ) {

                MainAppContent(
                    windowState = windowState,
                    dependencies = dependencies.collectAsState().value,
                    onCloseRequest = { shouldRestart = false }
                )
            }
        }

        val error = lastError.value
        if (error != null) {
            errorApp(
                initialWindowState = windowState,
                error = error,
                onRestart = {
                    shouldRestart = true
                    lastError.value = null
                },
                onExit = { shouldRestart = false }
            )
        }
    }

    exitProcess(0)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ApplicationScope.MainAppContent(
    windowState: AwtWindowState,
    dependencies: DependencyContainer?,
    onCloseRequest: () -> Unit,
) {
    var showLogWindow by remember { mutableStateOf(false) }
    val undecorated = remember { canIntegrateWithSystemBar() && DesktopPlatform.Current == Linux }

    Window(
        title = "Komelia",
        onCloseRequest = {
            onCloseRequest()
            exitApplication()
        },
        state = windowState,
        icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        decoration = if (undecorated) Undecorated() else SystemDefault,
        //Loses transparency on secondary monitor. See https://bugs.openjdk.org/browse/JDK-8304900
        // fixed in jdk22
//        transparent = undecorated,
        transparent = false,
        onPreviewKeyEvent = {
            keyEvents.tryEmit(it)
            false
        }
    ) {
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(500, 540)
            appWindow.value = window
            keyEvents.collect {
                if (it.key == Key.F11 && it.type == KeyUp) {
                    if (windowState.placement == Fullscreen) {
                        windowState.setFullscreen(false)
                    } else {
                        windowState.setFullscreen(true)
                    }
                } else if (it.key == Key.F12 && it.type == KeyUp) {
                    showLogWindow = !showLogWindow
                }
            }
        }

        val verticalInsets = remember { window.insets.left + window.insets.right }
        val horizontalInsets = remember { window.insets.top + window.insets.bottom }
        val widthClass = WindowSizeClass.fromDp(windowState.size.width - verticalInsets.dp)
        val heightClass = WindowSizeClass.fromDp(windowState.size.height - horizontalInsets.dp)

        CompositionLocalProvider(
            LocalWindow provides window,
            LocalAwtWindowState provides windowState
        ) {
            val borderModifier = derivedStateOf {
                if (undecorated && windowState.placement == Floating)
                    Modifier
                        //Loses transparency on secondary monitor. See https://bugs.openjdk.org/browse/JDK-8304900
                        // fixed in jdk22
//                        .clip(RoundedCornerShape(5.dp))
                        .border(1.dp, windowBorder.value)
                else Modifier
            }

            Box(borderModifier.value) {
                MainView(
                    dependencies = dependencies,
                    windowWidth = widthClass,
                    windowHeight = heightClass,
                    platformType = PlatformType.DESKTOP,
                    keyEvents = keyEvents
                )
            }
        }
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
            LogView(
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
            onCloseRequest = {
                onExit()
                exitApplication()
            },
            state = windowState,
            icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        ) {

            ErrorView(
                exception = error,
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

private fun configureLogging() {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = loggerContext.getLogger(ROOT_LOGGER_NAME)
    rootLogger.level = Level.INFO
//    loggerContext.getLogger("Exposed").level = Level.DEBUG

    val logEncoder = PatternLayoutEncoder()
    logEncoder.pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
    logEncoder.context = loggerContext
    logEncoder.start()

    val fileAppender = FileAppender<ILoggingEvent>()
    val logFile = Path.of(projectDirectories.dataDir).resolve("komelia.log").toString()
    fileAppender.file = logFile
    fileAppender.isAppend = false
    fileAppender.encoder = logEncoder
    fileAppender.context = loggerContext
    fileAppender.start()

    rootLogger.addAppender(fileAppender)
}
