package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineSeriesTable
import snd.komelia.offline.series.model.OfflineSeries
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Instant

class ExposedOfflineSeriesRepository(database: Database) : ExposedRepository(database), OfflineSeriesRepository {

    private val seriesTable = OfflineSeriesTable

    override suspend fun save(series: OfflineSeries) {
        transaction {
            seriesTable.upsert {
                it[seriesTable.id] = series.id.value
                it[seriesTable.libraryId] = series.libraryId.value
                it[seriesTable.name] = series.name
                it[seriesTable.url] = series.url
                it[seriesTable.booksCount] = series.bookCount
                it[seriesTable.deleted] = series.deleted
                it[seriesTable.oneshot] = series.oneshot
                it[seriesTable.createdDate] = series.created.epochSeconds
                it[seriesTable.lastModifiedDate] = series.lastModified.epochSeconds
                it[seriesTable.fileLastModifiedDate] = series.fileLastModified.epochSeconds
            }
        }
    }

    override suspend fun get(id: KomgaSeriesId): OfflineSeries {
        return find(id) ?: throw IllegalStateException("Series with id $id does not exist")
    }

    override suspend fun find(id: KomgaSeriesId): OfflineSeries? {
        return transaction {
            seriesTable.selectAll().where { seriesTable.id.eq(id.value) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun findAllByLibraryId(libraryId: KomgaLibraryId): List<OfflineSeries> {
        return transaction {
            seriesTable.selectAll()
                .where { seriesTable.libraryId.eq(libraryId.value) }
                .map { it.toModel() }
        }
    }

    override suspend fun delete(id: KomgaSeriesId) {
        transaction {
            seriesTable.deleteWhere { seriesTable.id.eq(id.value) }
        }
    }

    override suspend fun delete(seriesids: List<KomgaSeriesId>) {
        transaction {
            seriesTable.deleteWhere { seriesTable.id.inList(seriesids.map { it.value }) }
        }
    }

    private fun ResultRow.toModel(): OfflineSeries {
        return OfflineSeries(
            id = KomgaSeriesId(this[seriesTable.id]),
            libraryId = KomgaLibraryId(this[seriesTable.libraryId]),
            name = this[seriesTable.name],
            url = this[seriesTable.url],
            oneshot = this[seriesTable.oneshot],
            bookCount = this[seriesTable.booksCount],
            deleted = this[seriesTable.deleted],
            created = Instant.fromEpochSeconds(this[seriesTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[seriesTable.lastModifiedDate]),
            fileLastModified = Instant.fromEpochSeconds(this[seriesTable.fileLastModifiedDate]),
        )
    }
}