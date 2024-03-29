package io.github.snd_r.komelia.ui.dialogs.libraryedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komga.common.PatchValue
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.library.KomgaLibraryCreateRequest
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.library.KomgaLibraryUpdateRequest
import io.github.snd_r.komga.library.ScanInterval
import io.github.snd_r.komga.library.SeriesCover

class LibraryEditDialogViewModel(
    val library: KomgaLibrary?,
    val onDialogDismiss: () -> Unit,
    private val libraryClient: KomgaLibraryClient,
    private val appNotifications: AppNotifications,
) {


    var libraryName = mutableStateOf(library?.name ?: "")
        private set
    var libraryNameError by mutableStateOf<String?>(null)
        private set


    var rootFolder = mutableStateOf(library?.root ?: "")
        private set
    var rootFolderError by mutableStateOf<String?>(null)
        private set

    var emptyTrashAfterScan by mutableStateOf(library?.emptyTrashAfterScan ?: false)
    var scanForceModifiedTime by mutableStateOf(library?.scanForceModifiedTime ?: false)
    var scanOnStartup by mutableStateOf(library?.scanOnStartup ?: false)
    var oneshotsDirectory by mutableStateOf(library?.oneshotsDirectory ?: "")
    var scanCbx by mutableStateOf(library?.scanCbx ?: true)
    var scanEpub by mutableStateOf(library?.scanEpub ?: true)
    var scanPdf by mutableStateOf(library?.scanPdf ?: true)
    var scanInterval by mutableStateOf(library?.scanInterval ?: ScanInterval.EVERY_6H)
    var scanDirectoryExclusions by mutableStateOf(
        library?.scanDirectoryExclusions ?: listOf(
            "#recycle",
            "@eaDir",
            "@Recycle"
        )
    )

    var hashFiles by mutableStateOf(library?.hashFiles ?: true)
    var hashPages by mutableStateOf(library?.hashPages ?: false)
    var analyzeDimensions by mutableStateOf(library?.analyzeDimensions ?: true)
    var repairExtensions by mutableStateOf(library?.repairExtensions ?: false)
    var convertToCbz by mutableStateOf(library?.convertToCbz ?: false)
    var seriesCover by mutableStateOf(library?.seriesCover ?: SeriesCover.FIRST)


    var importComicInfoBook by mutableStateOf(library?.importComicInfoBook ?: true)
    var importComicInfoSeries by mutableStateOf(library?.importComicInfoSeries ?: true)
    var importComicInfoSeriesAppendVolume by mutableStateOf(library?.importComicInfoSeriesAppendVolume ?: true)
    var importComicInfoCollection by mutableStateOf(library?.importComicInfoCollection ?: true)
    var importComicInfoReadList by mutableStateOf(library?.importComicInfoReadList ?: true)
    var importEpubBook by mutableStateOf(library?.importEpubBook ?: true)
    var importEpubSeries by mutableStateOf(library?.importEpubSeries ?: true)
    var importMylarSeries by mutableStateOf(library?.importMylarSeries ?: true)
    var importLocalArtwork by mutableStateOf(library?.importLocalArtwork ?: true)
    var importBarcodeIsbn by mutableStateOf(library?.importBarcodeIsbn ?: false)


    private val generalTab = GeneralTab(this)
    private val scannerTab = ScannerTab(this)
    private val optionsTab = OptionsTab(this)
    private val metadataTab = MetadataTab(this)

    var currentTab by mutableStateOf<DialogTab>(generalTab)

    fun tabs(): List<DialogTab> = listOf(generalTab, scannerTab, optionsTab, metadataTab)

    suspend fun onNextTabSwitch() {
        if (currentTab == metadataTab) {
            onConfirmEdit()
        } else {
            val tabs = tabs()
            val currentIndex = tabs.indexOf(currentTab)
            currentTab = tabs[currentIndex + 1]
        }
    }

    fun setLibraryName(name: String) {
        libraryNameError =
            if (name.isBlank()) "Required"
            else null

        libraryName.value = name
    }

    fun setRootFolder(path: String) {
        rootFolderError =
            if (path.isBlank()) "Required"
            else null

        rootFolder.value = path
    }

    private fun stateIsValid(): Boolean {
        val validLibraryName = libraryName.value.isNotBlank()
        val validRootFolder = rootFolder.value.isNotBlank()


        if (!validLibraryName) {
            libraryNameError = "Required"
        }
        if (!validRootFolder) {
            rootFolderError = "Required"
        }

        return validLibraryName && validRootFolder
    }


    suspend fun onConfirmEdit() {
        if (!stateIsValid()) {
            currentTab = generalTab
            return
        }

        appNotifications.runCatchingToNotifications {
            val library = library
            if (library == null) {
                addLibrary()
            } else {
                editLibrary(library.id)
            }

            onDialogDismiss()
        }
    }

    private suspend fun addLibrary() {
        libraryClient.adOne(
            KomgaLibraryCreateRequest(
                name = libraryName.value,
                root = rootFolder.value,
                importComicInfoBook = importComicInfoBook,
                importComicInfoSeries = importComicInfoSeries,
                importComicInfoSeriesAppendVolume = importComicInfoSeriesAppendVolume,
                importComicInfoCollection = importComicInfoCollection,
                importComicInfoReadList = importComicInfoReadList,
                importEpubBook = importEpubBook,
                importEpubSeries = importEpubSeries,
                importMylarSeries = importMylarSeries,
                importLocalArtwork = importLocalArtwork,
                importBarcodeIsbn = importBarcodeIsbn,
                scanForceModifiedTime = scanForceModifiedTime,
                repairExtensions = repairExtensions,
                convertToCbz = convertToCbz,
                emptyTrashAfterScan = emptyTrashAfterScan,
                seriesCover = seriesCover,
                hashFiles = hashFiles,
                hashPages = hashPages,
                analyzeDimensions = analyzeDimensions,
                scanOnStartup = scanOnStartup,
                oneshotsDirectory = oneshotsDirectory,
                scanCbx = scanCbx,
                scanEpub = scanEpub,
                scanPdf = scanPdf,
                scanInterval = scanInterval,
                scanDirectoryExclusions = scanDirectoryExclusions,
            )
        )
    }

    private suspend fun editLibrary(libraryId: KomgaLibraryId) {
        libraryClient.patchOne(
            libraryId, KomgaLibraryUpdateRequest(
                name = libraryName.value,
                root = rootFolder.value,
                importComicInfoBook = importComicInfoBook,
                importComicInfoSeries = importComicInfoSeries,
                importComicInfoSeriesAppendVolume = importComicInfoSeriesAppendVolume,
                importComicInfoCollection = importComicInfoCollection,
                importComicInfoReadList = importComicInfoReadList,
                importEpubBook = importEpubBook,
                importEpubSeries = importEpubSeries,
                importMylarSeries = importMylarSeries,
                importLocalArtwork = importLocalArtwork,
                importBarcodeIsbn = importBarcodeIsbn,
                scanForceModifiedTime = scanForceModifiedTime,
                repairExtensions = repairExtensions,
                convertToCbz = convertToCbz,
                emptyTrashAfterScan = emptyTrashAfterScan,
                seriesCover = seriesCover,
                hashFiles = hashFiles,
                hashPages = hashPages,
                analyzeDimensions = analyzeDimensions,
                scanOnStartup = scanOnStartup,
                scanCbx = scanCbx,
                scanEpub = scanEpub,
                scanPdf = scanPdf,
                scanInterval = scanInterval,
                oneshotsDirectory = PatchValue.Some(oneshotsDirectory),
                scanDirectoryExclusions = PatchValue.Some(scanDirectoryExclusions)
            )
        )
    }
}
