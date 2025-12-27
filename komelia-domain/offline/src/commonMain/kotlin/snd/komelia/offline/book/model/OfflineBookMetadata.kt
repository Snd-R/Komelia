package snd.komelia.offline.book.model

import kotlinx.datetime.LocalDate
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadata
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaWebLink
import kotlin.time.Instant

data class OfflineBookMetadata(
    val bookId: KomgaBookId,
    val title: String,
    val summary: String,
    val number: String,
    val numberSort: Float,
    val releaseDate: LocalDate?,
    val authors: List<KomgaAuthor>,
    val tags: List<String>,
    val isbn: String,
    val links: List<KomgaWebLink>,

    val titleLock: Boolean,
    val summaryLock: Boolean,
    val numberLock: Boolean,
    val numberSortLock: Boolean,
    val releaseDateLock: Boolean,
    val authorsLock: Boolean,
    val tagsLock: Boolean,
    val isbnLock: Boolean,
    val linksLock: Boolean,

    val created: Instant,
    val lastModified: Instant,
)

fun KomgaBookMetadata.toOfflineBookMetadata(bookId: KomgaBookId) =
    OfflineBookMetadata(
        bookId = bookId,
        title = this.title,
        summary = this.summary,
        number = this.number,
        numberSort = this.numberSort,
        releaseDate = this.releaseDate,
        authors = this.authors,
        tags = this.tags,
        isbn = this.isbn,
        links = this.links,
        titleLock = this.titleLock,
        summaryLock = this.summaryLock,
        numberLock = this.numberLock,
        numberSortLock = this.numberSortLock,
        releaseDateLock = this.releaseDateLock,
        authorsLock = this.authorsLock,
        tagsLock = this.tagsLock,
        isbnLock = this.isbnLock,
        linksLock = this.linksLock,
        created = this.created,
        lastModified = this.lastModified
    )