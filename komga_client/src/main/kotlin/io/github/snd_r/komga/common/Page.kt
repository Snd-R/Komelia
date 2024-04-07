package io.github.snd_r.komga.common

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val content: List<T>,
    val pageable: Pageable,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean,
    val number: Int,
    val sort: Sort,
    val first: Boolean,
    val numberOfElements: Int,
    val size: Int,
    val empty: Boolean
)

@Serializable
data class Pageable(
    val sort: Sort,
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean,
)

@Serializable
data class Sort(
    val sorted: Boolean,
    val unsorted: Boolean,
    val empty: Boolean
)

data class KomgaPageRequest(
    val page: Int? = null,
    val size: Int? = null,
    val sort: KomgaSort = KomgaSort.UNSORTED,
    val unpaged: Boolean? = false,
)

fun KomgaPageRequest.toParams(): Parameters {
    val builder = ParametersBuilder()
    size?.let { builder.append("size", it.toString()) }
    page?.let { builder.append("page", it.toString()) }
    unpaged?.let { builder.append("unpaged", it.toString()) }

    val sort = buildString {
        for (order in sort) {
            append(order.property)
            append(",")
            append(order.direction.name.lowercase())
        }
    }
    if (sort.isNotBlank()) {
        builder.append("sort", sort)
    }

    return builder.build()
}

fun KomgaPageRequest.toMap(): Map<String, String> {
    val map = HashMap<String, String>()

    size?.let { map["size"] = it.toString() }
    page?.let { map["page"] = it.toString() }
    unpaged?.let { map["unpaged"] = it.toString() }
    val sort = buildString {
        for (order in sort) {
            append(order.property)
            append(",")
            append(order.direction.name.lowercase())
        }
    }
    if (sort.isNotBlank()) {
        map["sort"] = sort
    }

    return map
}
