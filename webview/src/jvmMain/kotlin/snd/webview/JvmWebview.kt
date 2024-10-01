package snd.webview

import io.github.snd_r.SharedLibrariesLoader
import java.awt.Component

class JvmWebview : Webview {

    init {
        SharedLibrariesLoader.loadLibrary("komelia_webview")
    }

    external fun start(component: Component)

    external fun updateSize(width: Int, height: Int)

    private external fun getWindowHandle0(surface: Component): Long
}
