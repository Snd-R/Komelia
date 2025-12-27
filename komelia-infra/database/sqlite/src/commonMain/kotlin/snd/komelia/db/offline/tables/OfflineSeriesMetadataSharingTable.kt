package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesMetadataSharingTable : Table("SERIES_METADATA_SHARING") {
    val seriesId = text("series_id")
    val label = text("label")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}