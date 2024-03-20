package io.github.snd_r.komelia.ui.platform

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
@OptIn(ExperimentalFoundationApi::class)
actual fun Tooltip(
    tooltip: @Composable () -> Unit,
    delayMillis: Int,
    modifier: Modifier ,
    content: @Composable () -> Unit
) {
    TooltipArea(
        tooltip = tooltip,
        delayMillis = delayMillis,
        modifier = modifier
    ) {
        content()
    }
}
