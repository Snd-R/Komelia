package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.IntegerColumnType
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Sum
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.andIfNotNull
import org.jetbrains.exposed.v1.core.case
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineBookTable
import snd.komelia.db.offline.tables.OfflineLibraryTable
import snd.komelia.db.offline.tables.OfflineReadProgressSeriesTable
import snd.komelia.db.offline.tables.OfflineReadProgressTable
import snd.komelia.offline.readprogress.OfflineReadProgress
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

class ExposedOfflineReadProgressRepository(
    database: Database
) : ExposedRepository(database), OfflineReadProgressRepository {
    private val bookTable = OfflineBookTable
    private val progressTable = OfflineReadProgressTable
    private val seriesProgressTable = OfflineReadProgressSeriesTable
    private val libraryTable = OfflineLibraryTable

    override suspend fun save(readProgress: OfflineReadProgress) {
        transaction {
            progressTable.upsert {
                it[progressTable.bookId] = readProgress.bookId.value
                it[progressTable.userId] = readProgress.userId.value
                it[progressTable.page] = readProgress.page
                it[progressTable.completed] = readProgress.completed
                it[progressTable.readDate] = readProgress.readDate.epochSeconds
                it[progressTable.deviceId] = readProgress.deviceId
                it[progressTable.deviceName] = readProgress.deviceName
                it[progressTable.locator] = readProgress.locator
                it[progressTable.createdDate] = readProgress.createdDate.epochSeconds
                it[progressTable.lastModifiedDate] = readProgress.createdDate.epochSeconds
            }
            aggregateSeriesProgress(listOf(readProgress.bookId), readProgress.userId)
        }
    }

    override suspend fun saveAll(readProgress: List<OfflineReadProgress>) {
        transaction {
            progressTable.batchUpsert(readProgress) { progress ->
                this[progressTable.bookId] = progress.bookId.value
                this[progressTable.userId] = progress.userId.value
                this[progressTable.page] = progress.page
                this[progressTable.completed] = progress.completed
                this[progressTable.readDate] = progress.readDate.epochSeconds
                this[progressTable.deviceId] = progress.deviceId
                this[progressTable.deviceName] = progress.deviceName
                this[progressTable.locator] = progress.locator
                this[progressTable.createdDate] = progress.createdDate.epochSeconds
                this[progressTable.lastModifiedDate] = progress.createdDate.epochSeconds
            }

            readProgress.groupBy { it.userId }
                .forEach { (userId, readProgresses) ->
                    aggregateSeriesProgress(readProgresses.map { it.bookId }, userId)
                }
        }
    }

    override suspend fun find(bookId: KomgaBookId, userId: KomgaUserId): OfflineReadProgress? {
        return transaction {
            progressTable
                .selectAll()
                .where {
                    progressTable.bookId.eq(bookId.value)
                        .and { progressTable.userId.eq(userId.value) }
                }.firstOrNull()
                ?.toModel()
        }
    }


    override suspend fun findAllModifiedAfter(
        timestamp: Instant,
        userId: KomgaUserId,
        serverId: OfflineMediaServerId,
    ): List<OfflineReadProgress> {
        return transaction {
            progressTable
                .join(
                    otherTable = bookTable,
                    joinType = JoinType.LEFT,
                    onColumn = progressTable.bookId,
                    otherColumn = bookTable.id,
                )
                .join(
                    otherTable = libraryTable,
                    joinType = JoinType.RIGHT,
                    onColumn = bookTable.libraryId,
                    otherColumn = libraryTable.id,
                )
                .select(progressTable.columns)
                .where {
                    libraryTable.mediaServerId.eq(serverId.value)
                        .and(progressTable.lastModifiedDate.greater(timestamp.epochSeconds))
                        .and(progressTable.userId.eq(userId.value))
                }
                .map { it.toModel() }
        }
    }

    override suspend fun findAllByBookIdsAndUserId(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId
    ): List<OfflineReadProgress> {
        return transaction {
            progressTable
                .selectAll()
                .where {
                    progressTable.bookId.inList(bookIds.map { it.value })
                        .and { progressTable.userId.eq(userId.value) }
                }.map { it.toModel() }
        }
    }

    override suspend fun findAllByServer(
        userId: KomgaUserId,
        serverId: OfflineMediaServerId,
    ): List<OfflineReadProgress> {
        return transaction {
            progressTable
                .join(
                    otherTable = bookTable,
                    joinType = JoinType.LEFT,
                    onColumn = progressTable.bookId,
                    otherColumn = bookTable.id,
                )
                .join(
                    otherTable = libraryTable,
                    joinType = JoinType.RIGHT,
                    onColumn = bookTable.libraryId,
                    otherColumn = libraryTable.id,
                )
                .select(progressTable.columns)
                .where(
                    libraryTable.mediaServerId.eq(serverId.value)
                        .and(progressTable.userId.eq(userId.value))
                )
                .map { it.toModel() }
        }
    }

    override suspend fun deleteByUserId(userId: KomgaUserId) {
        transaction {
            progressTable.deleteWhere { progressTable.userId.eq(userId.value) }
            seriesProgressTable.deleteWhere { seriesProgressTable.userId.eq(userId.value) }
        }
    }

    override suspend fun deleteByBookIdsAndUserId(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId
    ) {
        transaction {
            progressTable.deleteWhere {
                progressTable.bookId.inList(bookIds.map { it.value })
                    .and { progressTable.userId.eq(userId.value) }
            }
            aggregateSeriesProgress(bookIds, userId)
        }
    }

    override suspend fun deleteBySeriesIds(seriesIds: List<KomgaSeriesId>) {
        transaction {
            seriesProgressTable.deleteWhere {
                seriesProgressTable.seriesId.inList(seriesIds.map { it.value })
            }
        }

    }

    override suspend fun deleteByBookIds(bookIds: List<KomgaBookId>) {
        transaction {
            progressTable.deleteWhere {
                progressTable.bookId.inList(bookIds.map { it.value })
            }
            aggregateSeriesProgress(bookIds)
        }
    }

    override suspend fun delete(bookId: KomgaBookId, userId: KomgaUserId) {
        transaction {
            progressTable.deleteWhere {
                progressTable.bookId.eq(bookId.value)
                    .and { progressTable.userId.eq(userId.value) }
            }
            aggregateSeriesProgress(listOf(bookId), userId)
        }
    }

    override suspend fun deleteAllBy(bookId: KomgaBookId) {
        transaction {
            progressTable.deleteWhere {
                progressTable.bookId.eq(bookId.value)
            }
            aggregateSeriesProgress(listOf(bookId))
        }
    }

    private fun aggregateSeriesProgress(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId? = null
    ) {
        val seriesIds = bookTable
            .select(bookTable.seriesId)
            .where { bookTable.id.inList(bookIds.map { it.value }) }
            .map { it[bookTable.seriesId] }

        seriesProgressTable.deleteWhere {
            seriesProgressTable.seriesId.inList(seriesIds)
                .apply { userId?.let { and { seriesProgressTable.userId.eq(userId.value) } } }
        }

        seriesProgressTable.insert(
            bookTable.join(
                otherTable = progressTable,
                joinType = JoinType.INNER,
                onColumn = bookTable.id,
                otherColumn = progressTable.bookId
            ).select(
                bookTable.seriesId,
                progressTable.userId,
                Sum(
                    expr = case()
                        .When(progressTable.completed.eq(true), intLiteral(1))
                        .Else(0),
                    columnType = IntegerColumnType()
                ),
                Sum(
                    expr = case()
                        .When(progressTable.completed.eq(false), intLiteral(1))
                        .Else(0),
                    columnType = IntegerColumnType()
                ),
                progressTable.readDate.max(),
            ).where {
                bookTable.seriesId.inList(seriesIds)
                    .andIfNotNull(userId?.value?.let { progressTable.userId.eq(it) })
            }.groupBy(
                bookTable.seriesId,
                progressTable.userId
            )
        )
    }

    private fun ResultRow.toModel(): OfflineReadProgress {
        return OfflineReadProgress(
            bookId = KomgaBookId(this[progressTable.bookId]),
            userId = KomgaUserId(this[progressTable.userId]),
            page = this[progressTable.page],
            completed = this[progressTable.completed],
            readDate = Instant.fromEpochSeconds(this[progressTable.readDate]),
            deviceId = this[progressTable.deviceId],
            deviceName = this[progressTable.deviceName],
            locator = this[progressTable.locator],
            createdDate = Instant.fromEpochSeconds(this[progressTable.createdDate]),
            lastModifiedDate = Instant.fromEpochSeconds(this[progressTable.lastModifiedDate])
        )
    }
}