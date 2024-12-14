package io.github.snd_r.komelia.ui.dialogs.oneshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.stringEntry
import io.github.snd_r.komelia.ui.common.LockableChipTextFieldWithSuggestions
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
            allTags = seriesMetadata.allTags.collectAsState().value,
            allGenres = seriesMetadata.allGenres.collectAsState().value,
        )
    }

    @Composable
    private fun TagsContent(
        tags: StateHolder<List<String>>,
        tagsLock: StateHolder<Boolean>,
        genres: StateHolder<List<String>>,
        genresLock: StateHolder<Boolean>,
        allTags: List<String>,
        allGenres: List<String>,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            LockableChipTextFieldWithSuggestions(
                values = tags.value,
                onValuesChange = { tags.setValue(it) },
                label = "Tags",
                suggestions = remember(allTags) { allTags.map { stringEntry(it) } },
                locked = tagsLock.value,
                onLockChange = { tagsLock.setValue(it) }
            )
            LockableChipTextFieldWithSuggestions(
                values = genres.value,
                onValuesChange = { genres.setValue(it) },
                label = "Genres",
                suggestions = remember(allGenres) { allGenres.map { stringEntry(it) } },
                locked = genresLock.value,
                onLockChange = { genresLock.setValue(it) }
            )
        }
    }
}