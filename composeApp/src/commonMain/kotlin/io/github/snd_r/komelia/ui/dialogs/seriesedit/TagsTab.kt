package io.github.snd_r.komelia.ui.dialogs.seriesedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockableChipTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

internal class TagsTab(
    private val vm: SeriesEditDialogViewModel,
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
        )
    }
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