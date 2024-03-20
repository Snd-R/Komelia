package io.github.snd_r.komga.common

import kotlinx.serialization.Serializable

@Serializable
data class KomgaWebLink(
    val label: String,
    val url: String,
)
