package snd.komelia.ui.dialogs.series.editbulk

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.LabeledEntry.Companion.stringEntry
import snd.komelia.ui.common.components.LockableChipTextFieldWithSuggestions
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem

internal class TagsTab(
    private val vm: SeriesBulkEditDialogViewModel,
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
            allTags = vm.allTags,
            allGenres = vm.allGenres,
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
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val warningColor = MaterialTheme.colorScheme.tertiary
        Row(Modifier.border(Dp.Hairline, warningColor).padding(20.dp)) {
            Icon(Icons.Default.PriorityHigh, null, tint = warningColor)
            Text(
                text = "You are editing tags for multiple series. This will override existing tags of each series.",
                color = warningColor
            )
        }

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