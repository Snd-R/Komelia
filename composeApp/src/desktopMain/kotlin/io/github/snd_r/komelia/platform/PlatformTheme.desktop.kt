package io.github.snd_r.komelia.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.windowBorder


@Composable
actual fun ConfigurePlatformTheme(colors: ColorScheme) {
    windowBorder.value = colors.surfaceVariant
}