package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineSeriesMetadataTable : Table("SERIES_METADATA") {
    val seriesId = text("series_id")
    val status = text("status")
    val statusLock = bool("status_lock")
    val title = text("title")
    val titleLock = bool("title_lock")
    val titleSort = text("title_sort")
    val titleSortLock = bool("title_sort_lock")
    val alternateTitlesLock = bool("alternate_titles_lock")
    val publisher = text("publisher")
    val publisherLock = bool("publisher_lock")
    val summary = text("summary")
    val summaryLock = bool("summary_lock")
    val readingDirection = text("reading_direction").nullable()
    val readingDirectionLock = bool("reading_direction_lock")
    val ageRating = integer("age_rating").nullable()
    val ageRatingLock = bool("age_rating_lock")
    val language = text("language")
    val languageLock = bool("language_lock")
    val genresLock = bool("genres_lock")
    val tagsLock = bool("tags_lock")
    val totalBookCount = integer("total_book_count").nullable()
    val totalBookCountLock = bool("total_book_count_lock")
    val sharingLabelsLock = bool("sharing_labels_lock")
    val linksLock = bool("links_lock")

    init {
        foreignKey(seriesId, target = OfflineSeriesTable.primaryKey)
    }
}