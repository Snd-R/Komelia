package snd.komelia.db.offline.tables

import org.jetbrains.exposed.v1.core.Table

object OfflineBookMetadataTable : Table("BOOK_METADATA") {
    val bookId = text("book_id")
    val number = text("number")
    val numberLock = bool("number_lock")
    val numberSort = float("number_sort")
    val numberSortLock = bool("number_sort_lock")
    val releaseDate = text("release_date").nullable()
    val releaseDateLock = bool("release_date_lock")
    val summary = text("summary")
    val summaryLock = bool("summary_lock")
    val title = text("title")
    val titleLock = bool("title_lock")
    val authorsLock = bool("authors_lock")
    val tagsLock = bool("tags_lock")
    val isbn = text("isbn")
    val isbnLock = bool("isbn_lock")
    val linksLock = bool("links_lock")
    val createdDate = long("created_date")
    val lastModifiedDate = long("last_modified_date")

    init {
        foreignKey(bookId, target = OfflineBookTable.primaryKey)
    }
}