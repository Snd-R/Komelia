package snd.komelia.ui.dialogs.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher

@Composable
actual fun DownloadNotificationRequestDialog(onComplete: (granted: Boolean) -> Unit) {
    LaunchedEffect(Unit) { onComplete(true) }
}

@Composable
actual fun StoragePermissionRequestDialog(onComplete: (directory: PlatformFile?) -> Unit) {
    val launcher = rememberDirectoryPickerLauncher { directory -> onComplete(directory) }
    LaunchedEffect(Unit) {
        launcher.launch()
    }
}