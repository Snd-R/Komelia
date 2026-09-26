package snd.komelia.ui.series.mangabaka.state

import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komf.api.mangabaka.MangaBakaLink
import snd.komf.api.mangabaka.MangaBakaLinkType

class MangaBakaLinksState(metadata: KomfMangaBakaSeries) {
    val linkGroups: List<LinkGroup>

    init {
        val links = metadata.links ?: emptyList()

        val linkGroups = mutableListOf<LinkGroup>()
        val byType = links.groupBy { it.type }

        MangaBakaLinkType.entries.forEach { type ->
            val name = when (type) {
                MangaBakaLinkType.RETAILER -> "BUY"
                MangaBakaLinkType.PUBLISHER -> "PUBLISHER"
                MangaBakaLinkType.WEBPLATFORM -> "READ OFFICIALLY"
                MangaBakaLinkType.SOCIAL -> "SOCIAL"
                MangaBakaLinkType.NEWS,
                MangaBakaLinkType.INFO -> "INFO"

                MangaBakaLinkType.PIRACY,
                MangaBakaLinkType.OTHER -> "OTHER"
            }
            val links = byType[type] ?: return@forEach
            val groups = links.groupBy { it.nameDisplay }
            val subgroups = mutableListOf<LinkSubGroup>()
            val other = mutableListOf<MangaBakaLink>()
            for ((name, subgroup) in groups) {
                if (type != MangaBakaLinkType.SOCIAL &&
                    (subgroup.none { it.language == metadata.originalLanguage || it.language == "en" })
                ) {
                    other.addAll(subgroup)
                } else {
                    subgroups.add(LinkSubGroup(name, subgroup))
                }
            }
            linkGroups.add(LinkGroup(name, subgroups, other))
        }

        this.linkGroups = linkGroups
    }

    data class LinkGroup(
        val name: String,
        val subgroups: List<LinkSubGroup>,
        val hiddenLinks: List<MangaBakaLink>
    )

    data class LinkSubGroup(
        val name: String,
        val links: List<MangaBakaLink>
    )
}

