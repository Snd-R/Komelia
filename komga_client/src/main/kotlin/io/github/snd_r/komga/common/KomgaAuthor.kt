package io.github.snd_r.komga.common

import kotlinx.serialization.Serializable

@Serializable
data class KomgaAuthor(
    val name: String,
    val role: String,
)
