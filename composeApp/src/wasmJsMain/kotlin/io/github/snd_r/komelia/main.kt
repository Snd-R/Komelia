package io.github.snd_r.komelia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.ui.MainView
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableSharedFlow

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        var width by remember { mutableStateOf(WindowWidth.fromDp(window.innerWidth.dp)) }
        window.addEventListener("resize") {
            width = WindowWidth.fromDp(window.innerWidth.dp)
        }
        MainView(
            width,
            MutableSharedFlow()
        )
    }
}
