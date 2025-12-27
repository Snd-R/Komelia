package snd.komelia.ui.reader.epub

import androidx.compose.runtime.Composable
import snd.komelia.ui.platform.BackPressHandler
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