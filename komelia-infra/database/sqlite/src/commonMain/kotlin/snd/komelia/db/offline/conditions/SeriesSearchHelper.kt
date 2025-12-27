package snd.komelia.db.offline.conditions

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.notInSubQuery
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.union
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationTable
import snd.komelia.db.offline.tables.OfflineBookMetadataAggregationTagTable
import snd.komelia.db.offline.tables.OfflineReadProgressSeriesTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataGenreTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataSharingTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTable
import snd.komelia.db.offline.tables.OfflineSeriesMetadataTagTable
import snd.komelia.db.offline.tables.OfflineSeriesTable
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.search.KomgaSearchOperator
import snd.komga.client.search.KomgaSearchOperator.IsFalse
import snd.komga.client.search.KomgaSearchOperator.IsTrue
import snd.komga.client.user.KomgaUserId

class SeriesSearchHelper(
    val userId: KomgaUserId,
) {
    fun toCondition(searchCondition: KomgaSearchCondition.SeriesCondition?): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val search = toConditionInternal(searchCondition)
        return search.first to search.second
    }

    private fun toConditionInternal(searchCondition: KomgaSearchCondition.SeriesCondition?): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return when (searchCondition) {
            is KomgaSearchCondition.AllOfSeries -> searchCondition.toSeriesCondition()
            is KomgaSearchCondition.AnyOfSeries -> searchCondition.toSeriesCondition()
            is KomgaSearchCondition.LibraryId -> searchCondition.toLibraryIdCondition()
            is KomgaSearchCondition.Deleted -> searchCondition.toDeletedCondition()
            is KomgaSearchCondition.ReleaseDate -> searchCondition.toReleaseDateCondition()
            is KomgaSearchCondition.ReadStatus -> searchCondition.toReadStatusCondition()
            is KomgaSearchCondition.SeriesStatus -> searchCondition.toSeriesStatusCondition()
            is KomgaSearchCondition.Tag -> searchCondition.toTagCondition()
            is KomgaSearchCondition.Author -> searchCondition.toAuthorCondition()
            is KomgaSearchCondition.OneShot -> searchCondition.toOneshotCondition()
            is KomgaSearchCondition.AgeRating -> searchCondition.toAgeRatingCondition()
            is KomgaSearchCondition.CollectionId -> searchCondition.toCollectionIdCondition()
            is KomgaSearchCondition.Complete -> searchCondition.toCompleteCondition()
            is KomgaSearchCondition.Genre -> searchCondition.toGenreCondition()
            is KomgaSearchCondition.Language -> searchCondition.toLanguageCondition()
            is KomgaSearchCondition.Publisher -> searchCondition.toPublisherCondition()
            is KomgaSearchCondition.SharingLabel -> searchCondition.toSharingLabelCondition()
            is KomgaSearchCondition.Title -> searchCondition.toTitleCondition()
            is KomgaSearchCondition.TitleSort -> searchCondition.toTitleSortCondition()
            null -> Op.TRUE to emptySet()
        }
    }

    private fun KomgaSearchCondition.AllOfSeries.toSeriesCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.conditions.fold(Op.TRUE to emptySet()) { (accOp, accJoins), cond ->
            val (op, joins) = toConditionInternal(cond)
            accOp.and(op) to (accJoins + joins)
        }
    }

    private fun KomgaSearchCondition.AnyOfSeries.toSeriesCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.conditions.fold(Op.TRUE to emptySet()) { (accOp, accJoins), cond ->
            val (op, joins) = toConditionInternal(cond)
            accOp.or(op) to (accJoins + joins)
        }
    }

    private fun KomgaSearchCondition.LibraryId.toLibraryIdCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesTable.libraryId) { it.value } to emptySet()
    }

    private fun KomgaSearchCondition.Deleted.toDeletedCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineSeriesTable.deleted) to emptySet()
    }

    private fun KomgaSearchCondition.ReleaseDate.toReleaseDateCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineBookMetadataAggregationTable.releaseDate) to
                setOf(RequiredJoin.BookMetadataAggregation)
    }

    private fun KomgaSearchCondition.ReadStatus.toReadStatusCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val readCount = OfflineReadProgressSeriesTable.readCount
        val booksCount = OfflineSeriesTable.booksCount
        return when (val operator = this.operator) {
            is KomgaSearchOperator.Is ->
                when (operator.value) {
                    KomgaReadStatus.UNREAD -> readCount.isNull()
                    KomgaReadStatus.READ -> readCount.eq(booksCount)
                    KomgaReadStatus.IN_PROGRESS -> readCount.neq(booksCount)
                }


            is KomgaSearchOperator.IsNot ->
                when (operator.value) {
                    KomgaReadStatus.UNREAD -> readCount.isNotNull()
                    KomgaReadStatus.READ -> readCount.neq(booksCount).or { readCount.isNull() }
                    KomgaReadStatus.IN_PROGRESS -> readCount.eq(booksCount).or { readCount.isNull() }
                }

        } to setOf(RequiredJoin.ReadProgress(userId))
    }

    private fun KomgaSearchCondition.SeriesStatus.toSeriesStatusCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesMetadataTable.status) { it.name } to
                setOf(RequiredJoin.SeriesMetadata)
    }

    private fun KomgaSearchCondition.Tag.toTagCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val seriesId = OfflineSeriesTable.id
        val seriesMetadata = OfflineSeriesMetadataTagTable
        val bookMetaAggregation = OfflineBookMetadataAggregationTagTable
        val innerEquals = { tag: String ->
            seriesMetadata.select(seriesMetadata.seriesId)
                .where { seriesMetadata.tag.equalsIgnoreCase(tag) }
                .union(
                    bookMetaAggregation.select(bookMetaAggregation.seriesId)
                        .where { bookMetaAggregation.tag.equalsIgnoreCase(tag) }
                )
        }
        val innerAny = {
            seriesMetadata.select(seriesMetadata.seriesId)
                .where { seriesMetadata.tag.isNotNull() }
                .union(
                    bookMetaAggregation.select(bookMetaAggregation.seriesId)
                        .where { bookMetaAggregation.tag.isNotNull() }
                )
        }

        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> seriesId.inSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNot -> seriesId.notInSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNullT -> seriesId.notInSubQuery(innerAny())
            is KomgaSearchOperator.IsNotNullT -> seriesId.inSubQuery(innerAny())
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.Author.toAuthorCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val seriesId = OfflineSeriesTable.id
        val bookMetaAggregation = OfflineBookMetadataAggregationAuthorTable
        val inner = { name: String?, role: String? ->
            bookMetaAggregation.select(bookMetaAggregation.seriesId)
                .where { Op.TRUE }.apply {
                    if (name != null) andWhere { bookMetaAggregation.name.equalsIgnoreCase(name) }
                }.apply {
                    if (role != null) andWhere { bookMetaAggregation.role.equalsIgnoreCase(role) }
                }
        }

        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> {
                if (operator.value.name == null && operator.value.role == null) Op.TRUE
                else seriesId.inSubQuery(inner(operator.value.name, operator.value.role))
            }

            is KomgaSearchOperator.IsNot -> {
                if (operator.value.name == null && operator.value.role == null) Op.TRUE
                else seriesId.notInSubQuery(inner(operator.value.name, operator.value.role))
            }
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.OneShot.toOneshotCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesTable.oneshot) to emptySet()
    }

    private fun KomgaSearchCondition.AgeRating.toAgeRatingCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesMetadataTable.ageRating) to setOf(RequiredJoin.SeriesMetadata)
    }

    //TODO
    private fun KomgaSearchCondition.CollectionId.toCollectionIdCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return Op.TRUE to emptySet()
    }

    private fun KomgaSearchCondition.Complete.toCompleteCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val bookCount = OfflineSeriesMetadataTable.totalBookCount
        return when (this.operator) {
            IsFalse -> bookCount.isNotNull().and { bookCount.eq(OfflineSeriesTable.booksCount) }
            IsTrue -> bookCount.isNotNull().and { bookCount.neq(OfflineSeriesTable.booksCount) }
        } to setOf(RequiredJoin.SeriesMetadata)
    }

    private fun KomgaSearchCondition.Genre.toGenreCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val seriesId = OfflineSeriesTable.id
        val seriesGenres = OfflineSeriesMetadataGenreTable

        val innerEquals = { genre: String ->
            seriesGenres.select(seriesGenres.seriesId)
                .where { seriesGenres.genre.equalsIgnoreCase(genre) }
        }
        val innerAny = {
            seriesGenres.select(seriesGenres.seriesId)
                .where { seriesGenres.genre.isNotNull() }
        }

        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> seriesId.inSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNot -> seriesId.notInSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNullT -> seriesId.notInSubQuery(innerAny())
            is KomgaSearchOperator.IsNotNullT -> seriesId.inSubQuery(innerAny())
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.Language.toLanguageCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesMetadataTable.language) to setOf(RequiredJoin.SeriesMetadata)
    }

    private fun KomgaSearchCondition.Publisher.toPublisherCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesMetadataTable.publisher) to setOf(RequiredJoin.SeriesMetadata)
    }

    private fun KomgaSearchCondition.SharingLabel.toSharingLabelCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val seriesId = OfflineSeriesTable.id
        val sharingLabels = OfflineSeriesMetadataSharingTable
        val innerEquals = { label: String ->
            sharingLabels.select(sharingLabels.seriesId)
                .where { sharingLabels.label.equalsIgnoreCase(label) }
        }
        val innerAny = {
            sharingLabels.select(sharingLabels.seriesId).where { sharingLabels.label.isNotNull() }
        }

        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> seriesId.inSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNot -> seriesId.notInSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNotNullT -> seriesId.notInSubQuery(innerAny())
            is KomgaSearchOperator.IsNullT -> seriesId.inSubQuery(innerAny())
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.Title.toTitleCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesMetadataTable.title) to setOf(RequiredJoin.SeriesMetadata)
    }

    private fun KomgaSearchCondition.TitleSort.toTitleSortCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineSeriesMetadataTable.titleSort) to setOf(RequiredJoin.SeriesMetadata)
    }
}