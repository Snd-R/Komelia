package snd.komelia

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.w3c.dom.events.KeyboardEvent
import snd.komelia.db.getIndexedDb
import snd.komelia.ui.DependencyContainer
import snd.komelia.ui.MainView
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.WindowSizeClass

private val initScope = CoroutineScope(Dispatchers.Default)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val dependencies = MutableStateFlow<DependencyContainer?>(null)
    val keyEvents = MutableSharedFlow<KeyEvent>()
    val windowWidth = MutableStateFlow(WindowSizeClass.fromDp(window.innerWidth.dp))
    val windowHeight = MutableStateFlow(WindowSizeClass.fromDp(window.innerHeight.dp))
    initScope.launch {
        val database = getIndexedDb()
        val module = WasmAppModule(database)
        dependencies.value = module.initDependencies()
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

    ComposeViewport {
        MainView(
            dependencies = dependencies.collectAsState().value,
            windowWidth = windowWidth.collectAsState().value,
            windowHeight = windowHeight.collectAsState().value,
            platformType = PlatformType.WEB_KOMF,
            keyEvents = keyEvents
        )
    }
}
