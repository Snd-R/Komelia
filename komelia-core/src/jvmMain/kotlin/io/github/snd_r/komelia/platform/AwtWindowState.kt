package io.github.snd_r.komelia.platform

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowPlacement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map


class AwtWindowState(
    private val placementFlow: MutableStateFlow<WindowPlacement>
) : AppWindowState {
    override val isFullscreen get() = placementFlow.map { it == WindowPlacement.Fullscreen }

    override fun setFullscreen(enabled: Boolean) {
        if (enabled) {
            placementFlow.value = WindowPlacement.Fullscreen
        } else {
            placementFlow.value = WindowPlacement.Floating
        }
    }

    override fun setSystemBarsColor(color: Color) = Unit
}