package snd.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_analyze_dimensions
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_convert_to_cbz
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_hash_files
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_hash_pages
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_repair_extensions
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_series_cover
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_tab_options
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.strings.AppStrings
import snd.komga.client.library.SeriesCover

internal class OptionsTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.library_edit_tab_options,
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
    Column {
        CheckboxWithLabel(
            checked = hashFiles.value,
            onCheckedChange = hashFiles.setValue,
            label = {
                Text(stringResource(Res.string.library_edit_hash_files))
            }
        )

        CheckboxWithLabel(
            checked = hashPages.value,
            onCheckedChange = hashPages.setValue,
            label = { Text(stringResource(Res.string.library_edit_hash_pages)) }
        )

        CheckboxWithLabel(
            checked = analyzeDimensions.value,
            onCheckedChange = analyzeDimensions.setValue,
            label = { Text(stringResource(Res.string.library_edit_analyze_dimensions)) }
        )

        CheckboxWithLabel(
            checked = repairExtensions.value,
            onCheckedChange = repairExtensions.setValue,
            label = { Text(stringResource(Res.string.library_edit_repair_extensions)) }
        )

        CheckboxWithLabel(
            checked = convertToCbz.value,
            onCheckedChange = convertToCbz.setValue,
            label = { Text(stringResource(Res.string.library_edit_convert_to_cbz)) }
        )

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                seriesCover.value,
                stringResource(AppStrings.forSeriesCover(seriesCover.value))

            ),
            options = SeriesCover.entries.map {
                LabeledEntry(
                    it,
                    stringResource(AppStrings.forSeriesCover(it))
                )
            },
            onOptionChange = { seriesCover.onValueChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.library_edit_series_cover)) }
        )

    }
}