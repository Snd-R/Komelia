package snd.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_empty_trash_after_scan
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_exclude_directories
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_oneshots_directory
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_cbx
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_epub
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_file_types
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_force_modified_time
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_interval
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_on_startup
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_scan_pdf
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_tab_scanner
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.strings.AppStrings
import snd.komga.client.library.ScanInterval

internal class ScannerTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.library_edit_tab_scanner,
        icon = Icons.AutoMirrored.Filled.ManageSearch
    )

    @Composable
    override fun Content() {
        ScannerTabContent(
            emptyTrashAfterScan = StateHolder(vm.emptyTrashAfterScan, vm::emptyTrashAfterScan::set),
            scanForceModifiedTime = StateHolder(vm.scanForceModifiedTime, vm::scanForceModifiedTime::set),
            scanOnStartup = StateHolder(vm.scanOnStartup, vm::scanOnStartup::set),
            scanInterval = OptionsStateHolder(vm.scanInterval, ScanInterval.entries, vm::scanInterval::set),
            oneshotsDirectory = StateHolder(vm.oneshotsDirectory, vm::oneshotsDirectory::set),
            scanCbx = StateHolder(vm.scanCbx, vm::scanCbx::set),
            scanEpub = StateHolder(vm.scanEpub, vm::scanEpub::set),
            scanPdf = StateHolder(vm.scanPdf, vm::scanPdf::set),
            excludeDirectories = StateHolder(vm.scanDirectoryExclusions, vm::scanDirectoryExclusions::set),
        )
    }
}


@Composable
private fun ScannerTabContent(
    emptyTrashAfterScan: StateHolder<Boolean>,
    scanForceModifiedTime: StateHolder<Boolean>,
    scanOnStartup: StateHolder<Boolean>,
    scanInterval: OptionsStateHolder<ScanInterval>,
    oneshotsDirectory: StateHolder<String>,
    scanCbx: StateHolder<Boolean>,
    scanEpub: StateHolder<Boolean>,
    scanPdf: StateHolder<Boolean>,
    excludeDirectories: StateHolder<List<String>>,
) {

    Column {
        CheckboxWithLabel(
            checked = emptyTrashAfterScan.value,
            onCheckedChange = emptyTrashAfterScan.setValue,
            label = { Text(stringResource(Res.string.library_edit_empty_trash_after_scan)) }
        )
        CheckboxWithLabel(
            checked = scanForceModifiedTime.value,
            onCheckedChange = scanForceModifiedTime.setValue,
            label = { Text(stringResource(Res.string.library_edit_scan_force_modified_time)) }
        )

        CheckboxWithLabel(
            checked = scanOnStartup.value,
            onCheckedChange = scanOnStartup.setValue,
            label = { Text(stringResource(Res.string.library_edit_scan_on_startup)) }
        )

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                scanInterval.value,
                stringResource(AppStrings.forScanInterval(scanInterval.value))

            ),
            options = ScanInterval.entries.map {
                LabeledEntry(
                    it,
                    stringResource(AppStrings.forScanInterval(it))
                )
            },
            onOptionChange = { scanInterval.onValueChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.library_edit_scan_interval)) }
        )

        Spacer(Modifier.size(20.dp))
        TextField(
            value = oneshotsDirectory.value,
            onValueChange = oneshotsDirectory.setValue,
            label = { Text(stringResource(Res.string.library_edit_oneshots_directory)) },
            modifier = Modifier.fillMaxWidth()
        )
        ScanFileTypes(scanCbx, scanEpub, scanPdf)

        val state = rememberChipTextFieldState(excludeDirectories.value.map { Chip(it) })
        LaunchedEffect(state, excludeDirectories.value) {
            snapshotFlow { state.chips.map { it.text } }
                .collect { excludeDirectories.setValue(it) }
        }

        ChipTextField(
            state = state,
            label = { Text(stringResource(Res.string.library_edit_exclude_directories)) },
            onSubmit = { text -> Chip(text) },
            readOnlyChips = true
        )

    }
}

@Composable
private fun ScanFileTypes(
    scanCbx: StateHolder<Boolean>,
    scanEpub: StateHolder<Boolean>,
    scanPdf: StateHolder<Boolean>,
) {
    Column(Modifier.padding(vertical = 15.dp)) {
        Text(stringResource(Res.string.library_edit_scan_file_types))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScanFiletypeChip(
                label = { Text(stringResource(Res.string.library_edit_scan_cbx)) },
                selected = scanCbx.value,
                onValueChange = scanCbx.setValue
            )
            ScanFiletypeChip(
                label = { Text(stringResource(Res.string.library_edit_scan_pdf)) },
                selected = scanPdf.value,
                onValueChange = scanPdf.setValue
            )
            ScanFiletypeChip(
                label = { Text(stringResource(Res.string.library_edit_scan_epub)) },
                selected = scanEpub.value,
                onValueChange = scanEpub.setValue
            )
        }
    }

}

@Composable
private fun ScanFiletypeChip(
    label: @Composable () -> Unit,
    selected: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    FilterChip(
        label = label,
        onClick = { onValueChange(!selected) },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )
}
