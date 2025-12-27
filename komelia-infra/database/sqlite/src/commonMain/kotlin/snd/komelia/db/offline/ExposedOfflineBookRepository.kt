package snd.komelia.db.offline

import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.exposed.v1.core.Exists
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineBookMetadataTable
import snd.komelia.db.offline.tables.OfflineBookTable
import snd.komelia.db.offline.tables.OfflineReadProgressTable
import snd.komelia.formatDecimal
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUserId
import java.sql.SQLException
import kotlin.time.Instant

class ExposedOfflineBookRepository(database: Database) : OfflineBookRepository, ExposedRepository(database) {

    private val bookTable = OfflineBookTable
    private val bookMetadataTable = OfflineBookMetadataTable
    private val readProgressTable = OfflineReadProgressTable

    override suspend fun save(book: OfflineBook) {
        transaction {
            bookTable.upsert {
                it[bookTable.id] = book.id.value
                it[bookTable.seriesId] = book.seriesId.value
                it[bookTable.libraryId] = book.libraryId.value
                it[bookTable.name] = book.name
                it[bookTable.url] = book.url
                it[bookTable.fileSize] = book.sizeBytes
                it[bookTable.number] = book.number
                it[bookTable.fileHash] = book.fileHash
                it[bookTable.deleted] = book.deleted
                it[bookTable.oneshot] = book.oneshot
                it[bookTable.createdDate] = book.created.epochSeconds
                it[bookTable.lastModifiedDate] = book.lastModified.epochSeconds
                it[bookTable.remoteFileModifiedDate] = book.remoteFileLastModified.epochSeconds
                it[bookTable.localFileModifiedDate] = book.localFileLastModified.epochSeconds
                it[bookTable.remoteUnavailable] = book.remoteUnavailable
                it[bookTable.fileDownloadPath] = book.fileDownloadPath.toString()
            }
        }
    }

    override suspend fun get(id: KomgaBookId): OfflineBook {
        return find(id) ?: throw SQLException("Book with id $id does not exist")
    }

