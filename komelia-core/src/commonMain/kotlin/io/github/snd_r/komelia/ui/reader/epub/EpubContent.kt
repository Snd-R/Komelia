package io.github.snd_r.komelia.ui.reader.epub

import androidx.compose.runtime.Composable
import snd.webview.KomeliaWebview
import snd.webview.compose.Webview

@Composable
fun EpubContent(onWebviewCreated: (KomeliaWebview) -> Unit) {
    Webview(onWebviewCreated)
}