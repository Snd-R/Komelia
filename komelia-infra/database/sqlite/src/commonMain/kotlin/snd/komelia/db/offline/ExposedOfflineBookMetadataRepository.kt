package snd.komelia.db.offline

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineBookMetadataAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataLinkTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTagTable
import snd.komelia.offline.book.model.OfflineBookMetadata
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaWebLink
import kotlin.time.Instant

class ExposedOfflineBookMetadataRepository(database: Database) : OfflineBookMetadataRepository,
    ExposedRepository(database) {

    private val metadataTable = OfflineBookMetadataTable
    private val metadataAuthorsTable = OfflineBookMetadataAuthorTable
    private val metadataTagTable = OfflineBookMetadataTagTable
    private val metadataLinkTable = OfflineBookMetadataLinkTable

    override suspend fun save(metadata: OfflineBookMetadata) {
        transaction {
            OfflineBookMetadataTable.upsert {
                it[bookId] = metadata.bookId.value
                it[number] = metadata.number
                it[numberLock] = metadata.numberLock
                it[numberSort] = metadata.numberSort
                it[numberSortLock] = metadata.numberSortLock
                it[releaseDate] = metadata.releaseDate?.toString()
                it[releaseDateLock] = metadata.releaseDateLock
                it[summary] = metadata.summary
                it[summaryLock] = metadata.summaryLock
                it[title] = metadata.title
                it[titleLock] = metadata.titleLock
                it[authorsLock] = metadata.authorsLock
                it[tagsLock] = metadata.tagsLock
                it[isbn] = metadata.isbn
                it[isbnLock] = metadata.isbnLock
                it[linksLock] = metadata.linksLock
                it[createdDate] = metadata.created.epochSeconds
                it[lastModifiedDate] = metadata.lastModified.epochSeconds
            }

            OfflineBookMetadataAuthorTable.deleteWhere { OfflineBookMetadataAuthorTable.bookId.eq(metadata.bookId.value) }
            OfflineBookMetadataTagTable.deleteWhere { OfflineBookMetadataTagTable.bookId.eq(metadata.bookId.value) }
            OfflineBookMetadataLinkTable.deleteWhere { OfflineBookMetadataLinkTable.bookId.eq(metadata.bookId.value) }

            if (metadata.authors.isNotEmpty()) {
                OfflineBookMetadataAuthorTable.batchInsert(metadata.authors) { author ->
                    this[OfflineBookMetadataAuthorTable.bookId] = metadata.bookId.value
                    this[OfflineBookMetadataAuthorTable.name] = author.name
                    this[OfflineBookMetadataAuthorTable.role] = author.role
                }
            }

            if (metadata.tags.isNotEmpty()) {
                OfflineBookMetadataTagTable.batchInsert(metadata.tags) { tag ->
                    this[OfflineBookMetadataTagTable.bookId] = metadata.bookId.value
                    this[OfflineBookMetadataTagTable.tag] = tag
                }
            }

            if (metadata.links.isNotEmpty()) {
                OfflineBookMetadataLinkTable.batchInsert(metadata.links) { link ->
                    this[OfflineBookMetadataLinkTable.bookId] = metadata.bookId.value
                    this[OfflineBookMetadataLinkTable.label] = link.label
                    this[OfflineBookMetadataLinkTable.url] = link.url
                }
            }
        }
    }

    override suspend fun find(id: KomgaBookId): OfflineBookMetadata? {
        return find(listOf(id.value)).firstOrNull()
    }

    override suspend fun findAllByIds(bookIds: List<KomgaBookId>): List<OfflineBookMetadata> {
        return find(bookIds.map { it.value })
    }

    private suspend fun find(bookIds: List<String>): List<OfflineBookMetadata> {
        return transaction {
            val rows = metadataTable.selectAll()
                .where { metadataTable.bookId.inList(bookIds) }

            val authors = findAuthors(bookIds)
            val tags = findTags(bookIds)
            val links = findLinks(bookIds)

            rows.map { row ->
                val bookId = row[metadataTable.bookId]
                row.toModel(authors[bookId].orEmpty(), tags[bookId].orEmpty(), links[bookId].orEmpty())
            }
        }
    }


    private fun findAuthors(bookIds: List<String>): Map<String, List<KomgaAuthor>> {
        return metadataAuthorsTable.selectAll()
            .where { metadataAuthorsTable.bookId.inList(bookIds) }
            .groupBy(
                { it[metadataAuthorsTable.bookId] },
                {
                    KomgaAuthor(
                        it[metadataAuthorsTable.name],
                        it[metadataAuthorsTable.role]
                    )
                }
            )
    }

    private fun findTags(bookIds: List<String>): Map<String, List<String>> {
        return metadataTagTable.selectAll()
            .where { metadataTagTable.bookId.inList(bookIds) }
            .groupBy(
                { it[metadataTagTable.bookId] },
                { it[metadataTagTable.tag] }
            )
    }

    private fun findLinks(bookIds: List<String>): Map<String, List<KomgaWebLink>> {
        return metadataLinkTable.selectAll()
            .where { metadataLinkTable.bookId.inList(bookIds) }
            .groupBy(
                { it[metadataLinkTable.bookId] },
                {
                    KomgaWebLink(
                        it[metadataLinkTable.label],
                        it[metadataLinkTable.url]
                    )
                }
            )
    }


    override suspend fun get(id: KomgaBookId): OfflineBookMetadata {
        return find(id) ?: throw IllegalStateException("Metadata for book $id does not exist")
    }

    override suspend fun delete(id: KomgaBookId) {
        transaction {
            metadataAuthorsTable.deleteWhere { metadataAuthorsTable.bookId.eq(id.value) }
            metadataTagTable.deleteWhere { metadataTagTable.bookId.eq(id.value) }
            metadataLinkTable.deleteWhere { metadataLinkTable.bookId.eq(id.value) }
            metadataTable.deleteWhere { metadataTable.bookId.eq(id.value) }
        }
    }

    override suspend fun delete(bookIds: List<KomgaBookId>) {
        transaction {
            val ids = bookIds.map { it.value }
            metadataAuthorsTable.deleteWhere { metadataAuthorsTable.bookId.inList(ids) }
            metadataTagTable.deleteWhere { metadataTagTable.bookId.inList(ids) }
            metadataLinkTable.deleteWhere { metadataLinkTable.bookId.inList(ids) }
            metadataTable.deleteWhere { metadataTable.bookId.inList(ids) }
        }
    }

    private fun ResultRow.toModel(
        authors: List<KomgaAuthor>,
        tags: List<String>,
        links: List<KomgaWebLink>
    ): OfflineBookMetadata {
        return OfflineBookMetadata(
            bookId = KomgaBookId(this[OfflineBookMetadataTable.bookId]),
            title = this[OfflineBookMetadataTable.title],
            summary = this[OfflineBookMetadataTable.summary],
            number = this[OfflineBookMetadataTable.number],
            numberSort = this[OfflineBookMetadataTable.numberSort],
            releaseDate = this[OfflineBookMetadataTable.releaseDate]?.let { LocalDate.parse(it) },
            authors = authors,
            tags = tags,
            isbn = this[OfflineBookMetadataTable.isbn],
            links = links,
            titleLock = this[OfflineBookMetadataTable.titleLock],
            summaryLock = this[OfflineBookMetadataTable.summaryLock],
            numberLock = this[OfflineBookMetadataTable.numberLock],
            numberSortLock = this[OfflineBookMetadataTable.numberSortLock],
            releaseDateLock = this[OfflineBookMetadataTable.releaseDateLock],
            authorsLock = this[OfflineBookMetadataTable.authorsLock],
            tagsLock = this[OfflineBookMetadataTable.tagsLock],
            isbnLock = this[OfflineBookMetadataTable.isbnLock],
            linksLock = this[OfflineBookMetadataTable.linksLock],
            created = Instant.fromEpochSeconds(this[OfflineBookMetadataTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[OfflineBookMetadataTable.lastModifiedDate])
        )

    }
}
