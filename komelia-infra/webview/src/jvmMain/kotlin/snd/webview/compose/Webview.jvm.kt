package snd.webview.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import snd.webview.KomeliaWebview
import snd.webview.WebviewPanel

@Composable
actual fun Webview(onCreated: (KomeliaWebview) -> Unit, ) {
    val webviewComponent = remember { WebviewPanel(onCreated = onCreated) }

    SwingPanel(
        factory = { webviewComponent },
        modifier = Modifier.fillMaxSize()
    )
}