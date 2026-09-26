package snd.komelia.ui.series.mangabaka.state

import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komf.api.mangabaka.MangaBakaTitle

class MangaBakaTitlesState(metadata: KomfMangaBakaSeries) {
    private val mainTitleLanguage = "en"
    val mainTitle: MangaBakaDisplayTitle
    val secondaryTitles: List<MangaBakaDisplayTitle>


    init {
        val originalLanguage = metadata.originalLanguage
        val secondaryTitleLanguages = listOf("$originalLanguage-Latn", "$originalLanguage")

        val allTitles = metadata.titles ?: emptyList()

        val mainTitle = allTitles
            .firstOrNull { it.isPrimary == true && it.language == mainTitleLanguage }
            ?: allTitles.firstOrNull { it.language == mainTitleLanguage }

        val secondaryTitles = mutableListOf<MangaBakaTitle>()
        for (language in secondaryTitleLanguages) {
            (allTitles.firstOrNull { it.isPrimary == true && it.language == language }
                ?: allTitles.firstOrNull { it.language == language }
                    )?.let { secondaryTitles.add(it) }
        }
        val fallbackMainTitle = (secondaryTitles.firstOrNull() ?: allTitles.first { it.isPrimary == true })
        val grouped = secondaryTitles.groupBy { it.title }.toMutableMap()
        this.mainTitle = (mainTitle ?: fallbackMainTitle).let { mainTitle ->
            val titles = grouped.remove(mainTitle.title)?.map { it.language } ?: emptyList()
            MangaBakaDisplayTitle(mainTitle.title, titles + mainTitle.language)
        }
        this.secondaryTitles = grouped.map { (title, group) -> MangaBakaDisplayTitle(title, group.map { it.language }) }
    }
}

data class MangaBakaDisplayTitle(
    val title: String,
    val languages: List<String>
)