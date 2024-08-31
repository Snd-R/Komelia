package io.github.snd_r.komelia.ui.reader.common

import android.app.Activity
import android.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat.Type.navigationBars
import androidx.core.view.WindowInsetsCompat.Type.statusBars
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun ImmersiveMode(enabled: Boolean) {
    val view = LocalView.current
    val activity = view.context as Activity
    val insetsController = remember(view) { WindowInsetsControllerCompat(activity.window, view) }
    val visibleBarColor = MaterialTheme.colorScheme.surfaceDim

    DisposableEffect(enabled) {
        val window=  activity.window
        if (enabled) {
            insetsController.hide(statusBars() or navigationBars())
            window.statusBarColor = Color.TRANSPARENT
        } else {
            insetsController.show(statusBars() or navigationBars())
            window.statusBarColor = visibleBarColor.toArgb()
        }

        onDispose {
            insetsController.show(statusBars() or navigationBars())
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
}