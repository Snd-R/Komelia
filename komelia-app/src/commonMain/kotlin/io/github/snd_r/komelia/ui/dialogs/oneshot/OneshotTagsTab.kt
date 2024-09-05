package io.github.snd_r.komelia.ui.dialogs.oneshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockableChipTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditMetadataState
import io.github.snd_r.komelia.ui.dialogs.series.edit.SeriesEditMetadataState
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

internal class OneshotTagsTab(
    private val seriesMetadata: SeriesEditMetadataState,
    private val bookMetadata: BookEditMetadataState
) : DialogTab {

    override fun options() = TabItem(
        title = "TAGS",
        icon = Icons.Default.LocalOffer
    )

    @Composable
    override fun Content() {
        TagsContent(
            tags = StateHolder(bookMetadata.tags, bookMetadata::tags::set),
            tagsLock = StateHolder(bookMetadata.tagsLock, bookMetadata::tagsLock::set),
            genres = StateHolder(seriesMetadata.genres, seriesMetadata::genres::set),
            genresLock = StateHolder(seriesMetadata.genresLock, seriesMetadata::genresLock::set),
        )
    }

    @Composable
    private fun TagsContent(
        tags: StateHolder<List<String>>,
        tagsLock: StateHolder<Boolean>,
        genres: StateHolder<List<String>>,
        genresLock: StateHolder<Boolean>,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            LockableChipTextField(tags, "Tags", tagsLock)
            LockableChipTextField(genres, "Genres", genresLock)
        }
    }
}