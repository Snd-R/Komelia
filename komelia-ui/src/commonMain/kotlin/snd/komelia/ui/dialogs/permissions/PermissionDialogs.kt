package snd.komelia.ui.dialogs.permissions

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile


@Composable
expect fun DownloadNotificationRequestDialog(onComplete: (granted: Boolean) -> Unit)

@Composable
expect fun StoragePermissionRequestDialog(onComplete: (directory: PlatformFile?) -> Unit)

