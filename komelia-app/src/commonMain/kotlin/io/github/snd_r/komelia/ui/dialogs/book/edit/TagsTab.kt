package io.github.snd_r.komelia.ui.dialogs.book.edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.common.LockableChipTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

class TagsTab(
    private val vm: BookEditDialogViewModel
) : DialogTab {
    override fun options() = TabItem(
        title = "TAGS",
        icon = Icons.Default.LocalOffer
    )

    @Composable
    override fun Content() {
        TagsTabContent(
            tags = StateHolder(vm.tags, vm::tags::set),
            tagsLock = StateHolder(vm.tagsLock, vm::tagsLock::set)
        )
    }
}

@Composable
private fun TagsTabContent(
    tags: StateHolder<List<String>>,
    tagsLock: StateHolder<Boolean>,
) {
    LockableChipTextField(tags, "Tags", tagsLock)
}