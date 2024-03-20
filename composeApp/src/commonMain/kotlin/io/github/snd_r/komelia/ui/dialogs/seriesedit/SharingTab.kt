package io.github.snd_r.komelia.ui.dialogs.seriesedit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.common.LockableChipTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

internal class SharingTab(
    private val vm: SeriesEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "SHARING",
        icon = Icons.Default.People
    )

    @Composable
    override fun Content() {
        SharingContent(
            labels = StateHolder(vm.sharingLabels, vm::sharingLabels::set),
            labelsLock = StateHolder(vm.sharingLabelsLock, vm::sharingLabelsLock::set)
        )
    }
}

@Composable
private fun SharingContent(
    labels: StateHolder<List<String>>,
    labelsLock: StateHolder<Boolean>,
) {
    LockableChipTextField(
        values = labels,
        label = "Labels",
        lock = labelsLock
    )
}