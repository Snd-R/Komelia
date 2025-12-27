package snd.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komga.client.library.SeriesCover

internal class OptionsTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "OPTIONS",
        icon = Icons.Default.Tune
    )

    @Composable
    override fun Content() {
        OptionsTabContent(
            hashFiles = StateHolder(vm.hashFiles, vm::hashFiles::set),
            hashPages = StateHolder(vm.hashPages, vm::hashPages::set),
            analyzeDimensions = StateHolder(vm.analyzeDimensions, vm::analyzeDimensions::set),
            repairExtensions = StateHolder(vm.repairExtensions, vm::repairExtensions::set),
            convertToCbz = StateHolder(vm.convertToCbz, vm::convertToCbz::set),
            seriesCover = OptionsStateHolder(vm.seriesCover, SeriesCover.entries, vm::seriesCover::set),
        )
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
    val strings = LocalStrings.current.libraryEdit
    Column {
        CheckboxWithLabel(
            checked = hashFiles.value,
            onCheckedChange = hashFiles.setValue,
            label = { Text(strings.hashFiles) }
        )

        CheckboxWithLabel(
            checked = hashPages.value,
            onCheckedChange = hashPages.setValue,
            label = { Text(strings.hashPages) }
        )

        CheckboxWithLabel(
            checked = analyzeDimensions.value,
            onCheckedChange = analyzeDimensions.setValue,
            label = { Text(strings.analyzeDimensions) }
        )

        CheckboxWithLabel(
            checked = repairExtensions.value,
            onCheckedChange = repairExtensions.setValue,
            label = { Text(strings.repairExtensions) }
        )

        CheckboxWithLabel(
            checked = convertToCbz.value,
            onCheckedChange = convertToCbz.setValue,
            label = { Text(strings.convertToCbz) }
        )

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                seriesCover.value,
                strings.forSeriesCover(seriesCover.value)
            ),
            options = SeriesCover.entries.map { LabeledEntry(it, strings.forSeriesCover(it)) },
            onOptionChange = { seriesCover.onValueChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.seriesCover) }
        )

    }
}