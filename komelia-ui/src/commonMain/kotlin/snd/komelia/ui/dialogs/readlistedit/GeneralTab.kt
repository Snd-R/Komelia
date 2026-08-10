package snd.komelia.ui.dialogs.readlistedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.edit_tab_general
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.readlist_edit_name
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.readlist_edit_order
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.readlist_edit_order_manual
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.readlist_edit_summary
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem

internal class GeneralTab(
    private val vm: ReadListEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.edit_tab_general,
        icon = Icons.Default.FormatAlignCenter
    )

    @Composable
    override fun Content() {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextField(
                value = vm.name,
                onValueChange = vm::name::set,
                label = { Text(stringResource(Res.string.readlist_edit_name)) },
                supportingText = {
                    vm.nameValidationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                isError = vm.nameValidationError != null,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = vm.summary,
                onValueChange = vm::summary::set,
                label = { Text(stringResource(Res.string.readlist_edit_summary)) },
                minLines = 6,
                maxLines = 12,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Column {
                Text(
                    stringResource(Res.string.readlist_edit_order),
                    style = MaterialTheme.typography.bodyMedium
                )
                CheckboxWithLabel(
                    checked = vm.manualOrdering,
                    onCheckedChange = vm::manualOrdering::set,
                    label = { Text(stringResource(Res.string.readlist_edit_order_manual)) }
                )

            }
        }
    }

}