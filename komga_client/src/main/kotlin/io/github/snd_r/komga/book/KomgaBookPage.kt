package io.github.snd_r.komga.book

import kotlinx.serialization.Serializable

@Serializable
data class KomgaBookPage(
    val number: Int,
    val fileName: String,
    val mediaType: String,
    val width: Int?,
    val height: Int?,
    val sizeBytes: Long?,
    val size: String
)