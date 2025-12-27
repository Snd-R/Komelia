package snd.komelia.ui.settings.offline.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberStorageLabel(file: PlatformFile): String {
    return remember(file) { file.toString() }
}