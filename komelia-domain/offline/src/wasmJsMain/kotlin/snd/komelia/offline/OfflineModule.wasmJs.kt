package snd.komelia.offline

import coil3.PlatformContext
import snd.komelia.offline.mediacontainer.DivinaExtractor
import snd.komelia.offline.sync.BookDownloadService
import snd.komelia.offline.sync.PlatformDownloadManager

internal actual fun createDivinaExtractors(): List<DivinaExtractor> {
    TODO("Not yet implemented")
}

internal actual fun createPlatformDownloadManager(
    downloadService: BookDownloadService,
    androidContext: PlatformContext
): PlatformDownloadManager {
    TODO("Not yet implemented")
}