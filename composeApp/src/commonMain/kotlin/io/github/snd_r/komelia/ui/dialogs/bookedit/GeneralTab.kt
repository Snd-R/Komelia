package io.github.snd_r.komelia.ui.dialogs.bookedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LockableTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komelia.platform.ScrollBarConfig
import io.github.snd_r.komelia.platform.verticalScrollWithScrollbar
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


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
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .heightIn(max = 600.dp)
            .padding(end = 20.dp)
            .fillMaxWidth()
//            .fillMaxSize()
            .verticalScrollWithScrollbar(
                state = scrollState,
                scrollbarConfig = ScrollBarConfig(indicatorColor = MaterialTheme.colorScheme.onSurface)
            )
    ) {
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

            var text by remember {
                val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                df.setMaximumFractionDigits(340);
                mutableStateOf(df.format(sortNumber.value))
            }

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
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            LockableTextField(
                text = releaseDate.value,
                onTextChange = { releaseDate.setValue(it) },
                errorMessage = releaseDate.errorMessage,
                label = "Release Date",
                lock = releaseDateLock,
                modifier = Modifier.weight(.5f)
            )

            LockableTextField(
                text = isbn.value,
                onTextChange = isbn.setValue,
                errorMessage = isbn.errorMessage,
                label = "ISBN",
                lock = isbnLock,
                modifier = Modifier.weight(.5f)
            )
        }
    }
}