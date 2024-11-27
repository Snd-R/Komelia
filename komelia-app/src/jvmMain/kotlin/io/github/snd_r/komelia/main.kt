package io.github.snd_r.komelia

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.jetbrains.JBR
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppDirectories.projectDirectories
import io.github.snd_r.komelia.platform.AwtWindowState
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.ui.MainView
import io.github.snd_r.komelia.ui.error.ErrorView
import io.github.snd_r.komelia.ui.log.LogView
import io.github.snd_r.komelia.ui.log.LogbackFlowAppender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.event.WindowEvent
import java.nio.file.Path
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}
private var shouldRestart = true
private var appWindowState = MutableStateFlow<WindowState?>(null)
private val initScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private val windowPlacementFlow = MutableStateFlow(Maximized)


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureLogging()
    if (DesktopPlatform.Current == DesktopPlatform.Linux) {
        // try to load system glib2 and gtk libraries by loading webkit2gtk first
        // loading in this order should prevent conflict between system gtk and bundled glib2 version
        loadWebviewLibraries()
        loadVipsLibraries()

        // use xembed enabled canvas for webview embedding
        System.setProperty("sun.awt.xembedserver", "true")
    }

    val lastError = MutableStateFlow<Throwable?>(null)
    val dependencies = MutableStateFlow<DesktopDependencyContainer?>(null)
    val initError = MutableStateFlow<Throwable?>(null)
    initScope.launch {
        try {
            dependencies.value = initDependencies(initScope, AwtWindowState(windowPlacementFlow))
        } catch (e: Throwable) {
            ensureActive()
            initError.value = e
        }
    }

    while (shouldRestart) {
        application(exitProcessOnExit = false) {
            val windowState = rememberWindowState(placement = Maximized, size = DpSize(1280.dp, 720.dp))
            LaunchedEffect(windowState) {
                appWindowState.value = windowState

                snapshotFlow { windowState.placement }
                    .onEach { windowPlacementFlow.value = it }
                    .launchIn(this)

                windowPlacementFlow.collect {
                    if (windowState.placement != it) {
                        windowState.placement = it
                    }
                }
            }

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
                initialWindowState = appWindowState.value,
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
    windowState: WindowState,
    dependencies: DesktopDependencyContainer?,
    onCloseRequest: () -> Unit,
) {
    var showLogWindow by remember { mutableStateOf(false) }
    val keyEvents = remember { MutableSharedFlow<KeyEvent>() }
    val coroutineScope = rememberCoroutineScope()
    val undecorated = remember {
        JBR.isAvailable()
                && DesktopPlatform.Current == DesktopPlatform.Linux
                && System.getenv("USE_CSD")?.toBoolean() ?: true
    }
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
            coroutineScope.launch { keyEvents.emit(it) }

            if (it.key == Key.F11 && it.type == KeyUp) {

                if (windowState.placement == Fullscreen) {
                    // Does not switch back to maximized. https://github.com/JetBrains/compose-multiplatform/issues/4006
                    windowState.placement = Floating
                } else {
                    windowState.placement = Fullscreen
                }
            }
            if (it.key == Key.F12 && it.type == KeyUp) {
                showLogWindow = !showLogWindow
            }

            false
        }
    ) {
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(800, 540)
        }

        val verticalInsets = remember { window.insets.left + window.insets.right }
        val widthClass = WindowWidth.fromDp(windowState.size.width - verticalInsets.dp)

        CompositionLocalProvider(LocalWindow provides window) {
            val borderModifier = derivedStateOf {
                if (undecorated && windowState.placement == Floating)
                    Modifier
                        //Loses transparency on secondary monitor. See https://bugs.openjdk.org/browse/JDK-8304900
                        // fixed in jdk22
//                        .clip(RoundedCornerShape(5.dp))
                        .border(1.dp, windowBorder.value ?: Color.Unspecified)
                else Modifier
            }

            Box(borderModifier.value) {
                val vmFactory = remember(dependencies) { dependencies?.let { DesktopViewModelFactory(it) } }
                CompositionLocalProvider(LocalDesktopViewModelFactory provides vmFactory) {
                    MainView(
                        dependencies = dependencies,
                        windowWidth = widthClass,
                        platformType = PlatformType.DESKTOP,
                        keyEvents = keyEvents
                    )
                }
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
