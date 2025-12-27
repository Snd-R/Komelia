package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookTable : Table("BOOK") {
    val id = text("id")
    val seriesId = text("series_id")
    val libraryId = text("library_id")
    val name = text("name")
    val url = text("url")
    val fileSize = long("file_size")
    val number = integer("number")
    val fileHash = text("file_hash")
    val deleted = bool("deleted")
    val oneshot = bool("oneshot")
    val createdDate = long("created_date")
    val lastModifiedDate = long("last_modified_date")

    val remoteFileModifiedDate = long("remote_file_modified_date")
    val localFileModifiedDate = long("local_file_modified_date")
    val remoteUnavailable = bool("remote_unavailable")
    val fileDownloadPath = text("file_download_path")

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(libraryId, target = OfflineLibraryTable.primaryKey)
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}