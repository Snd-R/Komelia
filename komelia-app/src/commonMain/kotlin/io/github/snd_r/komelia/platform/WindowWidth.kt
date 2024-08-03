package io.github.snd_r.komelia.platform

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidth {
    COMPACT,
    MEDIUM,
    EXPANDED,
    FULL;

    companion object {
        fun fromDp(windowWidth: Dp): WindowWidth {
            return when {
                windowWidth < 600.dp -> COMPACT
                windowWidth < 840.dp -> MEDIUM
                windowWidth < 1200.dp -> EXPANDED
                else -> FULL
            }
        }
    }
}
