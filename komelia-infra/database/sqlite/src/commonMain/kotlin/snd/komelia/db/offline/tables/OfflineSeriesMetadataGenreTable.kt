package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesMetadataGenreTable : Table("SERIES_METADATA_GENRE") {
    val seriesId = text("series_id")
    val genre = text("genre")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}