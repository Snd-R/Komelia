package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataAggregationTagTable : Table("BOOK_METADATA_AGGREGATION_TAG") {
    val seriesId = text("series_id")
    val tag = text("tag")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}