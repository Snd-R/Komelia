package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataTagTable : Table("BOOK_METADATA_TAG") {
    val bookId = text("book_id")
    val tag = text("tag")

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}