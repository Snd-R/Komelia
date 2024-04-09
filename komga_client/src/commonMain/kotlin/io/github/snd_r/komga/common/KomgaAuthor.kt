package io.github.snd_r.komga.common

import kotlinx.serialization.Serializable

const val writerRole = "writer"
const val pencillerRole = "penciller"
const val inkerRole = "inker"
const val coloristRole = "colorist"
const val lettererRole = "letterer"
const val coverRole = "cover"
const val editorRole = "editor"
const val translatorRole = "translator"

@Serializable
data class KomgaAuthor(
    val name: String,
    val role: String,
)
