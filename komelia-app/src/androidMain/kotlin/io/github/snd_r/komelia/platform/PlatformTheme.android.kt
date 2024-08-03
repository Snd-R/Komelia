package io.github.snd_r.komelia.platform

import android.app.Activity
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import io.github.snd_r.komelia.ui.common.AppTheme

@Composable
actual fun ConfigurePlatformTheme(theme: AppTheme) {
    val activity = LocalView.current.context as Activity
    LaunchedEffect(theme) {
        activity.window.statusBarColor = theme.colorScheme.surface.toArgb()
        activity.window.navigationBarColor = theme.colorScheme.surface.toArgb()

        when (theme) {
            AppTheme.DARK -> activity.window.decorView.systemUiVisibility = 0
            AppTheme.LIGHT -> activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}