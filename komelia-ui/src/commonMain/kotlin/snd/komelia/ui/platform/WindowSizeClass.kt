package snd.komelia.ui.platform

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
    FULL;

    companion object {
        fun fromDp(size: Dp): WindowSizeClass {
            return when {
                size < 600.dp -> COMPACT
                size < 840.dp -> MEDIUM
                size < 1200.dp -> EXPANDED
                else -> FULL
            }
        }
    }
}
