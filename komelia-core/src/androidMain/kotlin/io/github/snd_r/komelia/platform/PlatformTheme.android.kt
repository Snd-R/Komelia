package io.github.snd_r.komelia.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.AppTheme.ThemeType

@Composable
actual fun ConfigurePlatformTheme(theme: AppTheme) {
    val view = LocalView.current
    val activity = view.context as Activity
    LaunchedEffect(theme) {
        WindowInsetsControllerCompat(activity.window, view).apply {
            when (theme.type) {
                ThemeType.DARK -> {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }

                ThemeType.LIGHT -> {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true

                }
            }
        }
    }
}