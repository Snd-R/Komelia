package io.github.snd_r.komga.filesystem

import kotlinx.serialization.Serializable

@Serializable
data class DirectoryRequest(val path: String)