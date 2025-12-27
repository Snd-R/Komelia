package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.SortOrder
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.common.KomgaSort.Direction.ASC
import snd.komga.client.common.KomgaSort.Direction.DESC
import snd.komga.client.common.Page
import snd.komga.client.common.Page.Companion.page
import snd.komga.client.common.Pageable
import snd.komga.client.common.Sort


fun KomgaSort.Order.toSortField(sorts: Map<String, ExpressionWithColumnType<out Any?>>): Pair<Expression<*>, SortOrder>? {
    val column = sorts[property] ?: return null

    val order = when (direction) {
        ASC -> SortOrder.ASC
        DESC -> SortOrder.DESC
    }
    return column to order
}

fun KomgaPageRequest.offset(): Long = ((this.pageIndex ?: 0) * (size ?: 20)).toLong()


fun <T> page(
    result: List<T>,
    pageRequest: KomgaPageRequest,
    count: Long,
    sorted: Boolean,
): Page<T> {
    val pageSort =
        if (sorted) Sort(sorted = true, unsorted = false, empty = count == 0L)
        else Sort(sorted = false, unsorted = true, empty = count == 0L)

    val pageable =
        if (pageRequest.unpaged == true) Pageable(
            pageSort,
            pageNumber = 0,
            pageSize = maxOf(count, 20L).toInt(),
            offset = pageRequest.offset().toInt(),
            paged = false,
            unpaged = true
        ) else Pageable(
            pageSort,
            pageNumber = pageRequest.pageIndex ?: 0,
            pageSize = pageRequest.size ?: 20,
            offset = pageRequest.offset().toInt(),
            paged = true,
            unpaged = false
        )

    return page(result, pageable, count.toInt())
}

fun <T> page(
    result: List<T>,
    count: Long,
    limit: Int,
    offset: Long,
    sorted: Boolean,
): Page<T> {
    val pageSort =
        if (sorted) Sort(sorted = true, unsorted = false, empty = count == 0L)
        else Sort(sorted = false, unsorted = true, empty = count == 0L)

    val pageable = Pageable(
        pageSort,
        pageNumber = (offset / limit).toInt(),
        pageSize = limit,
        offset = offset.toInt(),
        paged = true,
        unpaged = false
    )

    return page(result, pageable, count.toInt())
}
