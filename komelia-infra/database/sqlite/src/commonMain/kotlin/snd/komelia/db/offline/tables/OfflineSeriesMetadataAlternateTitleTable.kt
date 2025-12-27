package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesMetadataAlternateTitleTable : Table("SERIES_METADATA_ALTERNATE_TITLE") {
    val seriesId = text("series_id")
    val title = text("title")
    val label = text("label")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}