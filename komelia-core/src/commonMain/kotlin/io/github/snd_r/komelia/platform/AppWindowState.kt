package io.github.snd_r.komelia.platform

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

interface AppWindowState {
    val isFullscreen: Flow<Boolean>
    fun setFullscreen(enabled: Boolean)
    fun setSystemBarsColor(color: Color)
}