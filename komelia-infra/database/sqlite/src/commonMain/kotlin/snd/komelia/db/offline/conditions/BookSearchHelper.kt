package snd.komelia.db.offline.conditions

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.notInSubQuery
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import snd.komelia.db.offline.tables.OfflineBookMetadataAuthorTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTable
import snd.komelia.db.offline.tables.OfflineBookMetadataTagTable
import snd.komelia.db.offline.tables.OfflineBookTable
import snd.komelia.db.offline.tables.OfflineMediaTable
import snd.komelia.db.offline.tables.OfflineReadProgressTable
import snd.komelia.db.offline.tables.OfflineThumbnailBookTable
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.search.KomgaSearchCondition.PosterMatch
import snd.komga.client.search.KomgaSearchOperator
import snd.komga.client.user.KomgaUserId

class BookSearchHelper(
    val userId: KomgaUserId,
) {
    fun toCondition(searchCondition: KomgaSearchCondition.BookCondition?): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val search = toConditionInternal(searchCondition)
        return search.first to search.second
    }

    private fun toConditionInternal(searchCondition: KomgaSearchCondition.BookCondition?): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return when (searchCondition) {
            is KomgaSearchCondition.AllOfBook -> searchCondition.toBookCondition()
            is KomgaSearchCondition.AnyOfBook -> searchCondition.toBookCondition()
            is KomgaSearchCondition.LibraryId -> searchCondition.toLibraryIdCondition()
            is KomgaSearchCondition.SeriesId -> searchCondition.toSeriesIdCondition()
            is KomgaSearchCondition.ReadListId -> searchCondition.toReadListIdCondition()
            is KomgaSearchCondition.Title -> searchCondition.toTitleCondition()
            is KomgaSearchCondition.Deleted -> searchCondition.toDeletedCondition()
            is KomgaSearchCondition.ReleaseDate -> searchCondition.toReleaseDateCondition()
            is KomgaSearchCondition.NumberSort -> searchCondition.toNumberSortCondition()
            is KomgaSearchCondition.ReadStatus -> searchCondition.toReadStatusCondition()
            is KomgaSearchCondition.MediaStatus -> searchCondition.toMediaStatusCondition()
            is KomgaSearchCondition.MediaProfile -> searchCondition.toMediaProfileCondition()
            is KomgaSearchCondition.Tag -> searchCondition.toTagCondition()
            is KomgaSearchCondition.Author -> searchCondition.toAuthorCondition()
            is KomgaSearchCondition.Poster -> searchCondition.toPosterCondition()
            is KomgaSearchCondition.OneShot -> searchCondition.toOneshotCondition()
            null -> Op.TRUE to emptySet()
        }
    }

    private fun KomgaSearchCondition.AllOfBook.toBookCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.conditions.fold(Op.TRUE to emptySet()) { (accOp, accJoins), cond ->
            val (op, joins) = toConditionInternal(cond)
            accOp.and(op) to (accJoins + joins)
        }
    }

    private fun KomgaSearchCondition.AnyOfBook.toBookCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.conditions.fold(Op.TRUE to emptySet()) { (accOp, accJoins), cond ->
            val (op, joins) = toConditionInternal(cond)
            accOp.or(op) to (accJoins + joins)
        }
    }

    private fun KomgaSearchCondition.LibraryId.toLibraryIdCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineBookTable.libraryId) { it.value } to emptySet()
    }

    private fun KomgaSearchCondition.SeriesId.toSeriesIdCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineBookTable.seriesId) { it.value } to emptySet()
    }

    private fun KomgaSearchCondition.ReadListId.toReadListIdCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        //TODO
        return Op.TRUE to emptySet()
    }

    private fun KomgaSearchCondition.Title.toTitleCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(field = OfflineBookMetadataTable.title) to setOf(RequiredJoin.BookMetadata)
    }

    private fun KomgaSearchCondition.Deleted.toDeletedCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineBookTable.deleted) to emptySet()
    }

    private fun KomgaSearchCondition.ReleaseDate.toReleaseDateCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineBookMetadataTable.releaseDate) to
                setOf(RequiredJoin.BookMetadata)
    }

    private fun KomgaSearchCondition.NumberSort.toNumberSortCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineBookMetadataTable.numberSort) to setOf(RequiredJoin.BookMetadata)
    }

    private fun KomgaSearchCondition.ReadStatus.toReadStatusCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val completed = OfflineReadProgressTable.completed

        return when (val operator = this.operator) {
            is KomgaSearchOperator.Is ->
                when (operator.value) {
                    KomgaReadStatus.UNREAD -> completed.isNull()
                    KomgaReadStatus.READ -> completed.eq(true)
                    KomgaReadStatus.IN_PROGRESS -> completed.eq(false)
                }


            is KomgaSearchOperator.IsNot ->
                when (operator.value) {
                    KomgaReadStatus.UNREAD -> completed.isNotNull()
                    KomgaReadStatus.READ -> completed.isNull().or { completed.eq(false) }
                    KomgaReadStatus.IN_PROGRESS -> completed.isNull().or { completed.eq(true) }
                }

        } to setOf(RequiredJoin.ReadProgress(userId))
    }

    private fun KomgaSearchCondition.MediaStatus.toMediaStatusCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineMediaTable.status) { it.name } to setOf(RequiredJoin.Media)
    }

    private fun KomgaSearchCondition.MediaProfile.toMediaProfileCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toConditionNullable(OfflineMediaTable.mediaProfile) { it.name } to setOf(RequiredJoin.Media)
    }

    private fun KomgaSearchCondition.Tag.toTagCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val bookId = OfflineBookTable.id
        val tagsTable = OfflineBookMetadataTagTable
        val innerEquals = { tag: String ->
            tagsTable.select(tagsTable.bookId).where { tagsTable.tag.equalsIgnoreCase(tag) }
        }

        val innerAny = {
            tagsTable.select(tagsTable.bookId).where { tagsTable.tag.isNotNull() }
        }

        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> bookId.inSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNot -> bookId.notInSubQuery(innerEquals(operator.value))
            is KomgaSearchOperator.IsNullT -> bookId.notInSubQuery(innerAny())
            is KomgaSearchOperator.IsNotNullT -> bookId.inSubQuery(innerAny())
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.Author.toAuthorCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val bookId = OfflineBookTable.id
        val authorsTable = OfflineBookMetadataAuthorTable
        val inner = { name: String?, role: String? ->
            authorsTable.select(authorsTable.bookId)
                .where { Op.TRUE }
                .apply { if (name != null) andWhere { authorsTable.name.equalsIgnoreCase(name) } }
                .apply { if (role != null) andWhere { authorsTable.role.equalsIgnoreCase(role) } }
        }


        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> {
                if (operator.value.name == null && operator.value.role == null) Op.TRUE
                else bookId.inSubQuery(inner(operator.value.name, operator.value.role))
            }

            is KomgaSearchOperator.IsNot -> {
                if (operator.value.name == null && operator.value.role == null) Op.TRUE
                else bookId.notInSubQuery(inner(operator.value.name, operator.value.role))
            }
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.Poster.toPosterCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        val bookId = OfflineBookTable.id
        val thumbTable = OfflineThumbnailBookTable
        val inner = { type: PosterMatch.Type?, selected: Boolean? ->
            thumbTable.select(thumbTable.bookId)
                .where { Op.TRUE }
                .apply { if (type != null) andWhere { thumbTable.type.equalsIgnoreCase(type.name) } }
                .apply { if (selected != null) andWhere { thumbTable.selected.eq(selected) } }
        }


        val op = when (val operator = this.operator) {
            is KomgaSearchOperator.Is -> {
                if (operator.value.type == null && operator.value.selected == null) Op.TRUE
                else bookId.inSubQuery(inner(operator.value.type, operator.value.selected))
            }

            is KomgaSearchOperator.IsNot -> {
                if (operator.value.type == null && operator.value.selected == null) Op.TRUE
                else bookId.notInSubQuery(inner(operator.value.type, operator.value.selected))
            }
        }

        return op to emptySet()
    }

    private fun KomgaSearchCondition.OneShot.toOneshotCondition(): Pair<Op<Boolean>, Set<RequiredJoin>> {
        return this.operator.toCondition(OfflineBookTable.oneshot) to emptySet()
    }
}
