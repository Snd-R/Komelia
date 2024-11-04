package io.github.snd_r.komelia.image.coil

import io.ktor.http.*

fun removeEmptyPathSegments(url: String): String {
    val builder = URLBuilder(url)
    builder.pathSegments = builder.pathSegments.filter { it.isNotBlank() }
    return builder.buildString()
}