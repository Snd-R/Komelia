package io.github.snd_r.komelia.platform

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
actual fun ConfigurePlatformTheme(colors: ColorScheme) {
    val activity = LocalView.current.context as Activity
    LaunchedEffect(colors) {
        activity.window.statusBarColor = colors.surface.toArgb()
        activity.window.navigationBarColor = colors.surface.toArgb()
    }
}