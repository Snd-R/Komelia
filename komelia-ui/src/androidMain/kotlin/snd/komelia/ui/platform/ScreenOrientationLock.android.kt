package snd.komelia.ui.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun LockScreenOrientation(locked: Boolean) {
    val activity = LocalContext.current.findActivity() ?: return

    DisposableEffect(locked) {
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation =
            if (locked) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
