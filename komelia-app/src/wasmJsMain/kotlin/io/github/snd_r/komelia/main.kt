package io.github.snd_r.komelia

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.WindowWidth
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

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val dependencies = MutableStateFlow<WasmDependencyContainer?>(null)
    val keyEvents = MutableSharedFlow<KeyEvent>()
    val windowWidth = MutableStateFlow(WindowWidth.fromDp(window.innerWidth.dp))
    initScope.launch {
        dependencies.value = WasmDependencyContainer.createInstance(initScope)
    }
    window.addEventListener("resize") {
        windowWidth.value = WindowWidth.fromDp(window.innerWidth.dp)
    }
    document.addEventListener("keydown") { event ->
        initScope.launch { keyEvents.emit((event as KeyboardEvent).toComposeEvent()) }
    }
    document.addEventListener("keyup") { event ->
        initScope.launch { keyEvents.emit((event as KeyboardEvent).toComposeEvent()) }
    }
    overrideFetch()

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        val fontFamilyResolver = LocalFontFamilyResolver.current
            MainView(
                dependencies = dependencies.collectAsState().value,
                windowWidth = windowWidth.collectAsState().value,
                platformType = PlatformType.WEB_KOMF,
                keyEvents = keyEvents
            )

        LaunchedEffect(Unit) {
            loadFonts(fontFamilyResolver)
        }
    }
}

private fun overrideFetch() {
    js(
        """
    window.originalFetch = window.fetch;
    window.fetch = function (resource, init) {
        init = Object.assign({}, init);
        init.headers = Object.assign( { 'X-Requested-With' : 'XMLHttpRequest' }, init.headers) 
        init.credentials = init.credentials !== undefined ? init.credentials : 'include';
        return window.originalFetch(resource, init);
    };
"""
    )
}
