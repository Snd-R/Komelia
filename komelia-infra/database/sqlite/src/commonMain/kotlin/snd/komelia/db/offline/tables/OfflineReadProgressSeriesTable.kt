package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineReadProgressSeriesTable : Table("READ_PROGRESS_SERIES") {
    val seriesId = text("series_id")
    val userId = text("user_id")
    val readCount = integer("read_count")
    val inProgressCount = integer("in_progress_count")
    val mostRecentReadDate = long("most_recent_read_date")

    override val primaryKey = PrimaryKey(seriesId, userId)

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
        foreignKey(userId, target = OfflineUserTable.primaryKey)
    }
}