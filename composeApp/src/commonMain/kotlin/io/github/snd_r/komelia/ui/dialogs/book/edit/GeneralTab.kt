package io.github.snd_r.komelia.ui.dialogs.book.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.formatDecimal
import io.github.snd_r.komelia.ui.common.LockableTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

class GeneralTab(
    private val vm: BookEditDialogViewModel
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
            number = StateHolder(vm.number, vm::number::set),
            numberLock = StateHolder(vm.numberLock, vm::numberLock::set),
            sortNumber = StateHolder(vm.numberSort, vm::numberSort::set),
            sortNumberLock = StateHolder(vm.numberSortLock, vm::numberSortLock::set),
            summary = StateHolder(vm.summary, vm::summary::set),
            summaryLock = StateHolder(vm.summaryLock, vm::summaryLock::set),
            releaseDate = StateHolder(vm.releaseDate, vm::releaseDate::set),
            releaseDateLock = StateHolder(vm.releaseDateLock, vm::releaseDateLock::set),
            isbn = StateHolder(vm.isbn, vm::isbn::set),
            isbnLock = StateHolder(vm.isbnLock, vm::isbnLock::set),
        )
    }
}

@Composable
private fun GeneralTabContent(
    title: StateHolder<String>,
    titleLock: StateHolder<Boolean>,
    number: StateHolder<String>,
    numberLock: StateHolder<Boolean>,
    sortNumber: StateHolder<Float>,
    sortNumberLock: StateHolder<Boolean>,
    summary: StateHolder<String>,
    summaryLock: StateHolder<Boolean>,
    releaseDate: StateHolder<String>,
    releaseDateLock: StateHolder<Boolean>,
    isbn: StateHolder<String>,
    isbnLock: StateHolder<Boolean>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LockableTextField(
            text = title.value,
            onTextChange = title.setValue,
            errorMessage = title.errorMessage,
            label = "Title",
            lock = titleLock,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {

            LockableTextField(
                text = number.value,
                onTextChange = number.setValue,
                errorMessage = number.errorMessage,
                label = "Number",
                lock = numberLock,
                modifier = Modifier.weight(.5f)
            )

            var text by remember { mutableStateOf(sortNumber.value.formatDecimal(0)) }

            LockableTextField(
                text = text,
                onTextChange = {
                    if (it.isBlank()) {
                        text = it
                    } else {
                        val newNumber = it.toFloatOrNull()
                        if (newNumber != null) {
                            text = it
                            sortNumber.setValue(newNumber)
                        }
                    }
                },
                errorMessage = sortNumber.errorMessage,
                label = "Sort Number",
                lock = sortNumberLock,
                modifier = Modifier.weight(.5f)
            )

        }

        LockableTextField(
            text = summary.value,
            onTextChange = summary.setValue,
            errorMessage = summary.errorMessage,
            label = "Summary",
            lock = summaryLock,
            minLines = 6,
            maxLines = 12,
            modifier = Modifier.fillMaxWidth(),
            textFieldModifier = Modifier
        )

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            LockableTextField(
                text = releaseDate.value,
                onTextChange = { releaseDate.setValue(it) },
                errorMessage = releaseDate.errorMessage,
                label = "Release Date",
                lock = releaseDateLock,
                maxLines = 1,
                modifier = Modifier.weight(.5f)
            )

            LockableTextField(
                text = isbn.value,
                onTextChange = isbn.setValue,
                errorMessage = isbn.errorMessage,
                label = "ISBN",
                lock = isbnLock,
                maxLines = 1,
                modifier = Modifier.weight(.5f)
            )
        }
    }
}