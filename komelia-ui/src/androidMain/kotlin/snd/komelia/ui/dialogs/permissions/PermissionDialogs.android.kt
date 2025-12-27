package snd.komelia.ui.dialogs.permissions

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun DownloadNotificationRequestDialog(onComplete: (granted: Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionRequester = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> onComplete(granted) }
        )
        LaunchedEffect(Unit) {
            permissionRequester.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        LaunchedEffect(Unit) { onComplete(true) }
    }
}

@Composable
actual fun StoragePermissionRequestDialog(onComplete: (directory: PlatformFile?) -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult


        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
        onComplete(PlatformFile(uri))
    }
    LaunchedEffect(Unit) {
        launcher.launch(null)
    }
}