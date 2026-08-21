package snd.komelia.ui.platform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun hasLanPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        val context = LocalContext.current
        val result = context.checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK)
        return result == PackageManager.PERMISSION_GRANTED
    } else {
        return true
    }
}