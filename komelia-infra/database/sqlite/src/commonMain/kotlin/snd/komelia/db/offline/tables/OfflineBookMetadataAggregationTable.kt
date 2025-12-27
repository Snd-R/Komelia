package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataAggregationTable : Table("BOOK_METADATA_AGGREGATION") {
    val seriesId = text("series_id")
    val releaseDate = text("release_date").nullable()
    val summary = text("summary")
    val summaryNumber = text("summary_number")
    val createdDate = long("created_date")
    val lastModifiedDate = long("last_modified_date")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}