package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineThumbnailBookTable : Table("THUMBNAIL_BOOK") {
    val id = text("id")
    val bookId = text("book_id")
    val thumbnail = blob("thumbnail").nullable()
    val url = text("url").nullable()
    val type = text("type")
    val selected = bool("selected")
    val mediaType = text("media_type")
    val fileSize = long("file_size")
    val width = integer("width")
    val height = integer("height")

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}