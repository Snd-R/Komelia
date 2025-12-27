package snd.komelia

import kotlinx.coroutines.flow.Flow

interface AppWindowState {
    val isFullscreen: Flow<Boolean>
    fun setFullscreen(enabled: Boolean)
}