package snd.komelia.offline.media.model

import kotlinx.serialization.Serializable

@Serializable
data class EpubTocEntry(
    val title: String,
    val href: String?,
    val children: List<EpubTocEntry> = emptyList(),
)