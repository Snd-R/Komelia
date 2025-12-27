package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineMediaPageTable : Table("MEDIA_PAGE") {
    val bookId = text("book_id")
    val number = integer("number")
    val fileName = text("file_name")
    val mediaType = text("media_type")
    val width = integer("width").nullable()
    val height = integer("height").nullable()
    val fileSize = long("file_size").nullable()

    override val primaryKey = PrimaryKey(bookId, number)

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}