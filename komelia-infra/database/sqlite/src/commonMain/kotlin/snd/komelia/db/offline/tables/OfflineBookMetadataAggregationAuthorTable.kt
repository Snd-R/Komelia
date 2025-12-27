package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataAggregationAuthorTable : Table("BOOK_METADATA_AGGREGATION_AUTHOR") {
    val seriesId = text("series_id")
    val name = text("name")
    val role = text("role")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}