package io.github.snd_r.komelia.ui.reader.epub

import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.platform.BackPressHandler
import snd.webview.KomeliaWebview
import snd.webview.compose.Webview

@Composable
fun EpubContent(
    onWebviewCreated: (KomeliaWebview) -> Unit,
    onBackButtonPress: () -> Unit,
) {
    Webview(onWebviewCreated)
    BackPressHandler(onBackButtonPress)
}