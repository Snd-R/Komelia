package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataLinkTable : Table("BOOK_METADATA_LINK") {
    val bookId = text("book_id")
    val label = text("label")
    val url = text("url")

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}