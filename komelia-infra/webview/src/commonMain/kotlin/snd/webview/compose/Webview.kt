package snd.webview.compose

import androidx.compose.runtime.Composable
import snd.webview.KomeliaWebview

@Composable
expect fun Webview(
    onCreated: (KomeliaWebview) -> Unit,
)
