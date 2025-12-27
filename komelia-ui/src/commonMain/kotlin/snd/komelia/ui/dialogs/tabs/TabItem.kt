package snd.komelia.ui.dialogs.tabs

import androidx.compose.ui.graphics.vector.ImageVector

data class TabItem(
    val title: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true
)