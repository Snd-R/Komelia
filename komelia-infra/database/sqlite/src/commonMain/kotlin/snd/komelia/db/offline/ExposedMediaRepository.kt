package snd.komelia.db.offline

import kotlinx.serialization.InternalSerializationApi
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineMediaPageTable
import snd.komelia.db.offline.tables.OfflineMediaTable
import snd.komelia.offline.media.model.OfflineBookPage
import snd.komelia.offline.media.model.OfflineMedia
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.MediaProfile

class ExposedMediaRepository(database: Database) : ExposedRepository(database), OfflineMediaRepository {
    private val mediaTable = OfflineMediaTable
    private val pagesTable = OfflineMediaPageTable

    @OptIn(InternalSerializationApi::class)
    override suspend fun save(media: OfflineMedia) {
        transaction {
            mediaTable.upsert {
                it[mediaTable.bookId] = media.bookId.value
                it[mediaTable.status] = media.status.name
                it[mediaTable.mediaType] = media.mediaType
                it[mediaTable.mediaProfile] = media.mediaProfile?.name
                it[mediaTable.pageCount] = media.pageCount
                it[mediaTable.comment] = media.comment
                it[mediaTable.epubDivinaCompatible] = media.epubDivinaCompatible
                it[mediaTable.epubIsKepub] = media.epubIsKepub
                it[mediaTable.extension] = media.extension
            }

            savePages(media.bookId, media.pages)
        }
    }

    override suspend fun find(id: KomgaBookId): OfflineMedia? {
        return transaction {
            val mediaResult = mediaTable.selectAll()
                .where { mediaTable.bookId.eq(id.value) }
                .firstOrNull()
                ?: return@transaction null

            val pages = pagesTable.selectAll()
                .where { pagesTable.bookId.eq(id.value) }
                .orderBy(pagesTable.number, SortOrder.ASC)
                .map { it.toPageModel() }

            mediaResult.toModel(pages)
        }
    }

    override suspend fun findAll(ids: List<KomgaBookId>): List<OfflineMedia> {
        return transaction {
            mediaTable.selectAll()
                .where { mediaTable.bookId.inList(ids.map { it.value }) }
                .fetchAndMap()
        }
    }

    private fun selectPages(bookIds: List<String>): Map<String, List<OfflineBookPage>> {
        return pagesTable.selectAll()
            .where { pagesTable.bookId.inList(bookIds) }
            .groupBy(
                { it[pagesTable.bookId] },
                { it.toPageModel() }
            )
    }

    override suspend fun get(id: KomgaBookId): OfflineMedia {
        return find(id) ?: throw IllegalStateException("Media for book $id is not found")
    }

    override suspend fun delete(id: KomgaBookId) {
        transaction {
            pagesTable.deleteWhere { pagesTable.bookId.eq(id.value) }
            mediaTable.deleteWhere { mediaTable.bookId.eq(id.value) }
        }
    }

    override suspend fun delete(bookIds: List<KomgaBookId>) {
        transaction {
            val ids = bookIds.map { it.value }
            pagesTable.deleteWhere { pagesTable.bookId.inList(ids) }
            mediaTable.deleteWhere { mediaTable.bookId.inList(ids) }
        }
    }

    private fun savePages(bookId: KomgaBookId, pages: List<OfflineBookPage>) {
        pagesTable.deleteWhere { pagesTable.bookId.eq(bookId.value) }
        pagesTable.batchInsert(pages.withIndex()) { (index, page) ->
            this[pagesTable.bookId] = bookId.value
            this[pagesTable.number] = index
            this[pagesTable.fileName] = page.fileName
            this[pagesTable.mediaType] = page.mediaType
            this[pagesTable.width] = page.width
            this[pagesTable.height] = page.height
            this[pagesTable.fileSize] = page.fileSize
        }
    }

    private fun Query.fetchAndMap(): List<OfflineMedia> {
        val rows = this.toList()
        val bookIds = rows.map { it[mediaTable.bookId] }
        val pages = selectPages(bookIds)
        return rows.map { row ->
            val bookId = row[mediaTable.bookId]
            row.toModel(pages[bookId].orEmpty())
        }
    }

    private fun ResultRow.toPageModel() = OfflineBookPage(
        bookId = KomgaBookId(this[pagesTable.bookId]),
        fileName = this[pagesTable.fileName],
        mediaType = this[pagesTable.mediaType],
        width = this[pagesTable.width],
        height = this[pagesTable.height],
        fileSize = this[pagesTable.fileSize]
    )

    private fun ResultRow.toModel(pages: List<OfflineBookPage>) = OfflineMedia(
        bookId = KomgaBookId(this[mediaTable.bookId]),
        status = runCatching { KomgaMediaStatus.valueOf(this[mediaTable.status]) }
            .getOrDefault(KomgaMediaStatus.UNKNOWN),
        mediaType = this[mediaTable.mediaType],
        mediaProfile = this[mediaTable.mediaProfile]?.let { MediaProfile.valueOf(it) },
        comment = this[mediaTable.comment],
        epubDivinaCompatible = this[mediaTable.epubDivinaCompatible],
        epubIsKepub = this[mediaTable.epubIsKepub],
        pageCount = this[mediaTable.pageCount],
        pages = pages,
        extension = this[mediaTable.extension]
    )
}