    override suspend fun find(id: KomgaBookId): OfflineBook? {
        return transaction {
            bookTable
                .selectAll()
                .where { bookTable.id.eq(id.value) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun exists(id: KomgaBookId): Boolean {
        return transaction {
            val existsOp = Exists(bookTable.select(bookTable.id).where { bookTable.id.eq(id.value) })
            val result = Table.Dual
                .select(existsOp)
                .first()

            result[existsOp]
        }
    }

    override suspend fun findAllIdsBySeriesId(seriesId: KomgaSeriesId): List<KomgaBookId> {
        return transaction {
            bookTable
                .select(bookTable.id)
                .where { bookTable.seriesId.eq(seriesId.value) }
                .map { KomgaBookId(it[bookTable.id]) }
        }
    }

    override suspend fun findAllIdsByLibraryId(libraryId: KomgaLibraryId): List<KomgaBookId> {
        return transaction {
            bookTable
                .select(bookTable.id)
                .where { bookTable.libraryId.eq(libraryId.value) }
                .map { KomgaBookId(it[bookTable.id]) }
        }
    }

    override suspend fun findIn(ids: Collection<KomgaBookId>): List<OfflineBook> {
        return transaction {
            bookTable
                .selectAll()
                .where { bookTable.id.inList(ids.map { it.value }) }
                .map { it.toModel() }
        }
    }

    override suspend fun findFirstIdInSeriesOrNull(seriesId: KomgaSeriesId): KomgaBookId? {
        return transaction {
            bookTable
                .join(
                    otherTable = bookMetadataTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookTable.id,
                    otherColumn = bookMetadataTable.bookId,
                )
                .select(bookTable.id)
                .where { bookTable.seriesId.eq(seriesId.value) }
                .orderBy(bookMetadataTable.numberSort)
                .limit(1)
                .firstOrNull()
                ?.get(bookTable.id)
                ?.let { KomgaBookId(it) }
        }
    }

    override suspend fun findLastIdInSeriesOrNull(seriesId: KomgaSeriesId): KomgaBookId? {
        return transaction {
            bookTable
                .join(
                    otherTable = bookMetadataTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookTable.id,
                    otherColumn = bookMetadataTable.bookId,
                )
                .select(bookTable.id)
                .where { bookTable.seriesId.eq(seriesId.value) }
                .orderBy(bookMetadataTable.numberSort, SortOrder.DESC)
                .limit(1)
                .firstOrNull()
                ?.get(bookTable.id)
                ?.let { KomgaBookId(it) }
        }
    }

    override suspend fun findFirstUnreadIdInSeriesOrNull(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId
    ): KomgaBookId? {
        return transaction {
            bookTable
                .join(
                    otherTable = bookMetadataTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookTable.id,
                    otherColumn = bookMetadataTable.bookId,
                )
                .join(
                    otherTable = readProgressTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookTable.id,
                    otherColumn = readProgressTable.bookId,
                    additionalConstraint = { readProgressTable.userId.eq(userId.value) }
                )
                .select(bookTable.id)
                .where { bookTable.seriesId.eq(seriesId.value) }
                .andWhere {
                    readProgressTable.completed.isNull()
                        .or(readProgressTable.completed.eq(false))
                }
                .orderBy(bookMetadataTable.numberSort)
                .limit(1)
                .firstOrNull()
                ?.get(bookTable.id)
                ?.let { KomgaBookId(it) }
        }
    }

    override suspend fun findAllBySeriesIds(seriesIds: List<KomgaSeriesId>): List<OfflineBook> {
        return transaction {
            bookTable
                .selectAll()
                .where { bookTable.seriesId.inList(seriesIds.map { it.value }) }
                .map { it.toModel() }
        }
    }

    override suspend fun findAll(id: KomgaSeriesId): List<OfflineBook> {
        return transaction {
            bookTable
                .selectAll()
                .where { bookTable.seriesId.eq(id.value) }
                .map { it.toModel() }
        }
    }

    override suspend fun findAllNotDeleted(id: KomgaSeriesId): List<OfflineBook> {
        return transaction {
            bookTable
                .selectAll()
                .where {
                    bookTable.seriesId.eq(id.value)
                        .and { bookTable.remoteUnavailable.eq(false) }
                }
                .map { it.toModel() }
        }
    }

    override suspend fun delete(id: KomgaBookId) {
        transaction {
            bookTable.deleteWhere { bookTable.id.eq(id.value) }
        }
    }

    override suspend fun delete(ids: Collection<KomgaBookId>) {
        transaction {
            bookTable.deleteWhere { bookTable.id.inList(ids.map { it.value }) }
        }
    }

    private fun ResultRow.toModel(): OfflineBook {
        return OfflineBook(
            id = KomgaBookId(this[bookTable.id]),
            seriesId = KomgaSeriesId(this[bookTable.seriesId]),
            libraryId = KomgaLibraryId(this[bookTable.libraryId]),
            name = this[bookTable.name],
            number = this[bookTable.number],
            size = (this[bookTable.fileSize].toFloat() / 1024 / 1024).formatDecimal(2),
            sizeBytes = this[bookTable.fileSize],
            deleted = this[bookTable.deleted],
            fileHash = this[bookTable.fileHash],
            oneshot = this[bookTable.oneshot],
            url = this[bookTable.url],
            created = Instant.fromEpochSeconds(this[bookTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[bookTable.lastModifiedDate]),

            remoteFileLastModified = Instant.fromEpochSeconds(this[bookTable.remoteFileModifiedDate]),
            localFileLastModified = Instant.fromEpochSeconds(this[bookTable.localFileModifiedDate]),
            remoteUnavailable = this[bookTable.remoteUnavailable],
            fileDownloadPath = PlatformFile(this[bookTable.fileDownloadPath]),
        )
    }
}