package snd.komelia.db.offline.dto

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.union
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.conditions.containsIgnoreCase
import snd.komelia.db.offline.offset
import snd.komelia.db.offline.page
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationTagTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTagTable
import snd.komelia.db.offline.tables.OfflineBookTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataGenreTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataSharingTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTagTable
import snd.komelia.db.offline.tables.OfflineSeriesTable
import snd.komelia.offline.api.repository.OfflineReferentialRepository
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId

class ExposedOfflineReferentialRepository(
     database: Database
) : OfflineReferentialRepository, ExposedRepository(database) {
    private val seriesTable = OfflineSeriesTable
    private val seriesMetaTable = OfflineSeriesMetadataTable
    private val seriesMetaGenresTable = OfflineSeriesMetadataGenreTable
    private val seriesMetaTagTable = OfflineSeriesMetadataTagTable
    private val seriesMetaSharingTable = OfflineSeriesMetadataSharingTable
    private val bookTable = OfflineBookTable
    private val bookMetaTagTable = OfflineBookMetadataTagTable
    private val bookAuthorsTable = OfflineBookMetadataAuthorTable
    private val bookMetaAggregationTable = OfflineBookMetadataAggregationTable
    private val bookMetaAggregationAuthorTable = OfflineBookMetadataAggregationAuthorTable
    private val bookTagAggregationTable = OfflineBookMetadataAggregationTagTable

    override suspend fun findAllAuthorsByName(search: String): List<KomgaAuthor> {
        return transaction {
            bookAuthorsTable.select(bookAuthorsTable.name, bookAuthorsTable.role)
                .where { bookAuthorsTable.name.containsIgnoreCase(search) }
                .orderBy(bookAuthorsTable.name)
                .map {
                    KomgaAuthor(
                        name = it[bookAuthorsTable.name],
                        role = it[bookAuthorsTable.role],
                    )
                }
        }
    }

    override suspend fun findAllAuthorsByNameAndLibrary(
        search: String,
        libraryId: KomgaLibraryId
    ): List<KomgaAuthor> {
        return transaction {
            bookMetaAggregationAuthorTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookMetaAggregationAuthorTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(bookMetaAggregationAuthorTable.name, bookMetaAggregationAuthorTable.role)
                .withDistinct()
                .where { bookMetaAggregationAuthorTable.name.containsIgnoreCase(search) }
                .andWhere { seriesTable.libraryId.eq(libraryId.value) }
                .orderBy(bookMetaAggregationAuthorTable.name)
                .map {
                    KomgaAuthor(
                        name = it[bookMetaAggregationAuthorTable.name],
                        role = it[bookMetaAggregationAuthorTable.role],
                    )
                }
        }
    }

    override suspend fun findAllAuthorsByNameAndCollection(
        search: String,
        collectionId: KomgaCollectionId
    ): List<KomgaAuthor> {
        //TODO
        return emptyList()
    }

    override suspend fun findAllAuthorsByNameAndSeries(
        search: String,
        seriesId: KomgaSeriesId
    ): List<KomgaAuthor> {
        return transaction {
            bookMetaAggregationAuthorTable
                .select(bookMetaAggregationAuthorTable.name, bookMetaAggregationAuthorTable.role)
                .withDistinct()
                .where { bookMetaAggregationAuthorTable.name.containsIgnoreCase(search) }
                .andWhere { bookMetaAggregationAuthorTable.seriesId.eq(seriesId.value) }
                .orderBy(bookMetaAggregationAuthorTable.name)
                .map {
                    KomgaAuthor(
                        name = it[bookMetaAggregationAuthorTable.name],
                        role = it[bookMetaAggregationAuthorTable.role],
                    )
                }
        }
    }

    override suspend fun findAllAuthorsNamesByName(search: String): List<String> {
        return transaction {
            bookAuthorsTable.select(bookAuthorsTable.name)
                .withDistinct()
                .where { bookAuthorsTable.name.containsIgnoreCase(search) }
                .orderBy(bookAuthorsTable.name)
                .map { it[bookAuthorsTable.name] }
        }
    }

    override suspend fun findAllAuthorsRoles(): List<String> {
        return transaction {
            bookAuthorsTable.select(bookAuthorsTable.role)
                .withDistinct()
                .orderBy(bookAuthorsTable.role)
                .map { it[bookAuthorsTable.role] }
        }
    }

    override suspend fun findAllAuthorsByName(
        search: String?,
        role: String?,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        return findAuthorsByName(search, role, pageRequest, null)
    }

    override suspend fun findAllAuthorsByNameAndLibraries(
        search: String?,
        role: String?,
        libraryIds: List<KomgaLibraryId>,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        return findAuthorsByName(
            search,
            role,
            pageRequest,
            FilterBy(FilterByType.LIBRARY, libraryIds.map { it.value }.toSet())
        )
    }

    override suspend fun findAllAuthorsByNameAndCollection(
        search: String?,
        role: String?,
        collectionId: KomgaCollectionId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        return findAuthorsByName(
            search,
            role,
            pageRequest,
            FilterBy(FilterByType.COLLECTION, setOf(collectionId.value))
        )
    }

    override suspend fun findAllAuthorsByNameAndSeries(
        search: String?,
        role: String?,
        seriesId: KomgaSeriesId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        return findAuthorsByName(search, role, pageRequest, FilterBy(FilterByType.SERIES, setOf(seriesId.value)))
    }

    override suspend fun findAllAuthorsByNameAndReadList(
        search: String?,
        role: String?,
        readListId: KomgaReadListId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        return findAuthorsByName(
            search,
            role,
            pageRequest,
            FilterBy(FilterByType.READLIST, setOf(readListId.value))
        )
    }

    private suspend fun findAuthorsByName(
        search: String?,
        role: String?,
        pageRequest: KomgaPageRequest,
        filterBy: FilterBy?
    ): Page<KomgaAuthor> {
        return transaction {
            val query = bookMetaAggregationAuthorTable
                .apply {
                    if (filterBy?.type == FilterByType.LIBRARY) join(
                        otherTable = seriesTable,
                        joinType = JoinType.LEFT,
                        onColumn = bookMetaAggregationAuthorTable.seriesId,
                        otherColumn = seriesTable.id,
                    )
                }
                .select(bookMetaAggregationAuthorTable.name, bookMetaAggregationAuthorTable.role)
                .withDistinct()
                .apply {
                    search?.let { andWhere { bookMetaAggregationAuthorTable.name.containsIgnoreCase(search) } }
                }
                .apply {
                    role?.let { andWhere { bookMetaAggregationAuthorTable.role.eq(role) } }
                }
                .apply {
                    filterBy?.let {
                        when (it.type) {
                            FilterByType.LIBRARY -> andWhere { seriesTable.libraryId.inList(it.ids) }
                            FilterByType.COLLECTION -> {}
                            FilterByType.SERIES -> andWhere { bookMetaAggregationAuthorTable.seriesId.inList(it.ids) }
                            FilterByType.READLIST -> {}
                        }
                    }
                }

            val count = query.count()
            val sort = bookMetaAggregationAuthorTable.name

            val items = query.orderBy(sort)
                .apply {
                    if (pageRequest.unpaged == true)
                        limit(pageRequest.size ?: 20)
                            .offset(pageRequest.offset())
                }
                .map {
                    KomgaAuthor(
                        it[bookMetaAggregationAuthorTable.name],
                        it[bookMetaAggregationAuthorTable.role]
                    )
                }
            page(items, pageRequest, count, true)
        }
    }

    private enum class FilterByType {
        LIBRARY,
        COLLECTION,
        SERIES,
        READLIST,
    }

    private data class FilterBy(
        val type: FilterByType,
        val ids: Set<String>,
    )

    override suspend fun findAllGenres(): List<String> {
        return transaction {
            seriesMetaGenresTable.select(seriesMetaGenresTable.genre)
                .withDistinct()
                .orderBy(seriesMetaGenresTable.genre)
                .map { it[seriesMetaGenresTable.genre] }
        }
    }

    override suspend fun findAllGenresByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        return transaction {
            seriesMetaGenresTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesMetaGenresTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(seriesMetaGenresTable.genre)
                .withDistinct()
                .where { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                .orderBy(seriesMetaGenresTable.genre)
                .map { it[seriesMetaGenresTable.genre] }
        }
    }

    override suspend fun findAllGenresByCollection(collectionId: KomgaCollectionId): List<String> {
        // TODO
        return emptyList()
    }

    override suspend fun findAllSeriesAndBookTags(): List<String> {
        return transaction {
            val bookTagAlias = bookMetaTagTable.tag.alias("tag")
            val seriesTagAlias = seriesMetaTagTable.tag.alias("tag")

            bookMetaTagTable
                .select(bookTagAlias)
                .union(seriesMetaTagTable.select(seriesTagAlias))
                .map { it[bookTagAlias] }
                .sortedBy { it.lowercase() }
        }
    }

    override suspend fun findAllSeriesAndBookTagsByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        return transaction {
            val bookTagAlias = bookMetaTagTable.tag.alias("tag")
            val seriesTagAlias = seriesMetaTagTable.tag.alias("tag")

            bookMetaTagTable
                .join(
                    otherTable = bookTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookMetaTagTable.bookId,
                    otherColumn = bookTable.id,
                )
                .select(bookTagAlias)
                .where { bookTable.libraryId.inList(libraryIds.map { it.value }) }
                .union(
                    seriesMetaTagTable
                        .join(
                            otherTable = seriesTable,
                            joinType = JoinType.LEFT,
                            onColumn = seriesMetaTagTable.seriesId,
                            otherColumn = seriesTable.id,
                        )
                        .select(seriesTagAlias)
                        .where { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                )
                .map { it[bookTagAlias] }
                .sortedBy { it.lowercase() }
        }
    }

    override suspend fun findAllSeriesAndBookTagsByCollection(collectionId: KomgaCollectionId): List<String> {
        // TODO
        return emptyList()
    }

    override suspend fun findAllSeriesTags(): List<String> {
        return transaction {
            seriesMetaTagTable
                .select(seriesMetaTagTable.tag)
                .orderBy(seriesMetaTagTable.tag)
                .map { it[seriesMetaTagTable.tag] }
        }
    }

    override suspend fun findAllSeriesTagsByLibrary(libraryId: KomgaLibraryId): List<String> {
        return transaction {
            seriesMetaTagTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesMetaTagTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(seriesMetaTagTable.tag)
                .where { seriesTable.libraryId.eq(libraryId.value) }
                .orderBy(seriesMetaTagTable.tag)
                .map { it[seriesMetaTagTable.tag] }
        }
    }

    override suspend fun findAllSeriesTagsByCollection(collectionId: KomgaCollectionId): List<String> {
        //TODO
        return emptyList()
    }

    override suspend fun findAllBookTags(): List<String> {
        return transaction {
            bookMetaTagTable
                .select(bookMetaTagTable.tag)
                .orderBy(bookMetaTagTable.tag)
                .map { it[bookMetaTagTable.tag] }
        }
    }

    override suspend fun findAllBookTagsBySeries(seriesId: KomgaSeriesId): List<String> {
        return transaction {
            bookMetaTagTable
                .join(
                    otherTable = bookTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookMetaTagTable.bookId,
                    otherColumn = bookTable.id,
                )
                .select(bookMetaTagTable.tag)
                .where { bookTable.seriesId.eq(seriesId.value) }
                .orderBy(bookMetaTagTable.tag)
                .map { it[bookMetaTagTable.tag] }
        }
    }

    override suspend fun findAllBookTagsByReadList(readListId: KomgaReadListId): List<String> {
        // TODO
        return emptyList()
    }

    override suspend fun findAllLanguages(): List<String> {
        return transaction {
            seriesMetaTable
                .select(seriesMetaTable.language)
                .withDistinct()
                .where { seriesMetaTable.language.neq("") }
                .orderBy(seriesMetaTable.language)
                .map { it[seriesMetaTable.language] }
        }
    }

    override suspend fun findAllLanguagesByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        return transaction {
            seriesMetaTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesMetaTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(seriesMetaTable.language)
                .withDistinct()
                .where { seriesMetaTable.language.neq("") }
                .andWhere { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                .orderBy(seriesMetaTable.language)
                .map { it[seriesMetaTable.language] }
        }
    }

    //TODO
    override suspend fun findAllLanguagesByCollection(collectionId: KomgaCollectionId): List<String> {
        return emptyList()
    }

    override suspend fun findAllPublishers(): List<String> {
        return transaction {
            seriesMetaTable
                .select(seriesMetaTable.publisher)
                .withDistinct()
                .where { seriesMetaTable.publisher.neq("") }
                .orderBy(seriesMetaTable.publisher)
                .map { it[seriesMetaTable.publisher] }
        }
    }

    //TODO this is unused. remove
    override suspend fun findAllPublishers(pageable: KomgaPageRequest): Page<String> {
        return Page.empty()
    }

    override suspend fun findAllPublishersByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        return transaction {
            seriesMetaTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesMetaTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(seriesMetaTable.publisher)
                .withDistinct()
                .where { seriesMetaTable.publisher.neq("") }
                .andWhere { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                .orderBy(seriesMetaTable.publisher)
                .map { it[seriesMetaTable.publisher] }
        }
    }

    //TODO
    override suspend fun findAllPublishersByCollection(collectionId: KomgaCollectionId): List<String> {
        return emptyList()
    }

    override suspend fun findAllAgeRatings(): List<Int?> {
        return transaction {
            seriesMetaTable
                .select(seriesMetaTable.ageRating)
                .withDistinct()
                .orderBy(seriesMetaTable.ageRating)
                .map { it[seriesMetaTable.ageRating] }
        }
    }

    override suspend fun findAllAgeRatingsByLibraries(libraryIds: List<KomgaLibraryId>): List<Int?> {
        return transaction {
            seriesMetaTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesMetaTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(seriesMetaTable.ageRating)
                .withDistinct()
                .where { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                .orderBy(seriesMetaTable.ageRating)
                .map { it[seriesMetaTable.ageRating] }
        }
    }

    // TODO
    override suspend fun findAllAgeRatingsByCollection(collectionId: KomgaCollectionId): List<Int?> {
        return emptyList()
    }

    override suspend fun findAllSeriesReleaseDates(): List<LocalDate> {
        return transaction {
            bookMetaAggregationTable
                .select(bookMetaAggregationTable.releaseDate)
                .withDistinct()
                .where { bookMetaAggregationTable.releaseDate.isNotNull() }
                .orderBy(bookMetaAggregationTable.releaseDate, SortOrder.DESC)
                .mapNotNull { it[bookMetaAggregationTable.releaseDate]?.let { LocalDate.parse(it) } }
        }
    }

    override suspend fun findAllSeriesReleaseDatesByLibraries(libraryIds: List<KomgaLibraryId>): List<LocalDate> {
        return transaction {
            bookMetaAggregationTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookMetaAggregationTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(bookMetaAggregationTable.releaseDate)
                .withDistinct()
                .where { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                .andWhere { bookMetaAggregationTable.releaseDate.isNotNull() }
                .orderBy(bookMetaAggregationTable.releaseDate, SortOrder.DESC)
                .mapNotNull { it[bookMetaAggregationTable.releaseDate]?.let { LocalDate.parse(it) } }
        }
    }

    // TODO
    override suspend fun findAllSeriesReleaseDatesByCollection(collectionId: KomgaCollectionId): List<LocalDate> {
        return emptyList()
    }

    override suspend fun findAllSharingLabels(): List<String> {
        return transaction {
            seriesMetaSharingTable
                .select(seriesMetaSharingTable.label)
                .withDistinct()
                .orderBy(seriesMetaSharingTable.label)
                .map { it[seriesMetaSharingTable.label] }
        }
    }

    override suspend fun findAllSharingLabelsByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        return transaction {
            seriesMetaSharingTable
                .join(
                    otherTable = seriesTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesMetaSharingTable.seriesId,
                    otherColumn = seriesTable.id,
                )
                .select(seriesMetaSharingTable.label)
                .withDistinct()
                .where { seriesTable.libraryId.inList(libraryIds.map { it.value }) }
                .orderBy(seriesMetaSharingTable.label)
                .map { it[seriesMetaSharingTable.label] }
        }
    }

    // TODO
    override suspend fun findAllSharingLabelsByCollection(collectionId: KomgaCollectionId): List<String> {
        return emptyList()
    }
}
