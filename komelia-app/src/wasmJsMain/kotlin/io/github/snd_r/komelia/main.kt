package io.github.snd_r.komelia

import WasmDependencyContainer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowSizeClass
import io.github.snd_r.komelia.ui.MainView
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.w3c.dom.events.KeyboardEvent

private val initScope = CoroutineScope(Dispatchers.Default)
const val canvasElemId = "ComposeTarget"

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val dependencies = MutableStateFlow<WasmDependencyContainer?>(null)
    val keyEvents = MutableSharedFlow<KeyEvent>()
    val windowWidth = MutableStateFlow(WindowSizeClass.fromDp(window.innerWidth.dp))
    val windowHeight = MutableStateFlow(WindowSizeClass.fromDp(window.innerHeight.dp))
    initScope.launch {
        dependencies.value = initDependencies(initScope)
    }
    window.addEventListener("resize") {
        windowWidth.value = WindowSizeClass.fromDp(window.innerWidth.dp)
    }
    document.addEventListener("keydown") { event ->
        initScope.launch { keyEvents.emit((event as KeyboardEvent).toComposeEvent()) }
    }
    document.addEventListener("keyup") { event ->
        initScope.launch { keyEvents.emit((event as KeyboardEvent).toComposeEvent()) }
    }

    CanvasBasedWindow(canvasElementId = canvasElemId) {
        val fontFamilyResolver = LocalFontFamilyResolver.current
        MainView(
            dependencies = dependencies.collectAsState().value,
            windowWidth = windowWidth.collectAsState().value,
            windowHeight = windowHeight.collectAsState().value,
            platformType = PlatformType.WEB_KOMF,
            keyEvents = keyEvents
        )

        LaunchedEffect(Unit) {
            loadFonts(fontFamilyResolver)
        }
    }
}
