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
import snd.komelia.db.offline.tables.OfflineThumbnailSeriesTable
import snd.komelia.offline.series.model.OfflineThumbnailSeries
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId

class ExposedOfflineThumbnailSeriesRepository(database: Database) : ExposedRepository(database),
    OfflineThumbnailSeriesRepository {
    private val thumbnailTable = OfflineThumbnailSeriesTable

    override suspend fun save(thumbnail: OfflineThumbnailSeries) {
        transaction {
            thumbnailTable.upsert {
                it[thumbnailTable.id] = thumbnail.id.value
                it[thumbnailTable.seriesId] = thumbnail.seriesId.value
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

    override suspend fun find(thumbnailId: KomgaThumbnailId): OfflineThumbnailSeries? {
        return transaction {
            thumbnailTable.selectAll()
                .where { thumbnailTable.id.eq(thumbnailId.value) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun findSelectedBySeriesId(seriesId: KomgaSeriesId): OfflineThumbnailSeries? {
        return transaction {
            val res = thumbnailTable.selectAll()
                .where {
                    thumbnailTable.seriesId.eq(seriesId.value)
                        .and { thumbnailTable.selected.eq(true) }
                }
                .limit(1)
                .firstOrNull()
                ?.toModel()
            res
        }
    }

    override suspend fun findAllBySeriesId(seriesId: KomgaSeriesId): List<OfflineThumbnailSeries> {
        return transaction {
            thumbnailTable.selectAll()
                .where { thumbnailTable.seriesId.eq(seriesId.value) }
                .map { it.toModel() }
        }
    }

    override suspend fun findAllBySeriesIdAndType(
        seriesId: KomgaSeriesId,
        type: OfflineThumbnailSeries.Type,
    ): List<OfflineThumbnailSeries> {
        return transaction {
            thumbnailTable.selectAll().where {
                thumbnailTable.seriesId.eq(seriesId.value)
                    .and { thumbnailTable.type.eq(type.name) }
            }.map { it.toModel() }
        }
    }

    override suspend fun markSelected(thumbnail: OfflineThumbnailSeries) {
        transaction {

            thumbnailTable.update({
                thumbnailTable.seriesId.eq(thumbnail.seriesId.value)
                    .and { thumbnailTable.id.neq(thumbnail.id.value) }
            }) { it[thumbnailTable.selected] = false }

            thumbnailTable.update({
                thumbnailTable.seriesId.eq(thumbnail.seriesId.value)
                    .and { thumbnailTable.id.eq(thumbnail.id.value) }
            }) { it[thumbnailTable.selected] = true }
        }
    }

    override suspend fun delete(thumbnailSeriesId: KomgaThumbnailId) {
        return transaction {
            thumbnailTable
                .deleteWhere { thumbnailTable.id.eq(thumbnailSeriesId.value) }
        }
    }


    override suspend fun deleteBySeriesId(seriesId: KomgaSeriesId) {
        transaction {
            thumbnailTable
                .deleteWhere { thumbnailTable.seriesId.eq(seriesId.value) }
        }
    }

    override suspend fun deleteBySeriesIds(seriesIds: List<KomgaSeriesId>) {
        transaction {
            thumbnailTable
                .deleteWhere { thumbnailTable.seriesId.inList(seriesIds.map { it.value }) }
        }
    }

    fun ResultRow.toModel(): OfflineThumbnailSeries {
        return OfflineThumbnailSeries(
            id = KomgaThumbnailId(this[thumbnailTable.id]),
            seriesId = KomgaSeriesId(this[thumbnailTable.seriesId]),
            type = OfflineThumbnailSeries.Type.valueOf(this[thumbnailTable.type]),
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
