package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineThumbnailSeriesTable : Table("THUMBNAIL_SERIES") {
    val id = text("id")
    val seriesId = text("series_id")
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
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}