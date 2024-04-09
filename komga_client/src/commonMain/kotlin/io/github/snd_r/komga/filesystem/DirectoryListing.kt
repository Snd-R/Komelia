package io.github.snd_r.komga.filesystem

import kotlinx.serialization.Serializable

@Serializable
data class DirectoryListing(
    val parent: String? = null,
    val directories: List<Path>,
)

@Serializable
data class Path(
    val type: String,
    val name: String,
    val path: String
)