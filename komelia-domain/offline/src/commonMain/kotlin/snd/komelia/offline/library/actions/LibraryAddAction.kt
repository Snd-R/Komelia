package snd.komelia.offline.library.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.library.model.OfflineLibrary
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryCreateRequest
import snd.komga.client.sse.KomgaEvent

class LibraryAddAction(
    private val libraryRepository: OfflineLibraryRepository,
    private val events: MutableSharedFlow<KomgaEvent>
) : OfflineAction {

    suspend fun run(request: KomgaLibraryCreateRequest, mediaServerId: OfflineMediaServerId): OfflineLibrary {
//        val offlineLibrary = TODO()
//        libraryRepository.save(offlineLibrary)
//        events.emit(KomgaEvent.LibraryAdded(offlineLibrary.id))
//        return offlineLibrary
        TODO()
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