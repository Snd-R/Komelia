package io.github.snd_r.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.OptionsStateHolder
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogControlButtons
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.library.SeriesCover

internal class OptionsTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "OPTIONS",
        icon = Icons.Default.Tune
    )

    @Composable
    override fun Content() {
        Column {
            OptionsTabContent(
                hashFiles = StateHolder(vm.hashFiles, vm::hashFiles::set),
                hashPages = StateHolder(vm.hashPages, vm::hashPages::set),
                analyzeDimensions = StateHolder(vm.analyzeDimensions, vm::analyzeDimensions::set),
                repairExtensions = StateHolder(vm.repairExtensions, vm::repairExtensions::set),
                convertToCbz = StateHolder(vm.convertToCbz, vm::convertToCbz::set),
                seriesCover = OptionsStateHolder(vm.seriesCover, SeriesCover.entries, vm::seriesCover::set),
            )
            DialogControlButtons(
                confirmationText = "Next",
                onConfirmClick = vm::toMetadataTab,
                onDismissRequest = vm.onDialogDismiss
            )
        }
    }
}

@Composable
private fun OptionsTabContent(
    hashFiles: StateHolder<Boolean>,
    hashPages: StateHolder<Boolean>,
    analyzeDimensions: StateHolder<Boolean>,
    repairExtensions: StateHolder<Boolean>,
    convertToCbz: StateHolder<Boolean>,
    seriesCover: OptionsStateHolder<SeriesCover>,
) {
    Column {
        CheckboxWithLabel(
            checked = hashFiles.value,
            onCheckedChange = hashFiles.setValue,
            label = { Text("Compute hash for files") }
        )

        CheckboxWithLabel(
            checked = hashPages.value,
            onCheckedChange = hashPages.setValue,
            label = { Text("Compute hash for pages") }
        )

        CheckboxWithLabel(
            checked = analyzeDimensions.value,
            onCheckedChange = analyzeDimensions.setValue,
            label = { Text("Analyze pages dimensions") }
        )

        CheckboxWithLabel(
            checked = repairExtensions.value,
            onCheckedChange = repairExtensions.setValue,
            label = { Text("Automatically repair incorrect file extensions") }
        )

        CheckboxWithLabel(
            checked = convertToCbz.value,
            onCheckedChange = convertToCbz.setValue,
            label = { Text("Automatically convert to CBZ") }
        )

        DropdownChoiceMenu(
            selectedOption = seriesCover.value.name,
            options = SeriesCover.entries.map { it.name },
            onOptionChange = { seriesCover.onValueChange(SeriesCover.valueOf(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Series cover") }
        )

    }
}