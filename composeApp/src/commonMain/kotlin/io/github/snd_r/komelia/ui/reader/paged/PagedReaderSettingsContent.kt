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
import coil3.annotation.ExperimentalCoilApi
import io.github.snd_r.komelia.platform.formatDecimal
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ColumnScope.PagedReaderSettingsContent(
    pageState: PagedReaderState,
) {

    val strings = LocalStrings.current.readerSettings
    val scaleType = pageState.scaleType.collectAsState().value
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(scaleType, strings.forScaleType(scaleType)),
                options = LayoutScaleType.entries.map { LabeledEntry(it, strings.forScaleType(it)) },
                onOptionChange = { pageState.onScaleTypeChange(it.value) },
                modifier = Modifier.weight(1f),
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.scaleType) },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
            AnimatedVisibility(scaleType != LayoutScaleType.ORIGINAL) {
                CheckboxWithLabel(
                    pageState.imageStretchToFit.collectAsState().value,
                    pageState::onStretchToFitChange,
                    label = { Text("Stretch small images") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val layout = pageState.layout.collectAsState().value
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(layout, strings.forLayout(layout)),
                options = PageDisplayLayout.entries.map { LabeledEntry(it, strings.forLayout(it)) },
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
            options = PagedReaderState.ReadingDirection.entries.map {
                LabeledEntry(
                    it,
                    strings.forReadingDirection(it)
                )
            },
            onOptionChange = { pageState.onReadingDirectionChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.readingDirection) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        HorizontalDivider(Modifier.padding(vertical = 5.dp))

        val currentSpread = pageState.currentSpread.collectAsState()
        currentSpread.value.pages.forEach { page ->
            val sizeInMb = remember(currentSpread.value) {
                page.imageResult?.image?.size?.let { (it.toFloat() / 1024 / 1024).formatDecimal(2) }
            }
            val pageText = buildString {
                append("${strings.pageNumber} ${page.metadata.pageNumber}.")
                if (sizeInMb != null) append(" ${strings.memoryUsage}: ${sizeInMb}Mb")
            }

            Text(pageText, style = MaterialTheme.typography.bodyMedium)

            Text("${strings.pageScaledSize} ${page.imageResult?.image?.width} x ${page.imageResult?.image?.height}")

            if (page.metadata.size == null) {
                Text(
                    strings.noPageDimensionsWarning,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            } else {
                Text("${strings.pageOriginalSize}: ${page.metadata.size.width} x ${page.metadata.size.height}")
            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
        }
    }
}
