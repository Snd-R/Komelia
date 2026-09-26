package snd.komelia.ui.series.mangabaka.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komf.api.mangabaka.KomfMangaBakaSeries
import snd.komf.api.mangabaka.KomfMangaBakaTag

class MangaBakaMetadataState(
    val metadata: KomfMangaBakaSeries,
    val allTags: StateFlow<List<KomfMangaBakaTag>?>,
    val coroutineScope: CoroutineScope,
) {

    val compactContentTab = MutableStateFlow(MangaBakaContentTab.META)
    val titleState = MangaBakaTitlesState(metadata)
    val linksState = MangaBakaLinksState(metadata)
    val tagsState = MangaBakaTagsState(
        allTags = allTags,
        rawTags = metadata.tags ?: emptyList(),
        coroutineScope = coroutineScope
    )

    fun onCompactTabSelect(tab: MangaBakaContentTab){
        this.compactContentTab.value = tab
    }

    enum class MangaBakaContentTab {
        META,
        DESCRIPTION,
        LINKS,
        TAGS
    }
}