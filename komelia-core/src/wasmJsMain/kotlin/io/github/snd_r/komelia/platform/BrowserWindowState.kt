package io.github.snd_r.komelia.platform

import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow


class BrowserWindowState : AppWindowState {
    override val isFullscreen get() = fullscreenStateFlow
    private val fullscreenStateFlow = MutableStateFlow(checkIfFullscreen())

    init {
        document.onfullscreenchange = { fullscreenStateFlow.value = checkIfFullscreen() }
    }
    private fun checkIfFullscreen() = document.fullscreenElement != null

    override fun setFullscreen(enabled: Boolean) {
        if (enabled) {
            document.documentElement?.requestFullscreen()
        } else {
            document.exitFullscreen()
        }
    }
}