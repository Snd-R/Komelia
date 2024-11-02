package snd.webview.compose

import androidx.compose.runtime.Composable
import snd.webview.Webview

@Composable
expect fun Webview(
    onCreated: (Webview) -> Unit,
)
