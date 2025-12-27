package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformTitleBar(
    modifier: Modifier,
    applyInsets: Boolean,
    content: @Composable TitleBarScope.() -> Unit
) {
    SimpleTitleBarLayout(modifier, applyInsets, content)
}

actual fun canIntegrateWithSystemBar() = false