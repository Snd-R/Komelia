package io.github.snd_r.komelia.platform

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsCompat.Type.navigationBars
import androidx.core.view.WindowInsetsCompat.Type.statusBars
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidWindowState(
    private val activity: StateFlow<Activity?>,
) : AppWindowState {
    override val isFullscreen = MutableStateFlow(false)

    override fun setFullscreen(enabled: Boolean) {
        val activity = this.activity.value ?: return
        val insetsController = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        if (enabled) {
            insetsController.hide(statusBars() or navigationBars())
            insetsController.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            isFullscreen.value = true
        } else {
            insetsController.show(statusBars() or navigationBars())
            insetsController.systemBarsBehavior = BEHAVIOR_DEFAULT
            isFullscreen.value = false

        }
    }

    override fun setSystemBarsColor(color: Color) {
        val window = activity.value?.window ?: return
        window.statusBarColor = color.toArgb()
        window.navigationBarColor = color.toArgb()
    }
}