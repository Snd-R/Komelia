package io.github.snd_r.komelia

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.ui.MainView
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private val initScope = CoroutineScope(Dispatchers.Default)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val dependencies = MutableStateFlow<WasmDependencyContainer?>(null)
    initScope.launch {
        dependencies.value = WasmDependencyContainer.createInstance(initScope)
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        var width by remember { mutableStateOf(WindowWidth.fromDp(window.innerWidth.dp)) }
        window.addEventListener("resize") {
            width = WindowWidth.fromDp(window.innerWidth.dp)
        }
        MainView(
            dependencies = dependencies.collectAsState().value,
            windowWidth = width,
            platformType = PlatformType.WEB,
            keyEvents = MutableSharedFlow()
        )
    }
}

