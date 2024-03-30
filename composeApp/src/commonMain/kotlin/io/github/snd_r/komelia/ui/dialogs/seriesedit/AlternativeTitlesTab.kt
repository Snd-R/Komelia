package io.github.snd_r.komelia.ui.dialogs.seriesedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockIcon
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.common.withTextFieldKeyMapping
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.series.KomgaAlternativeTitle

internal class AlternativeTitlesTab(
    private val vm: SeriesEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "ALTERNATE TITLES",
        icon = Icons.Default.Title
    )

    @Composable
    override fun Content() {
        AlternativeTitlesTabContent(
            alternativeTitles = vm.alternateTitles,
            onTitleAdd = { vm.alternateTitles.add(KomgaAlternativeTitle("", "")) },
            onTitleChange = { index, title -> vm.alternateTitles[index] = title },
            onTitleRemove = { index -> vm.alternateTitles.removeAt(index) },
            alternativeTitlesLock = StateHolder(vm.alternateTitlesLock, vm::alternateTitlesLock::set)
        )
    }
}

@Composable
private fun AlternativeTitlesTabContent(
    alternativeTitles: List<KomgaAlternativeTitle>,
    onTitleAdd: () -> Unit,
    onTitleChange: (index: Int, title: KomgaAlternativeTitle) -> Unit,
    onTitleRemove: (index: Int) -> Unit,
    alternativeTitlesLock: StateHolder<Boolean>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .heightIn(min = 100.dp, max = 600.dp)
            .fillMaxWidth()
    ) {
        alternativeTitles.forEachIndexed { index, altTitle ->
            Row {
                LockIcon(alternativeTitlesLock)

                TextField(
                    value = altTitle.label,
                    onValueChange = { onTitleChange(index, altTitle.copy(label = it)) },
                    label = { Text("Label") },
                    maxLines = 1,
                    modifier = Modifier.weight(.3f).withTextFieldKeyMapping(onTitleAdd)
                )

                Spacer(Modifier.size(20.dp))

                TextField(
                    value = altTitle.title,
                    onValueChange = { onTitleChange(index, altTitle.copy(title = it)) },
                    label = { Text("Alternate title") },
                    maxLines = 1,
                    modifier = Modifier.weight(.7f).withTextFieldKeyMapping()
                )

                IconButton(onClick = { onTitleRemove(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }

        FilledTonalIconButton(onClick = onTitleAdd) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

}

