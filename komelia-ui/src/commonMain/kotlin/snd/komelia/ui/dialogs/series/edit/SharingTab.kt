package snd.komelia.ui.dialogs.series.edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_sharing_labels
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_tab_sharing
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.LockableChipTextField
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem

internal class SharingTab(
    private val vm: SeriesEditMetadataState,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.series_edit_tab_sharing,
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
        label = stringResource(Res.string.series_edit_sharing_labels),
        lock = labelsLock
    )
}