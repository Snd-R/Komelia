package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import snd.komelia.ui.Theme
import snd.komelia.ui.windowBorder


@Composable
actual fun ConfigurePlatformTheme(theme: Theme) {
    windowBorder.value = theme.colorScheme.surfaceVariant
}