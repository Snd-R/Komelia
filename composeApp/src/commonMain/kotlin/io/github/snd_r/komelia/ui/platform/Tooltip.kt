package io.github.snd_r.komelia.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Tooltip(
    tooltip: @Composable () -> Unit,
    delayMillis: Int = 500,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
