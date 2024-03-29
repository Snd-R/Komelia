package io.github.snd_r.komelia.ui.dialogs.seriesedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockableDropDown
import io.github.snd_r.komelia.ui.common.LockableTextField
import io.github.snd_r.komelia.ui.common.OptionsStateHolder
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komga.common.KomgaReadingDirection
import io.github.snd_r.komga.series.KomgaSeriesStatus

internal class GeneralTab(
    private val vm: SeriesEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "GENERAL",
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
            readingDirectionLock = StateHolder(vm.readingDirectionLock, vm::readingDirectionLock::set),
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
//        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        LockableTextField(
            text = title.value,
            onTextChange = title.setValue,
            errorMessage = title.errorMessage,
            label = "title",
            lock = titleLock,
        )
        LockableTextField(
            text = sortTitle.value,
            onTextChange = sortTitle.setValue,
            errorMessage = sortTitle.errorMessage,
            label = "Sort title",
            lock = sortTitleLock,
        )
        LockableTextField(
            text = summary.value,
            onTextChange = summary.setValue,
            errorMessage = summary.errorMessage,
            label = "Summary",
            lock = summaryLock,
            textFieldModifier = Modifier
        )

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {

            val statusOptions = remember { KomgaSeriesStatus.entries.map { it.name } }
            LockableDropDown(
                selectedOption = status.value.name,
                options = statusOptions,
                onOptionChange = { status.onValueChange(KomgaSeriesStatus.valueOf(it)) },
                label = { Text("Status") },
                lock = statusLock,
                textFieldModifier = Modifier.weight(.5f)
            )
            LockableTextField(
                text = language.value,
                onTextChange = language.setValue,
                errorMessage = language.errorMessage,
                label = "Language",
                lock = languageLock,
                modifier = Modifier.weight(.5f),
            )
        }

        val readingDirectionOptions = remember { KomgaReadingDirection.entries.map { it.name } }
        LockableDropDown(
            selectedOption = readingDirection.value?.name ?: "",
            options = readingDirectionOptions,
            onOptionChange = { readingDirection.onValueChange(KomgaReadingDirection.valueOf(it)) },
            label = { Text("Reading Direction") },
            lock = readingDirectionLock,
            textFieldModifier = Modifier.fillMaxWidth()
        )


        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            LockableTextField(
                text = publisher.value,
                onTextChange = publisher.setValue,
                errorMessage = publisher.errorMessage,
                label = "Publisher",
                lock = publisherLock,
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
                label = "Age Rating",
                lock = ageRatingLock,
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
                label = "Total Book Count",
                lock = totalBookCountLock,
                modifier = Modifier.fillMaxWidth().weight(.5f)
            )

            Spacer(Modifier.weight(.5f))
        }

    }
}

@Composable
private fun TabContainer(content: @Composable () -> Unit) {
    Box(
        Modifier
    ) {
        content()
    }
}

