package io.github.snd_r.komelia.ui.reader.paged

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout

@Composable
fun ColumnScope.PagedReaderSettingsContent(
    pageState: PagedReaderState,
) {

    val strings = LocalStrings.current.pagedReader
    val scaleType = pageState.scaleType.collectAsState().value
    Column {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(scaleType, strings.forScaleType(scaleType)),
            options = remember { LayoutScaleType.entries.map { LabeledEntry(it, strings.forScaleType(it)) } },
            onOptionChange = { pageState.onScaleTypeChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.scaleType) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val layout = pageState.layout.collectAsState().value
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(layout, strings.forLayout(layout)),
                options = remember { PageDisplayLayout.entries.map { LabeledEntry(it, strings.forLayout(it)) } },
                onOptionChange = { pageState.onLayoutChange(it.value) },
                modifier = Modifier.weight(1f),
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.layout) },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )

            val layoutOffset = pageState.layoutOffset.collectAsState().value
            AnimatedVisibility(layout == PageDisplayLayout.DOUBLE_PAGES) {
                CheckboxWithLabel(
                    layoutOffset,
                    pageState::onLayoutOffsetChange,
                    label = { Text(strings.offsetPages) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val readingDirection = pageState.readingDirection.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                readingDirection,
                strings.forReadingDirection(readingDirection)
            ),
            options = remember {
                PagedReaderState.ReadingDirection.entries.map { LabeledEntry(it, strings.forReadingDirection(it)) }
            },
            onOptionChange = { pageState.onReadingDirectionChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.readingDirection) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        HorizontalDivider(Modifier.padding(vertical = 5.dp))

        val readerStrings = LocalStrings.current.reader
        val currentSpread = pageState.currentSpread.collectAsState()
        currentSpread.value.pages.forEach { page ->
            val pageImage = page.imageResult?.image
            val pageSize = pageImage?.originalSize?.collectAsState()?.value
            if (pageImage != null) {
                val currentSize = pageImage.currentSize.collectAsState().value
                Text("${readerStrings.pageNumber} ${page.metadata.pageNumber}")

                if (currentSize != null) {
                    Text("${readerStrings.pageDisplaySize} ${currentSize.width} x ${currentSize.height}")
                }

                if (pageSize != null) {
                    Text("${readerStrings.pageOriginalSize}: ${pageSize.width} x ${pageSize.height}")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
        }
    }
}
