package io.github.snd_r.komga.announcements

import io.github.snd_r.komga.serializers.InstantIsoStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class KomgaJsonFeed(
    val version: String,
    val title: String,
    @SerialName("home_page_url")
    val homePageUrl: String?,
    val description: String?,
    val items: List<KomgaAnnouncement> = emptyList(),
) {
    @JvmInline
    @Serializable
    value class KomgaAnnouncementId(val value: String) {
        override fun toString() = value
    }

    @Serializable
    data class KomgaAnnouncement(
        val id: KomgaAnnouncementId,
        val url: String?,
        val title: String?,
        val summary: String?,

        @SerialName("content_html")
        val contentHtml: String?,

        @SerialName("date_modified")
        @Serializable(InstantIsoStringSerializer::class)
        val dateModified: Instant?,
        val author: Author?,
        val tags: Set<String> = emptySet(),
        @SerialName("_komga")
        val komgaExtension: KomgaExtension?,
    )

    @Serializable
    data class Author(
        val name: String?,
        val url: String?,
    )

    @Serializable
    data class KomgaExtension(
        val read: Boolean,
    )
}
