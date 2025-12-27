package snd.komelia.db.offline.dto

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Random
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.countDistinct
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.conditions.RequiredJoin
import snd.komelia.db.offline.conditions.SeriesSearchHelper
import snd.komelia.db.offline.offset
import snd.komelia.db.offline.page
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationTagTable
import snd.komelia.db.offline.tables.OfflineReadProgressSeriesTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataAlternateTitleTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataGenreTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataLinkTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataSharingTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTagTable
import snd.komelia.db.offline.tables.OfflineSeriesTable
import snd.komelia.db.offline.toSortField
import snd.komelia.offline.api.repository.OfflineSeriesDtoRepository
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.common.KomgaSort
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaAlternativeTitle
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesBookMetadata
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadata
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.series.KomgaSeriesStatus
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

class ExposedSeriesDtoRepository(
    database: Database
) : OfflineSeriesDtoRepository, ExposedRepository(database) {
    private val seriesTable = OfflineSeriesTable
    private val seriesMetaTable = OfflineSeriesMetadataTable
    private val seriesProgressTable = OfflineReadProgressSeriesTable
    private val bookMetaAggregation = OfflineBookMetadataAggregationTable
    private val bookMetaAggregationAuthor = OfflineBookMetadataAggregationAuthorTable
    private val bookMetaAggregationTag = OfflineBookMetadataAggregationTagTable

    private val groupFields: List<Column<*>> = seriesTable.columns +
            seriesMetaTable.columns +
            bookMetaAggregation.columns +
            seriesProgressTable.columns

    private val sorts: Map<String, ExpressionWithColumnType<out Any?>> =
        mapOf(
            "metadata.titleSort" to seriesMetaTable.titleSort,
            "createdDate" to seriesTable.createdDate,
            "created" to seriesTable.createdDate,
            "lastModifiedDate" to seriesTable.lastModifiedDate,
            "lastModified" to seriesTable.lastModifiedDate,
            "booksMetadata.releaseDate" to bookMetaAggregation.releaseDate,
            "readDate" to OfflineReadProgressSeriesTable.mostRecentReadDate,
//            "collection.number" to cs.NUMBER,
            "name" to seriesTable.name,
            "booksCount" to seriesTable.booksCount,
            "random" to Random(),
        )


    override suspend fun get(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId
    ): KomgaSeries {
        return find(seriesId, userId) ?: throw IllegalStateException("Series $seriesId is not found")
    }

    override suspend fun find(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId
    ): KomgaSeries? {
        return transaction {
            selectBase(userId)
                .where { seriesTable.id.eq(seriesId.value) }
                .groupBy(*groupFields.toTypedArray())
                .fetchAndMap()
                .firstOrNull()
        }
    }


    override suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries> {
        return findAll(
            search = KomgaSeriesSearch(null, null),
            userId = userId,
            pageRequest = pageRequest
        )
    }

    override suspend fun findAll(
        search: KomgaSeriesSearch,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries> {
        val (conditions, joins) = SeriesSearchHelper(userId).toCondition(search.condition)
        return findAll(
            conditions = conditions,
            joins = joins,
            searchTerm = search.fullTextSearch,
            userId = userId,
            pageRequest = pageRequest,
        )
    }

    override suspend fun findAllRecentlyUpdated(
        search: KomgaSeriesSearch,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries> {
        val pageable = pageRequest.copy(sort = KomgaSort.KomgaSeriesSort.byLastModifiedDateDesc())
        val (conditions, joins) = SeriesSearchHelper(userId).toCondition(search.condition)
        val conditionsRefined = conditions.and(seriesTable.createdDate.neq(seriesTable.lastModifiedDate))

        return findAll(
            conditions = conditionsRefined,
            joins = joins,
            searchTerm = search.fullTextSearch,
            userId = userId,
            pageRequest = pageable,
        )
    }

    private suspend fun findAll(
        conditions: Op<Boolean>,
        joins: Set<RequiredJoin> = emptySet(),
        searchTerm: String? = null,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest,
    ): Page<KomgaSeries> {
        return transaction {

            val count = seriesTable
                .join(
                    otherTable = seriesMetaTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesTable.id,
                    otherColumn = seriesMetaTable.seriesId
                )
                .join(
                    otherTable = bookMetaAggregation,
                    joinType = JoinType.LEFT,
                    onColumn = seriesTable.id,
                    otherColumn = bookMetaAggregation.seriesId
                )
                .join(
                    otherTable = seriesProgressTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesTable.id,
                    otherColumn = seriesProgressTable.seriesId,
                    additionalConstraint = { seriesProgressTable.userId.eq(userId.value) }
                )
                .apply {
                    joins.forEach { join ->
                        when (join) {
                            is RequiredJoin.Collection -> Unit

                            // always joined
                            RequiredJoin.SeriesMetadata -> Unit
                            is RequiredJoin.ReadProgress -> Unit
                            // Book joins - not needed
                            RequiredJoin.BookMetadata -> Unit
                            RequiredJoin.BookMetadataAggregation -> Unit
                            RequiredJoin.Media -> Unit
                            is RequiredJoin.ReadList -> Unit
                        }
                    }
                }
                .select(seriesTable.id.countDistinct())
                .where { conditions }
                .apply {
                    if (searchTerm != null) andWhere {
                        OfflineSeriesMetadataTable.title.like("%${searchTerm}%")
                    }
                }
                .firstOrNull()
                ?.let { it[seriesTable.id.countDistinct()] } ?: 0

            val orderBy = pageRequest.sort.orders.mapNotNull {
                it.toSortField(sorts)
            }

            val result = selectBase(userId, joins)
                .where { conditions }
                .apply {
                    if (searchTerm != null) andWhere {
                        OfflineSeriesMetadataTable.title.like("%${searchTerm}%")
                    }
                }
                .orderBy(*orderBy.toTypedArray())
                .apply { if (pageRequest.unpaged == false) limit(pageRequest.size ?: 20).offset(pageRequest.offset()) }
                .fetchAndMap()

            page(result, pageRequest, count, orderBy.isNotEmpty())
        }
    }

    private fun selectBase(
        userId: KomgaUserId,
        joins: Set<RequiredJoin> = emptySet(),
    ): Query {
        return seriesTable
            .join(
                otherTable = seriesMetaTable,
                joinType = JoinType.LEFT,
                onColumn = seriesTable.id,
                otherColumn = seriesMetaTable.seriesId
            )
            .join(
                otherTable = bookMetaAggregation,
                joinType = JoinType.LEFT,
                onColumn = seriesTable.id,
                otherColumn = bookMetaAggregation.seriesId
            )
            .join(
                otherTable = seriesProgressTable,
                joinType = JoinType.LEFT,
                onColumn = seriesTable.id,
                otherColumn = seriesProgressTable.seriesId,
                additionalConstraint = { seriesProgressTable.userId.eq(userId.value) }
            )
            .apply {
                joins.forEach { join ->
                    when (join) {
                        is RequiredJoin.Collection -> Unit

                        // always joined
                        RequiredJoin.SeriesMetadata -> Unit
                        is RequiredJoin.ReadProgress -> Unit
                        // Book joins - not needed
                        RequiredJoin.BookMetadata -> Unit
                        RequiredJoin.BookMetadataAggregation -> Unit
                        RequiredJoin.Media -> Unit
                        is RequiredJoin.ReadList -> Unit
                    }
                }
            }
            .select(groupFields)
    }

    private fun selectGenres(seriesIds: List<String>): Map<String, List<String>> {
        return OfflineSeriesMetadataGenreTable.selectAll()
            .where { OfflineSeriesMetadataGenreTable.seriesId.inList(seriesIds) }
            .groupBy({ it[OfflineSeriesMetadataGenreTable.seriesId] }, { it[OfflineSeriesMetadataGenreTable.genre] })
    }

    private fun selectTags(seriesIds: List<String>): Map<String, List<String>> {
        return OfflineSeriesMetadataTagTable.selectAll()
            .where { OfflineSeriesMetadataTagTable.seriesId.inList(seriesIds) }
            .groupBy({ it[OfflineSeriesMetadataTagTable.seriesId] }, { it[OfflineSeriesMetadataTagTable.tag] })
    }

    private fun selectSharingLabels(seriesIds: List<String>): Map<String, List<String>> {
        return OfflineSeriesMetadataSharingTable.selectAll()
            .where { OfflineSeriesMetadataSharingTable.seriesId.inList(seriesIds) }
            .groupBy(
                { it[OfflineSeriesMetadataSharingTable.seriesId] },
                { it[OfflineSeriesMetadataSharingTable.label] }
            )
    }

    private fun selectLinks(seriesIds: List<String>): Map<String, List<KomgaWebLink>> {
        return OfflineSeriesMetadataLinkTable.selectAll()
            .where { OfflineSeriesMetadataLinkTable.seriesId.inList(seriesIds) }
            .groupBy(
                { it[OfflineSeriesMetadataLinkTable.seriesId] },
                {
                    KomgaWebLink(
                        it[OfflineSeriesMetadataLinkTable.label],
                        it[OfflineSeriesMetadataLinkTable.url]
                    )
                }
            )
    }

    private fun selectAlternateTitles(seriesIds: List<String>): Map<String, List<KomgaAlternativeTitle>> {
        return OfflineSeriesMetadataAlternateTitleTable.selectAll()
            .where { OfflineSeriesMetadataAlternateTitleTable.seriesId.inList(seriesIds) }
            .groupBy({ it[OfflineSeriesMetadataAlternateTitleTable.seriesId] }, {
                KomgaAlternativeTitle(
                    it[OfflineSeriesMetadataAlternateTitleTable.title],
                    it[OfflineSeriesMetadataAlternateTitleTable.label]
                )
            })
    }

    private fun selectBookTags(seriesIds: List<String>): Map<String, List<String>> {
        return bookMetaAggregationTag.selectAll()
            .where { bookMetaAggregationTag.seriesId.inList(seriesIds) }
            .groupBy(
                { it[bookMetaAggregationTag.seriesId] },
                { it[bookMetaAggregationTag.tag] }
            )

    }

    private fun selectBookAuthors(seriesIds: List<String>): Map<String, List<KomgaAuthor>> {
        return bookMetaAggregationAuthor.selectAll()
            .where { bookMetaAggregationAuthor.seriesId.inList(seriesIds) }
            .groupBy({ it[bookMetaAggregationAuthor.seriesId] }, {
                KomgaAuthor(
                    it[bookMetaAggregationAuthor.name],
                    it[bookMetaAggregationAuthor.role]
                )
            })
    }

    private fun Query.fetchAndMap(): List<KomgaSeries> {
        val rows = this.toList()
        val seriesIds = rows.map { it[OfflineSeriesTable.id] }

        val genres = selectGenres(seriesIds)
        val tags = selectTags(seriesIds)
        val sharingLabels = selectSharingLabels(seriesIds)
        val links = selectLinks(seriesIds)
        val alternativeTitles = selectAlternateTitles(seriesIds)
        val bookAuthors = selectBookAuthors(seriesIds)
        val bookTags = selectBookTags(seriesIds)

        return rows.map { row ->
            val seriesId = row[OfflineSeriesTable.id]
            row.toKomgaSeries(
                genres[seriesId].orEmpty(),
                tags[seriesId].orEmpty(),
                sharingLabels[seriesId].orEmpty(),
                links[seriesId].orEmpty(),
                alternativeTitles[seriesId].orEmpty(),
                bookAuthors[seriesId].orEmpty(),
                bookTags[seriesId].orEmpty()
            )

        }
    }

    private fun ResultRow.toKomgaSeries(
        genres: List<String>,
        tags: List<String>,
        sharingLabels: List<String>,
        links: List<KomgaWebLink>,
        alternativeTitles: List<KomgaAlternativeTitle>,
        bookAuthors: List<KomgaAuthor>,
        bookTags: List<String>,
    ): KomgaSeries {
        val bookCount = this[OfflineSeriesTable.booksCount]
        val booksReadCount = getOrNull(seriesProgressTable.readCount) ?: 0
        val booksInProgressCount = getOrNull(seriesProgressTable.inProgressCount) ?: 0
        val bookUnreadCount = bookCount - booksReadCount - booksInProgressCount
        val seriesMetadata = this.toKomgaSeriesMetadata(
            genres = genres,
            tags = tags,
            sharingLabels = sharingLabels,
            links = links,
            alternativeTitles = alternativeTitles
        )
        val bookMetadata = this.toKomgaSeriesBookMetadata(bookAuthors, bookTags)

        return KomgaSeries(
            id = KomgaSeriesId(this[OfflineSeriesTable.id]),
            libraryId = KomgaLibraryId(this[OfflineSeriesTable.libraryId]),
            name = this[OfflineSeriesTable.name],
            url = this[OfflineSeriesTable.url],
            booksCount = bookCount,
            booksReadCount = booksReadCount,
            booksUnreadCount = bookUnreadCount,
            booksInProgressCount = booksInProgressCount,
            metadata = seriesMetadata,
            deleted = this[OfflineSeriesTable.deleted],
            oneshot = this[OfflineSeriesTable.oneshot],
            booksMetadata = bookMetadata,
            created = Instant.fromEpochSeconds(this[OfflineSeriesTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[OfflineSeriesTable.lastModifiedDate]),
            fileLastModified = Instant.fromEpochSeconds(this[OfflineSeriesTable.fileLastModifiedDate]),
        )
    }

    private fun ResultRow.toKomgaSeriesMetadata(
        genres: List<String>,
        tags: List<String>,
        sharingLabels: List<String>,
        links: List<KomgaWebLink>,
        alternativeTitles: List<KomgaAlternativeTitle>,
    ) = KomgaSeriesMetadata(
        status = KomgaSeriesStatus.valueOf(this[seriesMetaTable.status]),
        statusLock = this[seriesMetaTable.statusLock],
        title = this[seriesMetaTable.title],
        titleLock = this[seriesMetaTable.titleLock],
        alternateTitles = alternativeTitles,
        alternateTitlesLock = this[seriesMetaTable.alternateTitlesLock],
        titleSort = this[seriesMetaTable.titleSort],
        titleSortLock = this[seriesMetaTable.titleSortLock],
        summary = this[seriesMetaTable.summary],
        summaryLock = this[seriesMetaTable.summaryLock],
        readingDirection = this[seriesMetaTable.readingDirection]?.let { KomgaReadingDirection.valueOf(it) },
        readingDirectionLock = this[seriesMetaTable.readingDirectionLock],
        publisher = this[seriesMetaTable.publisher],
        publisherLock = this[seriesMetaTable.publisherLock],
        ageRating = this[seriesMetaTable.ageRating],
        ageRatingLock = this[seriesMetaTable.ageRatingLock],
        language = this[seriesMetaTable.language],
        languageLock = this[seriesMetaTable.languageLock],
        genres = genres,
        genresLock = this[seriesMetaTable.genresLock],
        tags = tags,
        tagsLock = this[seriesMetaTable.tagsLock],
        totalBookCount = this[seriesMetaTable.totalBookCount],
        totalBookCountLock = this[seriesMetaTable.totalBookCountLock],
        sharingLabels = sharingLabels,
        sharingLabelsLock = this[seriesMetaTable.sharingLabelsLock],
        links = links,
        linksLock = this[seriesMetaTable.linksLock],
    )

    private fun ResultRow.toKomgaSeriesBookMetadata(
        authors: List<KomgaAuthor>,
        tags: List<String>,
    ) = KomgaSeriesBookMetadata(
        authors = authors,
        tags = tags,
        releaseDate = this[bookMetaAggregation.releaseDate]?.let { LocalDate.parse(it) },
        summary = this[bookMetaAggregation.summary],
        summaryNumber = this[bookMetaAggregation.summaryNumber],
        created = Instant.fromEpochSeconds(this[bookMetaAggregation.createdDate]),
        lastModified = Instant.fromEpochSeconds(this[bookMetaAggregation.lastModifiedDate]),
    )
}
