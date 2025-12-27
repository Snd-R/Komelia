package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesTable : Table("SERIES") {
    val id = text("id")
    val libraryId = text("library_id")
    val name = text("name")
    val url = text("url")
    val booksCount = integer("books_count")
    val deleted = bool("deleted")
    val oneshot = bool("oneshot")
    val createdDate = long("created_date")
    val lastModifiedDate = long("last_modified_date")
    val fileLastModifiedDate = long("file_last_modified_date")

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(libraryId, target = OfflineLibraryTable.primaryKey)
    }
}