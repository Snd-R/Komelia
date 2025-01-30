package io.github.snd_r.komelia.platform

import kotlinx.coroutines.flow.Flow

interface AppWindowState {
    val isFullscreen: Flow<Boolean>
    fun setFullscreen(enabled: Boolean)
}