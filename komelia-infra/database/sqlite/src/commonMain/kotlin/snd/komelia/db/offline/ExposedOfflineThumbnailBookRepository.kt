package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineThumbnailBookTable
import snd.komelia.offline.book.model.OfflineThumbnailBook
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaThumbnailId

class ExposedOfflineThumbnailBookRepository(database: Database) : ExposedRepository(database),
    OfflineThumbnailBookRepository {
    private val thumbnailTable = OfflineThumbnailBookTable

    override suspend fun save(thumbnail: OfflineThumbnailBook) {
        transaction {
            thumbnailTable.upsert {
                it[thumbnailTable.id] = thumbnail.id.value
                it[thumbnailTable.bookId] = thumbnail.bookId.value
                it[thumbnailTable.thumbnail] = thumbnail.thumbnail?.let { ExposedBlob(it) }
                it[thumbnailTable.url] = thumbnail.url
                it[thumbnailTable.type] = thumbnail.type.name
                it[thumbnailTable.selected] = thumbnail.selected
                it[thumbnailTable.mediaType] = thumbnail.mediaType
                it[thumbnailTable.fileSize] = thumbnail.fileSize
                it[thumbnailTable.width] = thumbnail.width
                it[thumbnailTable.height] = thumbnail.height
            }
        }
    }

    override suspend fun find(id: KomgaThumbnailId): OfflineThumbnailBook? {
        return transaction {
            thumbnailTable.selectAll()
                .where { thumbnailTable.id.eq(id.value) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun findSelectedByBookId(bookId: KomgaBookId): OfflineThumbnailBook? {
        return transaction {
            thumbnailTable.selectAll()
                .where {
                    thumbnailTable.bookId.eq(bookId.value)
                        .and { thumbnailTable.selected.eq(true) }
                }
                .limit(1)
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun findAllByBookId(bookId: KomgaBookId): List<OfflineThumbnailBook> {
        return transaction {
            thumbnailTable.selectAll()
                .where { thumbnailTable.bookId.eq(bookId.value) }
                .map { it.toModel() }
        }
    }

    override suspend fun findAllByBookIdAndType(
        bookId: KomgaBookId,
        type: Collection<OfflineThumbnailBook.Type>
    ): List<OfflineThumbnailBook> {
        return transaction {
            thumbnailTable.selectAll().where {
                thumbnailTable.bookId.eq(bookId.value)
                    .and { thumbnailTable.type.inList(type.map { it.name }) }
            }.map { it.toModel() }
        }
    }

    override suspend fun markSelected(thumbnail: OfflineThumbnailBook) {
        transaction {

            thumbnailTable.update({
                thumbnailTable.bookId.eq(thumbnail.bookId.value)
                    .and { thumbnailTable.id.neq(thumbnail.id.value) }
            }) { it[thumbnailTable.selected] = false }

            thumbnailTable.update({
                thumbnailTable.bookId.eq(thumbnail.bookId.value)
                    .and { thumbnailTable.id.eq(thumbnail.id.value) }
            }) { it[thumbnailTable.selected] = true }
        }
    }

    override suspend fun delete(id: KomgaThumbnailId) {
        return transaction {
            thumbnailTable
                .deleteWhere { thumbnailTable.id.eq(id.value) }
        }
    }

    override suspend fun deleteByBookIdAndType(
        id: KomgaBookId,
        type: OfflineThumbnailBook.Type
    ) {
        transaction {
            thumbnailTable.deleteWhere {
                thumbnailTable.bookId.eq(id.value)
                    .and { thumbnailTable.type.eq(type.name) }
            }
        }
    }

    override suspend fun deleteAllBy(id: KomgaBookId) {
        transaction {
            thumbnailTable
                .deleteWhere { thumbnailTable.bookId.eq(id.value) }
        }
    }

    override suspend fun deleteByBookIds(bookIds: Collection<KomgaBookId>) {
        transaction {
            thumbnailTable
                .deleteWhere { thumbnailTable.bookId.inList(bookIds.map { it.value }) }
        }
    }

    fun ResultRow.toModel(): OfflineThumbnailBook {
        return OfflineThumbnailBook(
            id = KomgaThumbnailId(this[thumbnailTable.id]),
            bookId = KomgaBookId(this[thumbnailTable.bookId]),
            type = OfflineThumbnailBook.Type.valueOf(this[thumbnailTable.type]),
            selected = this[thumbnailTable.selected],
            mediaType = this[thumbnailTable.mediaType],
            fileSize = this[thumbnailTable.fileSize],
            width = this[thumbnailTable.width],
            height = this[thumbnailTable.height],
            url = this[thumbnailTable.url],
            thumbnail = this[thumbnailTable.thumbnail]?.bytes,
        )
    }
}