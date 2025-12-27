package snd.komelia.db.offline.dto

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.IntegerColumnType
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Sum
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.case
import org.jetbrains.exposed.v1.core.countDistinct
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.conditions.BookSearchHelper
import snd.komelia.db.offline.conditions.RequiredJoin
import snd.komelia.db.offline.offset
import snd.komelia.db.offline.page
import snd.komelia.db.offline.tables.OfflineBookMetadataAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataLinkTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTagTable
import snd.komelia.db.offline.tables.OfflineBookTable
import snd.komelia.db.offline.tables.OfflineLibraryTable
import snd.komelia.db.offline.tables.OfflineMediaServerTable
import snd.komelia.db.offline.tables.OfflineMediaTable
import snd.komelia.db.offline.tables.OfflineReadProgressTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTable
import snd.komelia.db.offline.tables.OfflineSeriesTable
import snd.komelia.db.offline.tables.OfflineUserTable
import snd.komelia.db.offline.toSortField
import snd.komelia.formatDecimal
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.api.repository.OfflineBookDtoRepository
import snd.komelia.offline.user.model.OfflineUser
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadata
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.Media
import snd.komga.client.book.MediaProfile
import snd.komga.client.book.ReadProgress
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

class ExposedOfflineBookDtoRepository(
    database: Database
) : OfflineBookDtoRepository, ExposedRepository(database) {

    private val bookTable = OfflineBookTable
    private val mediaTable = OfflineMediaTable
    private val bookMetaTable = OfflineBookMetadataTable
    private val readProgressTable = OfflineReadProgressTable
    private val bookMetaAuthorsTable = OfflineBookMetadataAuthorTable
    private val bookMetaTagTable = OfflineBookMetadataTagTable
    private val bookMetaLinkTable = OfflineBookMetadataLinkTable
    private val seriesTable = OfflineSeriesTable
    private val seriesMetaTable = OfflineSeriesMetadataTable

    private val libraryTable = OfflineLibraryTable
    private val serverTable = OfflineMediaServerTable
    private val userTable = OfflineUserTable

    private val sorts = mapOf(
        "name" to bookTable.name,
        "series" to seriesMetaTable.titleSort,
        "created" to bookTable.createdDate,
        "createdDate" to bookTable.createdDate,
        "lastModified" to bookTable.lastModifiedDate,
        "lastModifiedDate" to bookTable.lastModifiedDate,
        "fileSize" to bookTable.fileSize,
        "size" to bookTable.fileSize,
        "fileHash" to bookTable.fileHash,
        "url" to bookTable.url,
        "media.status" to mediaTable.status,
        "media.comment" to mediaTable.comment,
        "media.mediaType" to mediaTable.mediaType,
        "media.pagesCount" to mediaTable.pageCount,
        "metadata.title" to bookMetaTable.title,
        "metadata.numberSort" to bookMetaTable.numberSort,
        "metadata.releaseDate" to bookMetaTable.releaseDate,
        "readProgress.lastModified" to readProgressTable.lastModifiedDate,
        "readProgress.readDate" to readProgressTable.readDate,
//            "readList.number" to rlb.NUMBER,
    )

    private fun serverLibrariesCondition(userId: KomgaUserId): Query {
        return serverTable
            .join(
                otherTable = userTable,
                joinType = JoinType.LEFT,
                onColumn = serverTable.id,
                otherColumn = userTable.serverId,
            )
            .join(
                otherTable = libraryTable,
                joinType = JoinType.LEFT,
                onColumn = serverTable.id,
                otherColumn = libraryTable.mediaServerId,
            )
            .select(libraryTable.id)
            .where { userTable.id.eq(userId.value) }


    }

    override suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomeliaBook> {
        return findAll(userId, KomgaBookSearch(), pageRequest)
    }

    override suspend fun findAll(
        userId: KomgaUserId,
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest
    ): Page<KomeliaBook> {
        return transaction {
            val (conditions, joins) = BookSearchHelper(userId).toCondition(search.condition)
            findAll(
                conditions = conditions,
                userId = userId,
                pageRequest = pageRequest,
                searchTerm = search.fullTextSearch,
                joins = joins
            )
        }
    }

    private fun findAll(
        conditions: Op<Boolean>,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest,
        searchTerm: String?,
        joins: Set<RequiredJoin>
    ): Page<KomeliaBook> {

        val librariesCondition = serverLibrariesCondition(userId)

        val count = bookTable
            .join(
                otherTable = mediaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = mediaTable.bookId,
            )
            .join(
                otherTable = bookMetaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = bookMetaTable.bookId,
            )
            .join(
                otherTable = readProgressTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = readProgressTable.bookId,
                additionalConstraint = { readProgressTable.userId.eq(userId.value) }
            )
            .join(
                otherTable = seriesMetaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.seriesId,
                otherColumn = seriesMetaTable.seriesId,
            )
            .apply {
                joins.forEach { join ->
                    when (join) {
                        // TODO
                        is RequiredJoin.ReadList -> Unit
                        // always joined
                        RequiredJoin.BookMetadata -> Unit
                        RequiredJoin.Media -> Unit
                        is RequiredJoin.ReadProgress -> Unit
                        // Series joins - not needed
                        RequiredJoin.BookMetadataAggregation -> Unit
                        RequiredJoin.SeriesMetadata -> Unit
                        is RequiredJoin.Collection -> Unit
                    }
                }
            }
            .select(bookTable.id.countDistinct())
            .where { conditions }
            .apply {
                if (searchTerm != null) andWhere {
                    OfflineSeriesMetadataTable.title.like("%${searchTerm}%")
                }
                if (userId != OfflineUser.ROOT) {
                    andWhere { bookTable.libraryId.inSubQuery(librariesCondition) }
                }
            }.groupBy(bookTable.id)
            .firstOrNull()
            ?.let { it[bookTable.id.countDistinct()] } ?: 0

        val orderBy = pageRequest.sort.orders.mapNotNull {
            it.toSortField(sorts)
        }
        val result = selectBase(userId, joins)
            .where { conditions }
            .apply {
                if (searchTerm != null) andWhere {
                    OfflineSeriesMetadataTable.title.like("%${searchTerm}%")
                }
                if (userId != OfflineUser.ROOT) {
                    andWhere { bookTable.libraryId.inSubQuery(librariesCondition) }
                }
            }.orderBy(*orderBy.toTypedArray())
            .apply { if (pageRequest.unpaged == false) limit(pageRequest.size ?: 20).offset(pageRequest.offset()) }
            .fetchAndMap()


        return page(result, pageRequest, count, orderBy.isNotEmpty())
    }

    override suspend fun get(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook {
        return findByIdOrNull(bookId, userId) ?: throw IllegalStateException("Book $bookId is not found")
    }

    override suspend fun findByIdOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook? {
        return transaction {
            selectBase(userId)
                .where { bookTable.id.eq(bookId.value) }
                .fetchAndMap()
                .firstOrNull()
        }
    }

    override suspend fun findPreviousInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook? {
        return transaction {
            findSiblingSeries(bookId, userId, next = false)
        }
    }

    override suspend fun findNextInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook? {
        return transaction {
            findSiblingSeries(bookId, userId, next = true)
        }
    }


    override suspend fun findAllOnDeck(
        userId: KomgaUserId,
        filterOnLibraryIds: Collection<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest
    ): Page<KomeliaBook> {
        return transaction {

            val librariesCondition = serverLibrariesCondition(userId)
            val countUnread: Sum<Int> = Sum(
                case()
                    .When(readProgressTable.completed.isNull(), intLiteral(1))
                    .Else(0),
                IntegerColumnType()
            )
            val countRead: Sum<Int> = Sum(
                case()
                    .When(readProgressTable.completed.eq(true), intLiteral(1))
                    .Else(0),
                IntegerColumnType()
            )
            val countInProgress: Sum<Int> = Sum(
                case()
                    .When(readProgressTable.completed.eq(false), intLiteral(1))
                    .Else(0),
                IntegerColumnType()
            )

            val seriesIds = seriesTable
                .join(
                    otherTable = bookTable,
                    joinType = JoinType.LEFT,
                    onColumn = seriesTable.id,
                    otherColumn = bookTable.seriesId,
                )
                .join(
                    otherTable = readProgressTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookTable.id,
                    otherColumn = readProgressTable.bookId,
                    additionalConstraint = { readProgressTable.userId.eq(userId.value) }
                )
                .join(
                    otherTable = seriesMetaTable,
                    joinType = JoinType.LEFT,
                    onColumn = bookTable.seriesId,
                    otherColumn = seriesMetaTable.seriesId,
                )
                .select(seriesTable.id)
                .apply {
                    if (userId != OfflineUser.ROOT) {
                        where { seriesTable.libraryId.inSubQuery(librariesCondition) }
                    }
                }
                .groupBy(seriesTable.id)
                .having({
                    countUnread.greaterEq(1)
                        .and { countRead.greaterEq(1) }
                        .and { countInProgress.eq(0) }
                })
                .orderBy(readProgressTable.lastModifiedDate.max(), SortOrder.DESC)
                .map { it[seriesTable.id] }


            val pageIndex = pageRequest.pageIndex ?: 0
            val pageSize = pageRequest.size ?: 20
            val result = seriesIds
                .drop(pageIndex * pageSize)
                .take(pageSize)
                .mapNotNull { seriesId ->
                    selectBase(userId)
                        .where {
                            bookTable.seriesId.eq(seriesId)
                                .and { readProgressTable.completed.isNull() }
                        }
                        .orderBy(bookMetaTable.numberSort, SortOrder.ASC)
                        .limit(1)
                        .fetchAndMap()
                        .firstOrNull()
                }

            page(
                result,
                pageRequest,
                seriesIds.size.toLong(),
                true
            )
        }
    }

    private fun selectBase(
        userId: KomgaUserId,
        joins: Set<RequiredJoin> = emptySet()
    ): Query {
        val selectFields = bookTable.columns +
                bookMetaTable.columns +
                mediaTable.columns +
                readProgressTable.columns +
                seriesMetaTable.title

        return bookTable
            .join(
                otherTable = mediaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = mediaTable.bookId,
            )
            .join(
                otherTable = bookMetaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = bookMetaTable.bookId,
            )
            .join(
                otherTable = readProgressTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = readProgressTable.bookId,
                additionalConstraint = { readProgressTable.userId.eq(userId.value) }
            )
            .join(
                otherTable = seriesMetaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.seriesId,
                otherColumn = seriesMetaTable.seriesId,
            )
            .apply {
                joins.forEach { join ->
                    when (join) {
                        // TODO
                        is RequiredJoin.ReadList -> Unit
                        // always joined
                        RequiredJoin.BookMetadata -> Unit
                        RequiredJoin.Media -> Unit
                        is RequiredJoin.ReadProgress -> Unit
                        // Series joins - not needed
                        RequiredJoin.BookMetadataAggregation -> Unit
                        RequiredJoin.SeriesMetadata -> Unit
                        is RequiredJoin.Collection -> Unit
                    }
                }
            }
            .select(selectFields)
    }

    private fun findSiblingSeries(
        bookId: KomgaBookId,
        userId: KomgaUserId,
        next: Boolean
    ): KomeliaBook? {
        val result = bookTable
            .join(
                otherTable = bookMetaTable,
                joinType = JoinType.LEFT,
                onColumn = bookTable.id,
                otherColumn = bookMetaTable.bookId,
            )
            .select(bookTable.seriesId, bookMetaTable.numberSort)
            .where { bookTable.id.eq(bookId.value) }
            .first()

        val seriesId = result[bookTable.seriesId]
        val numberSort = result[bookMetaTable.numberSort]

        return selectBase(userId)
            .where {
                bookTable.seriesId.eq(seriesId)
                    .and { bookMetaTable.numberSort.greater(numberSort) }
            }
            .orderBy(bookMetaTable.numberSort, if (next) SortOrder.ASC else SortOrder.DESC)
            .limit(1)
            .fetchAndMap()
            .firstOrNull()
    }

    private fun selectAuthors(bookIds: List<String>): Map<String, List<KomgaAuthor>> {
        return bookMetaAuthorsTable.selectAll()
            .where { bookMetaAuthorsTable.bookId.inList(bookIds) }
//            .filter { it[bookMetaAuthorsTable.name] != null }
            .groupBy(
                { it[bookMetaAuthorsTable.bookId] },
                {
                    KomgaAuthor(
                        it[bookMetaAuthorsTable.name],
                        it[bookMetaAuthorsTable.role]
                    )
                }
            )
    }

    private fun selectTags(bookIds: List<String>): Map<String, List<String>> {
        return bookMetaTagTable.selectAll()
            .where { bookMetaTagTable.bookId.inList(bookIds) }
            .groupBy(
                { it[bookMetaTagTable.bookId] },
                { it[bookMetaTagTable.tag] }
            )
    }

    private fun selectLinks(bookIds: List<String>): Map<String, List<KomgaWebLink>> {
        return bookMetaLinkTable.selectAll()
            .where { bookMetaLinkTable.bookId.inList(bookIds) }
            .groupBy(
                { it[bookMetaLinkTable.bookId] },
                {
                    KomgaWebLink(
                        it[bookMetaLinkTable.label],
                        it[bookMetaLinkTable.url]
                    )
                }
            )
    }

    private fun Query.fetchAndMap(): List<KomeliaBook> {
        val rows = this.toList()
        val bookIds = rows.map { it[bookTable.id] }

        val authors = selectAuthors(bookIds)
        val tags = selectTags(bookIds)
        val links = selectLinks(bookIds)

        return rows.map { row ->
            val bookId = row[bookTable.id]
            row.toKomeliaBook(
                authors = authors[bookId].orEmpty(),
                tags = tags[bookId].orEmpty(),
                links = links[bookId].orEmpty()
            )
        }
    }

    private fun ResultRow.toKomeliaBook(
        authors: List<KomgaAuthor>,
        tags: List<String>,
        links: List<KomgaWebLink>
    ): KomeliaBook {
        return KomeliaBook(
            id = KomgaBookId(this[bookTable.id]),
            seriesId = KomgaSeriesId(this[bookTable.seriesId]),
            seriesTitle = this[seriesMetaTable.title],
            libraryId = KomgaLibraryId(this[bookTable.libraryId]),
            name = this[bookTable.name],
            url = this[bookTable.url],
            number = this[bookTable.number],
            created = Instant.fromEpochSeconds(this[bookTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[bookTable.lastModifiedDate]),
            fileLastModified = Instant.fromEpochSeconds(this[bookTable.remoteFileModifiedDate]),
            sizeBytes = this[bookTable.fileSize],
            size = "${(this[bookTable.fileSize].toFloat() / 1024 / 1024).formatDecimal(2)}MiB",
            media = this.toKomgaMedia(),
            metadata = this.toKomgaMetadata(authors, tags, links),
            readProgress = this.toKomgaReadProgress(),
            deleted = this[bookTable.deleted],
            fileHash = this[bookTable.fileHash],
            oneshot = this[bookTable.oneshot],
            downloaded = true,
            localFileLastModified = Instant.fromEpochSeconds(this[bookTable.localFileModifiedDate]),
            remoteFileUnavailable = this[bookTable.remoteUnavailable]
        )
    }

    private fun ResultRow.toKomgaMetadata(
        authors: List<KomgaAuthor>,
        tags: List<String>,
        links: List<KomgaWebLink>
    ): KomgaBookMetadata {
        return KomgaBookMetadata(
            title = this[bookMetaTable.title],
            summary = this[bookMetaTable.summary],
            number = this[bookMetaTable.number],
            numberSort = this[bookMetaTable.numberSort],
            releaseDate = this[bookMetaTable.releaseDate]?.let { LocalDate.parse(it) },
            authors = authors,
            tags = tags,
            isbn = this[bookMetaTable.isbn],
            links = links,
            titleLock = this[bookMetaTable.titleLock],
            summaryLock = this[bookMetaTable.summaryLock],
            numberLock = this[bookMetaTable.numberLock],
            numberSortLock = this[bookMetaTable.numberSortLock],
            releaseDateLock = this[bookMetaTable.releaseDateLock],
            authorsLock = this[bookMetaTable.authorsLock],
            tagsLock = this[bookMetaTable.tagsLock],
            isbnLock = this[bookMetaTable.isbnLock],
            linksLock = this[bookMetaTable.linksLock],
            created = Instant.fromEpochSeconds(this[bookMetaTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[bookMetaTable.lastModifiedDate]),
        )
    }

    private fun ResultRow.toKomgaMedia(): Media {
        return Media(
            status = KomgaMediaStatus.valueOf(this[mediaTable.status]),
            mediaType = this[mediaTable.status],
            pagesCount = this[mediaTable.pageCount],
            comment = this[mediaTable.comment],
            epubDivinaCompatible = this[mediaTable.epubDivinaCompatible],
            epubIsKepub = this[mediaTable.epubIsKepub],
            mediaProfile = this[mediaTable.mediaProfile]?.let { MediaProfile.valueOf(it) },
        )
    }

    private fun ResultRow.toKomgaReadProgress(): ReadProgress? {
        if (getOrNull(readProgressTable.userId) == null) return null

        return ReadProgress(
            page = this[readProgressTable.page],
            completed = this[readProgressTable.completed],
            readDate = Instant.fromEpochSeconds(this[readProgressTable.readDate]),
            deviceId = this[readProgressTable.deviceId],
            deviceName = this[readProgressTable.deviceName],
            created = Instant.fromEpochSeconds(this[readProgressTable.createdDate]),
            lastModified = Instant.fromEpochSeconds(this[readProgressTable.lastModifiedDate]),
        )
    }
}
