package io.github.snd_r.komga.library

import io.github.snd_r.komga.common.PatchValue
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class KomgaLibraryId(val value: String) {
    override fun toString() = value
}

@Serializable
data class KomgaLibrary(
    val id: KomgaLibraryId,
    val name: String,
    val root: String,
    val importComicInfoBook: Boolean,
    val importComicInfoSeries: Boolean,
    val importComicInfoSeriesAppendVolume: Boolean,
    val importComicInfoCollection: Boolean,
    val importComicInfoReadList: Boolean,
    val importEpubBook: Boolean,
    val importEpubSeries: Boolean,
    val importMylarSeries: Boolean,
    val importLocalArtwork: Boolean,
    val importBarcodeIsbn: Boolean,
    val scanForceModifiedTime: Boolean,
    val repairExtensions: Boolean,
    val convertToCbz: Boolean,
    val emptyTrashAfterScan: Boolean,
    val seriesCover: SeriesCover,
    val hashFiles: Boolean,
    val hashPages: Boolean,
    val analyzeDimensions: Boolean,
    val unavailable: Boolean,
    val scanOnStartup: Boolean,
    val oneshotsDirectory: String?,
    val scanCbx: Boolean,
    val scanEpub: Boolean,
    val scanPdf: Boolean,
    val scanInterval: ScanInterval,
    val scanDirectoryExclusions: List<String>
)


enum class SeriesCover {
    FIRST,
    FIRST_UNREAD_OR_FIRST,
    FIRST_UNREAD_OR_LAST,
    LAST,
}

enum class ScanInterval {
    DISABLED,
    HOURLY,
    EVERY_6H,
    EVERY_12H,
    DAILY,
    WEEKLY,
}

@Serializable
data class KomgaLibraryCreateRequest(
    val name: String,
    val root: String,
    val importComicInfoBook: Boolean = true,
    val importComicInfoSeries: Boolean = true,
    val importComicInfoCollection: Boolean = true,
    val importComicInfoReadList: Boolean = true,
    val importComicInfoSeriesAppendVolume: Boolean = true,
    val importEpubBook: Boolean = true,
    val importEpubSeries: Boolean = true,
    val importMylarSeries: Boolean = true,
    val importLocalArtwork: Boolean = true,
    val importBarcodeIsbn: Boolean = true,
    val scanForceModifiedTime: Boolean = false,
    val scanInterval: ScanInterval = ScanInterval.EVERY_6H,
    val scanOnStartup: Boolean = false,
    val scanCbx: Boolean = true,
    val scanPdf: Boolean = true,
    val scanEpub: Boolean = true,
    val scanDirectoryExclusions: List<String> = emptyList(),
    val repairExtensions: Boolean = false,
    val convertToCbz: Boolean = false,
    val emptyTrashAfterScan: Boolean = false,
    val seriesCover: SeriesCover = SeriesCover.FIRST,
    val hashFiles: Boolean = true,
    val hashPages: Boolean = false,
    val analyzeDimensions: Boolean = true,
    val oneshotsDirectory: String? = null,
)

@Serializable
data class KomgaLibraryUpdateRequest(
    val name: String? = null,
    val root: String? = null,
    val importComicInfoBook: Boolean? = null,
    val importComicInfoSeries: Boolean? = null,
    val importComicInfoSeriesAppendVolume: Boolean? = null,
    val importComicInfoCollection: Boolean? = null,
    val importComicInfoReadList: Boolean? = null,
    val importEpubBook: Boolean? = null,
    val importEpubSeries: Boolean? = null,
    val importMylarSeries: Boolean? = null,
    val importLocalArtwork: Boolean? = null,
    val importBarcodeIsbn: Boolean? = null,
    val scanForceModifiedTime: Boolean? = null,
    val repairExtensions: Boolean? = null,
    val convertToCbz: Boolean? = null,
    val emptyTrashAfterScan: Boolean? = null,
    val seriesCover: SeriesCover? = null,
    val hashFiles: Boolean? = null,
    val hashPages: Boolean? = null,
    val analyzeDimensions: Boolean? = null,
    val scanOnStartup: Boolean? = null,
    val scanCbx: Boolean? = null,
    val scanEpub: Boolean? = null,
    val scanPdf: Boolean? = null,
    val scanInterval: ScanInterval? = null,
    val oneshotsDirectory: PatchValue<String> = PatchValue.Unset,
    val scanDirectoryExclusions: PatchValue<List<String>> = PatchValue.Unset
)
