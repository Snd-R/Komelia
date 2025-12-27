package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataAuthorTable : Table("BOOK_METADATA_AUTHOR") {
    val bookId = text("book_id")
    val name = text("name")
    val role = text("role")

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}