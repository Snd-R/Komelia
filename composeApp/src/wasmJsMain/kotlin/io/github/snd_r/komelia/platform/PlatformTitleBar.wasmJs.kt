package io.github.snd_r.komelia.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformTitleBar(
    modifier: Modifier,
    content: @Composable TitleBarScope.() -> Unit
) {
    SimpleTitleBarLayout(modifier, content)
}