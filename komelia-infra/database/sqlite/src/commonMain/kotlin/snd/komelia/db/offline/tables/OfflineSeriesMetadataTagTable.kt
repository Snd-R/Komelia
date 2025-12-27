package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesMetadataTagTable : Table("SERIES_METADATA_TAG") {
    val seriesId = text("series_id")
    val tag = text("tag")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}