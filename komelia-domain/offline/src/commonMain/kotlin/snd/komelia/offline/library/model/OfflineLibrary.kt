package snd.komelia.offline.library.model

import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.ScanInterval
import snd.komga.client.library.SeriesCover
import kotlin.uuid.Uuid

data class OfflineLibrary(
    val id: KomgaLibraryId = KomgaLibraryId(Uuid.generateV4().toHexDashString()),
    val mediaServerId: OfflineMediaServerId,
    val name: String,
    val root: String,
    val importComicInfoBook: Boolean,
    val importComicInfoSeries: Boolean,
    val importComicInfoCollection: Boolean,
    val importComicInfoReadList: Boolean,
    val importComicInfoSeriesAppendVolume: Boolean,
    val importEpubBook: Boolean,
    val importEpubSeries: Boolean,
    val importMylarSeries: Boolean,
    val importLocalArtwork: Boolean,
    val importBarcodeIsbn: Boolean,
    val scanForceModifiedTime: Boolean,
    val scanInterval: ScanInterval,
    val scanOnStartup: Boolean,
    val scanCbx: Boolean,
    val scanPdf: Boolean,
    val scanEpub: Boolean,
    val scanDirectoryExclusions: List<String>,
    val repairExtensions: Boolean,
    val convertToCbz: Boolean,
    val emptyTrashAfterScan: Boolean,
    val seriesCover: SeriesCover,
    val hashFiles: Boolean,
    val hashPages: Boolean,
    val hashKoreader: Boolean,
    val analyzeDimensions: Boolean,
    val oneshotsDirectory: String?,
    val unavailable: Boolean,
) {
}

fun KomgaLibrary.toOfflineLibrary(serverId: OfflineMediaServerId) = OfflineLibrary(
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
