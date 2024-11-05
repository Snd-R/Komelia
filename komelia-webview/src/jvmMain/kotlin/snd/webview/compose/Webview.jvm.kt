package snd.webview.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import snd.webview.Webview
import snd.webview.WebviewPanel

@Composable
actual fun Webview(
    onCreated: (Webview) -> Unit,
) {
    val webviewComponent = remember { WebviewPanel(onCreated = onCreated) }

    SwingPanel(
        factory = { webviewComponent },
        background = Color.Black,
        modifier = Modifier.fillMaxSize()
    )
}