package io.github.snd_r.komelia.ui.dialogs.series.edit

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
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

internal class TagsTab(
    private val vm: SeriesEditMetadataState,
) : DialogTab {

    override fun options() = TabItem(
        title = "TAGS",
        icon = Icons.Default.LocalOffer
    )

    @Composable
    override fun Content() {
        TagsContent(
            tags = StateHolder(vm.tags, vm::tags::set),
            tagsLock = StateHolder(vm.tagsLock, vm::tagsLock::set),
            genres = StateHolder(vm.genres, vm::genres::set),
            genresLock = StateHolder(vm.genresLock, vm::genresLock::set),
            allTags = vm.allTags.collectAsState().value,
            allGenres = vm.allGenres.collectAsState().value
        )
    }
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