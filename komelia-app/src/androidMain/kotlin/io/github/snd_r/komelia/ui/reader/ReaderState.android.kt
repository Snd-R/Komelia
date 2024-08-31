@file:Suppress("DEPRECATION") // support for API < 30
package io.github.snd_r.komelia.ui.reader

import android.app.Activity
import android.content.Context
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN

actual fun onImmersiveModeEnable(enable: Boolean, context: Context) {
    val activity = context as Activity

    val currentFlags = activity.window.decorView.systemUiVisibility
    if (enable) {
        activity.window.addFlags(FLAG_FULLSCREEN)
        activity.window.decorView.systemUiVisibility = currentFlags or SYSTEM_UI_FLAG_HIDE_NAVIGATION
    } else {
        activity.window.clearFlags(FLAG_FULLSCREEN)
        activity.window.decorView.systemUiVisibility = currentFlags and SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
    }
}
