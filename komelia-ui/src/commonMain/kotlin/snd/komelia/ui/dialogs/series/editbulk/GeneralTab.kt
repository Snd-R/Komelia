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
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LockableDropDown
import snd.komelia.ui.common.components.LockableTextField
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.series.KomgaSeriesStatus

internal class GeneralTab(
    private val vm: SeriesBulkEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "GENERAL",
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
    val strings = LocalStrings.current.seriesEdit

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        LockableDropDown(
            selectedOption = LabeledEntry(status.value, status.value?.let { strings.forSeriesStatus(it) } ?: ""),
            options = KomgaSeriesStatus.entries.map { LabeledEntry(it, strings.forSeriesStatus(it)) },
            onOptionChange = { status.onValueChange(it.value) },
            label = { Text(strings.status) },
            lock = statusLock,
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        LockableDropDown(
            selectedOption = readingDirection.value?.let { LabeledEntry(it, strings.forReadingDirection(it)) }
                ?: LabeledEntry(null, ""),
            options = KomgaReadingDirection.entries.map { LabeledEntry(it, strings.forReadingDirection(it)) },
            onOptionChange = { readingDirection.onValueChange(it.value) },
            label = { Text(strings.readingDirection) },
            lock = readingDirectionLock,
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        LockableTextField(
            text = language.value ?: "",
            onTextChange = language.setValue,
            errorMessage = language.errorMessage,
            label = strings.language,
            lock = languageLock,
            maxLines = 1,
        )

        LockableTextField(
            text = publisher.value ?: "",
            onTextChange = publisher.setValue,
            errorMessage = publisher.errorMessage,
            label = strings.publisher,
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
            label = strings.ageRating,
            lock = ageRatingLock,
            maxLines = 1,
        )
    }
}
