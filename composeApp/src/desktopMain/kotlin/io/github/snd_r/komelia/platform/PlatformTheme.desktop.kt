package io.github.snd_r.komelia.platform

import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.windowBorder


@Composable
actual fun ConfigurePlatformTheme(theme: AppTheme) {
    windowBorder.value = theme.colorScheme.surfaceVariant
}