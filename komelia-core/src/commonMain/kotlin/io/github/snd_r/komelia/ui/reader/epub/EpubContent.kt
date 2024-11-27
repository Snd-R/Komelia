package io.github.snd_r.komelia.ui.reader.epub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalWindowState
import snd.webview.KomeliaWebview
import snd.webview.compose.Webview

@Composable
fun EpubContent(
    onWebviewCreated: (KomeliaWebview) -> Unit,
) {
    if (LocalPlatform.current == MOBILE) {
        val windowState = LocalWindowState.current
        DisposableEffect(Unit) {
            windowState.setFullscreen(true)
            onDispose { windowState.setFullscreen(false) }
        }
    }
    Webview(onWebviewCreated)
}