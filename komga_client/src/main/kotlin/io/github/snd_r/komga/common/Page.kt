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

@Serializable
data class KomgaPageRequest(
    val page: Int? = null,
    val size: Int? = null,
    val sort: List<String> = emptyList(),
    val unpaged: Boolean? = false,
)

fun KomgaPageRequest.toParams(): Parameters {
    val builder = ParametersBuilder()
    size?.let { builder.append("size", it.toString()) }
    page?.let { builder.append("page", it.toString()) }
    unpaged?.let { builder.append("unpaged", it.toString()) }
    if (sort.isNotEmpty()) builder.append("sort", sort.joinToString(","))

    return builder.build()
}

fun KomgaPageRequest.toMap(): Map<String, String> {
    val map = HashMap<String, String>()

    size?.let { map["size"] = it.toString() }
    page?.let { map["page"] = it.toString() }
    unpaged?.let { map["unpaged"] = it.toString() }
    if (sort.isNotEmpty()) map["sort"] = sort.joinToString(",")

    return map
}
