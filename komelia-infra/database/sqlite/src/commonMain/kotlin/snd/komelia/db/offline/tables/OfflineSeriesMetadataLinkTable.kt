package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesMetadataLinkTable : Table("SERIES_METADATA_LINK") {
    val seriesId = text("series_id")
    val label = text("label")
    val url = text("url")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}