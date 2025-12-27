package snd.komelia.ui.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import snd.komelia.ui.Theme
import snd.komelia.ui.Theme.ThemeType

@Composable
actual fun ConfigurePlatformTheme(theme: Theme) {
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