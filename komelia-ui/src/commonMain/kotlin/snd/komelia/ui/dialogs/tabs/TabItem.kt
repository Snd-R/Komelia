package snd.komelia.ui.dialogs.tabs

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

data class TabItem(
    val title: StringResource,
    val icon: ImageVector? = null,
    val enabled: Boolean = true
)