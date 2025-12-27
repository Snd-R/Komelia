package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineSeriesMetadataAlternateTitleTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataGenreTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataLinkTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataSharingTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTagTable
import snd.komelia.offline.series.model.OfflineSeriesMetadata
import snd.komelia.offline.series.repository.OfflineSeriesMetadataRepository
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.series.KomgaAlternativeTitle
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesStatus

class ExposedOfflineSeriesMetadataRepository(
    database: Database
) : ExposedRepository(database), OfflineSeriesMetadataRepository {
    override suspend fun save(metadata: OfflineSeriesMetadata) {
        transaction {
            OfflineSeriesMetadataTable.upsert {
                it[OfflineSeriesMetadataTable.seriesId] = metadata.seriesId.value
                it[OfflineSeriesMetadataTable.status] = metadata.status.name
                it[OfflineSeriesMetadataTable.statusLock] = metadata.statusLock
                it[OfflineSeriesMetadataTable.title] = metadata.title
                it[OfflineSeriesMetadataTable.titleLock] = metadata.titleLock
                it[OfflineSeriesMetadataTable.titleSort] = metadata.titleSort
                it[OfflineSeriesMetadataTable.titleSortLock] = metadata.titleSortLock
                it[OfflineSeriesMetadataTable.alternateTitlesLock] = metadata.alternateTitlesLock
                it[OfflineSeriesMetadataTable.publisher] = metadata.publisher
                it[OfflineSeriesMetadataTable.publisherLock] = metadata.publisherLock
                it[OfflineSeriesMetadataTable.summary] = metadata.summary
                it[OfflineSeriesMetadataTable.summaryLock] = metadata.summaryLock
                it[OfflineSeriesMetadataTable.readingDirection] = metadata.readingDirection?.name
                it[OfflineSeriesMetadataTable.readingDirectionLock] = metadata.readingDirectionLock
                it[OfflineSeriesMetadataTable.ageRating] = metadata.ageRating
                it[OfflineSeriesMetadataTable.ageRatingLock] = metadata.ageRatingLock
                it[OfflineSeriesMetadataTable.language] = metadata.language
                it[OfflineSeriesMetadataTable.languageLock] = metadata.languageLock
                it[OfflineSeriesMetadataTable.genresLock] = metadata.genresLock
                it[OfflineSeriesMetadataTable.tagsLock] = metadata.tagsLock
                it[OfflineSeriesMetadataTable.totalBookCount] = metadata.totalBookCount
                it[OfflineSeriesMetadataTable.totalBookCountLock] = metadata.totalBookCountLock
                it[OfflineSeriesMetadataTable.sharingLabelsLock] = metadata.sharingLabelsLock
                it[OfflineSeriesMetadataTable.linksLock] = metadata.linksLock
            }
            OfflineSeriesMetadataGenreTable.deleteWhere { OfflineSeriesMetadataGenreTable.seriesId.eq(metadata.seriesId.value) }
            OfflineSeriesMetadataTagTable.deleteWhere { OfflineSeriesMetadataTagTable.seriesId.eq(metadata.seriesId.value) }
            OfflineSeriesMetadataSharingTable.deleteWhere { OfflineSeriesMetadataSharingTable.seriesId.eq(metadata.seriesId.value) }
            OfflineSeriesMetadataLinkTable.deleteWhere { OfflineSeriesMetadataLinkTable.seriesId.eq(metadata.seriesId.value) }
            OfflineSeriesMetadataAlternateTitleTable.deleteWhere {
                OfflineSeriesMetadataAlternateTitleTable.seriesId.eq(metadata.seriesId.value)
            }

            if (metadata.genres.isNotEmpty()) {
                OfflineSeriesMetadataGenreTable.batchInsert(metadata.genres) { genre ->
                    this[OfflineSeriesMetadataGenreTable.seriesId] = metadata.seriesId.value
                    this[OfflineSeriesMetadataGenreTable.genre] = genre
                }
            }
            if (metadata.tags.isNotEmpty()) {
                OfflineSeriesMetadataTagTable.batchInsert(metadata.genres) { tag ->
                    this[OfflineSeriesMetadataTagTable.seriesId] = metadata.seriesId.value
                    this[OfflineSeriesMetadataTagTable.tag] = tag
                }
            }
            if (metadata.sharingLabels.isNotEmpty()) {
                OfflineSeriesMetadataSharingTable.batchInsert(metadata.sharingLabels) { label ->
                    this[OfflineSeriesMetadataSharingTable.seriesId] = metadata.seriesId.value
                    this[OfflineSeriesMetadataSharingTable.label] = label
                }
            }

            if (metadata.links.isNotEmpty()) {
                OfflineSeriesMetadataLinkTable.batchInsert(metadata.links) { link ->
                    this[OfflineSeriesMetadataLinkTable.seriesId] = metadata.seriesId.value
                    this[OfflineSeriesMetadataLinkTable.label] = link.label
                    this[OfflineSeriesMetadataLinkTable.url] = link.url
                }
            }

            if (metadata.alternateTitles.isNotEmpty()) {
                OfflineSeriesMetadataAlternateTitleTable.batchInsert(metadata.alternateTitles) { title ->
                    this[OfflineSeriesMetadataAlternateTitleTable.seriesId] = metadata.seriesId.value
                    this[OfflineSeriesMetadataAlternateTitleTable.label] = title.label
                    this[OfflineSeriesMetadataAlternateTitleTable.title] = title.title
                }
            }


        }
    }

    override suspend fun find(id: KomgaSeriesId): OfflineSeriesMetadata? {
        return transaction {
            val seriesResult =
                OfflineSeriesMetadataTable.selectAll().where { OfflineSeriesMetadataTable.seriesId.eq(id.value) }
                    .firstOrNull() ?: return@transaction null

            seriesResult.toModel(
                genres = findGenres(id),
                tags = findTags(id),
                sharingLabels = findSharingLabels(id),
                links = findLinks(id),
                alternateTitles = findAlternateTitles(id)
            )
        }
    }

    private fun findGenres(seriesId: KomgaSeriesId) = OfflineSeriesMetadataGenreTable.selectAll()
        .where { OfflineSeriesMetadataGenreTable.seriesId.eq(seriesId.value) }
        .map { it[OfflineSeriesMetadataGenreTable.genre] }

    private fun findTags(seriesId: KomgaSeriesId) = OfflineSeriesMetadataTagTable.selectAll()
        .where { OfflineSeriesMetadataTagTable.seriesId.eq(seriesId.value) }
        .map { it[OfflineSeriesMetadataTagTable.tag] }

    private fun findSharingLabels(seriesId: KomgaSeriesId) = OfflineSeriesMetadataSharingTable.selectAll()
        .where { OfflineSeriesMetadataSharingTable.seriesId.eq(seriesId.value) }
        .map { it[OfflineSeriesMetadataSharingTable.label] }

    private fun findLinks(seriesId: KomgaSeriesId) = OfflineSeriesMetadataLinkTable.selectAll()
        .where { OfflineSeriesMetadataLinkTable.seriesId.eq(seriesId.value) }
        .map {
            KomgaWebLink(
                it[OfflineSeriesMetadataLinkTable.label],
                it[OfflineSeriesMetadataLinkTable.url]
            )
        }

    private fun findAlternateTitles(seriesId: KomgaSeriesId) = OfflineSeriesMetadataAlternateTitleTable.selectAll()
        .where { OfflineSeriesMetadataAlternateTitleTable.seriesId.eq(seriesId.value) }
        .map {
            KomgaAlternativeTitle(
                it[OfflineSeriesMetadataAlternateTitleTable.label],
                it[OfflineSeriesMetadataAlternateTitleTable.title]
            )
        }

    override suspend fun delete(id: KomgaSeriesId) {
        transaction {
            OfflineSeriesMetadataGenreTable.deleteWhere { OfflineSeriesMetadataGenreTable.seriesId.eq(id.value) }
            OfflineSeriesMetadataTagTable.deleteWhere { OfflineSeriesMetadataTagTable.seriesId.eq(id.value) }
            OfflineSeriesMetadataSharingTable.deleteWhere { OfflineSeriesMetadataSharingTable.seriesId.eq(id.value) }
            OfflineSeriesMetadataLinkTable.deleteWhere { OfflineSeriesMetadataLinkTable.seriesId.eq(id.value) }
            OfflineSeriesMetadataTable.deleteWhere { OfflineSeriesMetadataTable.seriesId.eq(id.value) }
            OfflineSeriesMetadataAlternateTitleTable.deleteWhere {
                OfflineSeriesMetadataAlternateTitleTable.seriesId.eq(id.value)
            }
        }
    }

    override suspend fun delete(seriesIds: List<KomgaSeriesId>) {
        transaction {
            val ids = seriesIds.map { it.value }
            OfflineSeriesMetadataGenreTable.deleteWhere { OfflineSeriesMetadataGenreTable.seriesId.inList(ids) }
            OfflineSeriesMetadataTagTable.deleteWhere { OfflineSeriesMetadataTagTable.seriesId.inList(ids) }
            OfflineSeriesMetadataSharingTable.deleteWhere { OfflineSeriesMetadataSharingTable.seriesId.inList(ids) }
            OfflineSeriesMetadataLinkTable.deleteWhere { OfflineSeriesMetadataLinkTable.seriesId.inList(ids) }
            OfflineSeriesMetadataTable.deleteWhere { OfflineSeriesMetadataTable.seriesId.inList(ids) }
            OfflineSeriesMetadataAlternateTitleTable.deleteWhere {
                OfflineSeriesMetadataAlternateTitleTable.seriesId.inList(ids)
            }
        }
    }

    private fun ResultRow.toModel(
        genres: List<String>,
        tags: List<String>,
        sharingLabels: List<String>,
        links: List<KomgaWebLink>,
        alternateTitles: List<KomgaAlternativeTitle>
    ): OfflineSeriesMetadata {
        return OfflineSeriesMetadata(
            seriesId = KomgaSeriesId(this[OfflineSeriesMetadataTable.seriesId]),
            status = KomgaSeriesStatus.valueOf(this[OfflineSeriesMetadataTable.status]),
            statusLock = this[OfflineSeriesMetadataTable.statusLock],
            title = this[OfflineSeriesMetadataTable.title],
            alternateTitles = alternateTitles,
            alternateTitlesLock = this[OfflineSeriesMetadataTable.alternateTitlesLock],
            titleLock = this[OfflineSeriesMetadataTable.titleLock],
            titleSort = this[OfflineSeriesMetadataTable.titleSort],
            titleSortLock = this[OfflineSeriesMetadataTable.titleSortLock],
            summary = this[OfflineSeriesMetadataTable.summary],
            summaryLock = this[OfflineSeriesMetadataTable.summaryLock],
            readingDirection = this[OfflineSeriesMetadataTable.readingDirection]?.let { KomgaReadingDirection.valueOf(it) },
            readingDirectionLock = this[OfflineSeriesMetadataTable.readingDirectionLock],
            publisher = this[OfflineSeriesMetadataTable.publisher],
            publisherLock = this[OfflineSeriesMetadataTable.publisherLock],
            ageRating = this[OfflineSeriesMetadataTable.ageRating],
            ageRatingLock = this[OfflineSeriesMetadataTable.ageRatingLock],
            language = this[OfflineSeriesMetadataTable.language],
            languageLock = this[OfflineSeriesMetadataTable.languageLock],
            genres = genres,
            genresLock = this[OfflineSeriesMetadataTable.genresLock],
            tags = tags,
            tagsLock = this[OfflineSeriesMetadataTable.tagsLock],
            totalBookCount = this[OfflineSeriesMetadataTable.totalBookCount],
            totalBookCountLock = this[OfflineSeriesMetadataTable.totalBookCountLock],
            sharingLabels = sharingLabels,
            sharingLabelsLock = this[OfflineSeriesMetadataTable.sharingLabelsLock],
            links = links,
            linksLock = this[OfflineSeriesMetadataTable.linksLock]
        )
    }
}