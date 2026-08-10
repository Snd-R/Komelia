package snd.komelia.ui.dialogs.series.editbulk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_age_rating
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_language
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_publisher
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_reading_direction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_status
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_tab_general
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LockableDropDown
import snd.komelia.ui.common.components.LockableTextField
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.strings.AppStrings
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.series.KomgaSeriesStatus

internal class GeneralTab(
    private val vm: SeriesBulkEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.series_edit_tab_general,
        icon = Icons.Default.FormatAlignCenter
    )

    @Composable
    override fun Content() {
        GeneralTabContent(
            status = OptionsStateHolder(vm.status, KomgaSeriesStatus.entries, vm::status::set),
            statusLock = StateHolder(vm.statusLock, vm::statusLock::set),
            language = StateHolder(vm.language, vm::language::set),
            languageLock = StateHolder(vm.languageLock, vm::languageLock::set),
            readingDirection = OptionsStateHolder(
                vm.readingDirection,
                KomgaReadingDirection.entries,
                vm::readingDirection::set
            ),
            readingDirectionLock = StateHolder(vm.readingDirectionLock, vm::readingDirectionLock::set),
            publisher = StateHolder(vm.publisher, vm::publisher::set),
            publisherLock = StateHolder(vm.publisherLock, vm::publisherLock::set),
            ageRating = StateHolder(vm.ageRating, vm::ageRating::set),
            ageRatingLock = StateHolder(vm.ageRatingLock, vm::ageRatingLock::set),
        )
    }
}

@Composable
private fun GeneralTabContent(
    status: OptionsStateHolder<KomgaSeriesStatus?>,
    statusLock: StateHolder<Boolean>,
    language: StateHolder<String?>,
    languageLock: StateHolder<Boolean>,
    readingDirection: OptionsStateHolder<KomgaReadingDirection?>,
    readingDirectionLock: StateHolder<Boolean>,
    publisher: StateHolder<String?>,
    publisherLock: StateHolder<Boolean>,
    ageRating: StateHolder<Int?>,
    ageRatingLock: StateHolder<Boolean>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        LockableDropDown(
            selectedOption = LabeledEntry(
                status.value,
                status.value?.let { stringResource(AppStrings.forSeriesStatus(it)) } ?: ""),
            options = KomgaSeriesStatus.entries.map {
                LabeledEntry(
                    it,
                    stringResource(AppStrings.forSeriesStatus(it))
                )
            },
            onOptionChange = { status.onValueChange(it.value) },
            label = { Text(stringResource(Res.string.series_edit_status)) },
            lock = statusLock,
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        LockableDropDown(
            selectedOption = readingDirection.value?.let {
                LabeledEntry(
                    it,
                    stringResource(AppStrings.forReadingDirection(it))
                )
            }
                ?: LabeledEntry(null, ""),
            options = KomgaReadingDirection.entries.map {
                LabeledEntry(
                    it,
                    stringResource(AppStrings.forReadingDirection(it))
                )
            },
            onOptionChange = { readingDirection.onValueChange(it.value) },
            label = { Text(stringResource(Res.string.series_edit_reading_direction)) },
            lock = readingDirectionLock,
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        LockableTextField(
            text = language.value ?: "",
            onTextChange = language.setValue,
            errorMessage = language.errorMessage,
            label = stringResource(Res.string.series_edit_language),
            lock = languageLock,
            maxLines = 1,
        )

        LockableTextField(
            text = publisher.value ?: "",
            onTextChange = publisher.setValue,
            errorMessage = publisher.errorMessage,
            label = stringResource(Res.string.series_edit_publisher),
            lock = publisherLock,
            maxLines = 1,
        )

        LockableTextField(
            text = ageRating.value?.toString() ?: "",
            onTextChange = {
                try {
                    if (it.isBlank()) ageRating.setValue(null)
                    else ageRating.setValue(it.toInt())
                } catch (e: NumberFormatException) {
                    // ignore
                }
            },
            errorMessage = ageRating.errorMessage,
            label = stringResource(Res.string.series_edit_age_rating),
            lock = ageRatingLock,
            maxLines = 1,
        )
    }
}
