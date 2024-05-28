package io.github.snd_r.komelia.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformTitleBar(
    content: @Composable TitleBarScope.() -> Unit,
)

interface TitleBarScope {
    @Stable
    fun Modifier.align(alignment: Alignment.Horizontal): Modifier
}
