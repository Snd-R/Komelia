package snd.webview.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import snd.webview.KomeliaWebview

@Composable
actual fun Webview(onCreated: (KomeliaWebview) -> Unit) {
    val webview = remember { KomeliaWebview() }
    LaunchedEffect(webview) {
        onCreated(webview)
    }
}