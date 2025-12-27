package snd.komelia.ui.settings.offline.downloads

import coil3.PlatformContext
import io.github.vinceglb.filekit.PlatformFile
import snd.komelia.AppDirectories

internal actual fun getDefaultInternalDownloadsDir(platformContent: PlatformContext): DefaultDownloadStorageLocation {
    return DefaultDownloadStorageLocation(
        platformFile = PlatformFile(AppDirectories.defaultOfflineLibraryPath.toFile()),
        label = AppDirectories.defaultOfflineLibraryPath.toString()
    )
}