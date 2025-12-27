package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineLibraryTable : Table("LIBRARY") {
    val id = text("id")
    val mediaServerId = text("server_id")
    val name = text("name")
    val root = text("root")
    val importComicInfoBook = bool("import_comic_info_book")
    val importComicInfoSeries = bool("import_comic_info_series")
    val importComicInfoSeriesAppendVolume = bool("import_comic_info_series_append_volume")
    val importComicInfoCollection = bool("import_comic_info_collection")
    val importComicInfoReadList = bool("import_comic_info_read_list")
    val importEpubBook = bool("import_epub_book")
    val importEpubSeries = bool("import_epub_series")
    val importMylarSeries = bool("import_mylar_series")
    val importLocalArtwork = bool("import_local_artwork")
    val importBarcodeIsbn = bool("import_barcode_isbn")
    val scanForceModifiedTime = bool("scan_force_modified_time")
    val scanOnStartup = bool("scan_on_startup")
    val scanInterval = text("scan_interval")
    val scanCbx = bool("scan_cbx")
    val scanPdf = bool("scan_pdf")
    val scanEpub = bool("scan_epub")
    val repairExtensions = bool("repair_extensions")
    val convertToCbz = bool("convert_to_cbz")
    val emptyTrashAfterScan = bool("empty_trash_after_scan")
    val seriesCover = text("series_cover")
    val hashFiles = bool("hash_files")
    val hashPages = bool("hash_pages")
    val hashKoreader = bool("hash_koreader")
    val analyzeDimensions = bool("analyze_dimensions")
    val oneshotsDirectory = text("oneshots_directory").nullable()
    val unavailable = bool("unavailable")

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(mediaServerId, target = OfflineMediaServerTable.primaryKey)
    }
}