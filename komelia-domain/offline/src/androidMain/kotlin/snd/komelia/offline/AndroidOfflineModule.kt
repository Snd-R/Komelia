package snd.komelia.offline

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.offline.mediacontainer.DivinaExtractor
import snd.komelia.offline.mediacontainer.DivinaZipExtractor
import snd.komelia.offline.mediacontainer.EpubExtractor
import snd.komelia.offline.mediacontainer.EpubZipExtractor
import snd.komelia.offline.mediacontainer.ZipExtractor
import snd.komelia.offline.sync.AndroidDownloadManager
import snd.komelia.offline.sync.BookDownloadService
import snd.komelia.offline.sync.PlatformDownloadManager
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komga.client.KomgaClientFactory
import snd.komga.client.user.KomgaUser

class AndroidOfflineModule(
    repositories: OfflineRepositories,
    onlineUser: StateFlow<KomgaUser?>,
    onlineServerUrl: StateFlow<String>,
    isOffline: StateFlow<Boolean>,
    komgaClientFactory: KomgaClientFactory,
    private val context: Context,
) : OfflineModule(
    repositories = repositories,
    authenticatedUser = onlineUser,
    onlineServerUrl = onlineServerUrl,
    isOffline = isOffline,
    komgaClientFactory = komgaClientFactory,
) {
    private val zipExtractor = ZipExtractor()

    override fun createDivinaExtractors(): List<DivinaExtractor> {
        return listOf(DivinaZipExtractor(zipExtractor))
    }

    override fun createEpubExtractor(): EpubExtractor {
        return EpubZipExtractor(zipExtractor)
    }


    override fun createPlatformDownloadManager(
        downloadService: BookDownloadService,
        logJournalRepository: LogJournalRepository,
        events: MutableSharedFlow<DownloadEvent>,
    ): PlatformDownloadManager {
        return AndroidDownloadManager(
            context = context,
        )
    }
}