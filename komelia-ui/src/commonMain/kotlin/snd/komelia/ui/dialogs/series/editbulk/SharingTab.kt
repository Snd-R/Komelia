package snd.komelia.ui.dialogs.series.editbulk

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_bulk_warning
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_sharing_labels
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_tab_sharing
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.LockableChipTextField
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem

internal class SharingTab(
    private val vm: SeriesBulkEditDialogViewModel,
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
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val warningColor = MaterialTheme.colorScheme.tertiary
        Row(Modifier.border(Dp.Hairline, warningColor).padding(20.dp)) {
            Icon(Icons.Default.PriorityHigh, null, tint = warningColor)
            Text(
                text = stringResource(Res.string.series_edit_bulk_warning),
                color = warningColor
            )
        }

        LockableChipTextField(
            values = labels,
            label = stringResource(Res.string.series_edit_sharing_labels),
            lock = labelsLock
        )
    }
}