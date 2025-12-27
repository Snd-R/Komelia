package snd.komelia.offline.library.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.library.model.OfflineLibrary
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logError
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logInfo
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komga.client.library.KomgaLibrary

class LibraryKomgaImportAction(
    private val libraryRepository: OfflineLibraryRepository,
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val logJournalRepository: LogJournalRepository,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(library: KomgaLibrary, mediaServerId: OfflineMediaServerId) {
        try {

            transactionTemplate.execute {
                doImport(library, mediaServerId)
                logJournalRepository.logInfo { "Library updated '${library.name}'" }
            }
        } catch (e: Exception) {
            logJournalRepository.logError(e) { "Library update error '${library.name}'" }
            throw e
        }
    }

    private suspend fun doImport(library: KomgaLibrary, mediaServerId: OfflineMediaServerId) {
        val mediaServer = mediaServerRepository.get(mediaServerId)
        val offlineLibrary = library.toOfflineLibrary(mediaServer.id)
        libraryRepository.save(offlineLibrary)

    }

    private fun KomgaLibrary.toOfflineLibrary(serverId: OfflineMediaServerId) = OfflineLibrary(
        id = this.id,
        mediaServerId = serverId,
        name = this.name,
        root = this.root,
        importComicInfoBook = this.importComicInfoBook,
        importComicInfoSeries = this.importComicInfoSeries,
        importComicInfoSeriesAppendVolume = this.importComicInfoSeriesAppendVolume,
        importComicInfoCollection = this.importComicInfoCollection,
        importComicInfoReadList = this.importComicInfoReadList,
        importEpubBook = this.importEpubBook,
        importEpubSeries = this.importEpubSeries,
        importMylarSeries = this.importMylarSeries,
        importLocalArtwork = this.importLocalArtwork,
        importBarcodeIsbn = this.importBarcodeIsbn,
        scanForceModifiedTime = this.scanForceModifiedTime,
        repairExtensions = this.repairExtensions,
        convertToCbz = this.convertToCbz,
        emptyTrashAfterScan = this.emptyTrashAfterScan,
        seriesCover = this.seriesCover,
        hashFiles = this.hashFiles,
        hashPages = this.hashPages,
        hashKoreader = this.hashKoreader,
        analyzeDimensions = this.analyzeDimensions,
        unavailable = this.unavailable,
        scanOnStartup = this.scanOnStartup,
        oneshotsDirectory = this.oneshotsDirectory,
        scanCbx = this.scanCbx,
        scanEpub = this.scanEpub,
        scanPdf = this.scanPdf,
        scanInterval = this.scanInterval,
        scanDirectoryExclusions = this.scanDirectoryExclusions
    )
}