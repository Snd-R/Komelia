package snd.komelia.ui.dialogs.series.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_sort_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_status
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_summary
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_tab_general
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.series_edit_total_book_count
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
    private val vm: SeriesEditMetadataState,
) : DialogTab {

    override fun options() = TabItem(
        title = Res.string.series_edit_tab_general,
        icon = Icons.Default.FormatAlignCenter
    )

    @Composable
    override fun Content() {
        GeneralTabContent(
            title = StateHolder(vm.title, vm::title::set),
            titleLock = StateHolder(vm.titleLock, vm::titleLock::set),
            sortTitle = StateHolder(vm.titleSort, vm::titleSort::set),
            sortTitleLock = StateHolder(vm.titleSortLock, vm::titleSortLock::set),
            summary = StateHolder(vm.summary, vm::summary::set),
            summaryLock = StateHolder(vm.summaryLock, vm::summaryLock::set),
            status = OptionsStateHolder(vm.status, KomgaSeriesStatus.entries, vm::status::set),
            statusLock = StateHolder(vm.statusLock, vm::statusLock::set),
            language = StateHolder(vm.language, vm::language::set),
            languageLock = StateHolder(vm.languageLock, vm::languageLock::set),
            readingDirection = OptionsStateHolder(
                vm.readingDirection,
                KomgaReadingDirection.entries,
                vm::readingDirection::set
            ),
            readingDirectionLock = StateHolder(
                vm.readingDirectionLock,
                vm::readingDirectionLock::set
            ),
            publisher = StateHolder(vm.publisher, vm::publisher::set),
            publisherLock = StateHolder(vm.publisherLock, vm::publisherLock::set),
            ageRating = StateHolder(vm.ageRating, vm::ageRating::set),
            ageRatingLock = StateHolder(vm.ageRatingLock, vm::ageRatingLock::set),
            totalBookCount = StateHolder(vm.totalBookCount, vm::totalBookCount::set),
            totalBookCountLock = StateHolder(vm.totalBookCountLock, vm::totalBookCountLock::set),
        )
    }
}

@Composable
private fun GeneralTabContent(
    title: StateHolder<String>,
    titleLock: StateHolder<Boolean>,
    sortTitle: StateHolder<String>,
    sortTitleLock: StateHolder<Boolean>,
    summary: StateHolder<String>,
    summaryLock: StateHolder<Boolean>,
    status: OptionsStateHolder<KomgaSeriesStatus>,
    statusLock: StateHolder<Boolean>,
    language: StateHolder<String>,
    languageLock: StateHolder<Boolean>,
    readingDirection: OptionsStateHolder<KomgaReadingDirection?>,
    readingDirectionLock: StateHolder<Boolean>,
    publisher: StateHolder<String>,
    publisherLock: StateHolder<Boolean>,
    ageRating: StateHolder<Int?>,
    ageRatingLock: StateHolder<Boolean>,
    totalBookCount: StateHolder<Int?>,
    totalBookCountLock: StateHolder<Boolean>,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LockableTextField(
            text = title.value,
            onTextChange = title.setValue,
            errorMessage = title.errorMessage,
            label = stringResource(Res.string.series_edit_title),
            lock = titleLock,
        )
        LockableTextField(
            text = sortTitle.value,
            onTextChange = sortTitle.setValue,
            errorMessage = sortTitle.errorMessage,
            label = stringResource(Res.string.series_edit_sort_title),
            lock = sortTitleLock,
        )
        LockableTextField(
            text = summary.value,
            onTextChange = summary.setValue,
            errorMessage = summary.errorMessage,
            label = stringResource(Res.string.series_edit_summary),
            lock = summaryLock,
            textFieldModifier = Modifier
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            LockableDropDown(
                selectedOption = LabeledEntry(
                    status.value,
                    stringResource(AppStrings.forSeriesStatus(status.value))
                ),
                options = KomgaSeriesStatus.entries.map {
                    LabeledEntry(
                        it,
                        stringResource(AppStrings.forSeriesStatus(status.value))
                    )
                },
                onOptionChange = { status.onValueChange(it.value) },
                label = { Text(stringResource(Res.string.series_edit_status)) },
                lock = statusLock,
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
                inputFieldModifier = Modifier.weight(.5f)
            )
            LockableTextField(
                text = language.value,
                onTextChange = language.setValue,
                errorMessage = language.errorMessage,
                label = stringResource(Res.string.series_edit_language),
                lock = languageLock,
                maxLines = 1,
                modifier = Modifier.weight(.5f),
            )
        }

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
            inputFieldModifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )


        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LockableTextField(
                text = publisher.value,
                onTextChange = publisher.setValue,
                errorMessage = publisher.errorMessage,
                label = stringResource(Res.string.series_edit_publisher),
                lock = publisherLock,
                maxLines = 1,
                modifier = Modifier.weight(.5f)
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
                modifier = Modifier.weight(.5f)
            )
        }

        Row {
            LockableTextField(
                text = totalBookCount.value?.toString() ?: "",
                onTextChange = {
                    try {
                        if (it.isBlank()) totalBookCount.setValue(null)
                        else totalBookCount.setValue(it.toInt())
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                },
                errorMessage = totalBookCount.errorMessage,
                label = stringResource(Res.string.series_edit_total_book_count),
                lock = totalBookCountLock,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().weight(.5f)
            )

            Spacer(Modifier.weight(.5f))
        }

    }
}
