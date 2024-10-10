package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.AppSliderDefaults
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.reader.PageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.roundToInt

@Composable
fun ColumnScope.ContinuousReaderSettingsContent(state: ContinuousReaderState) {
    val strings = LocalStrings.current.continuousReader
    val padding = state.sidePaddingFraction.collectAsState().value
    Column {
        Text("${strings.sidePadding} ${(padding * 200).roundToInt()}%")
        Slider(
            value = padding,
            onValueChange = state::onSidePaddingChange,
            steps = 7,
            valueRange = 0f..0.4f,
            modifier = Modifier.cursorForHand(),
            colors = AppSliderDefaults.colors()
        )
        val spacing = state.pageSpacing.collectAsState(Dispatchers.Main.immediate).value
        TextField(
            value = if (spacing == 0) "" else spacing.toString(),
            onValueChange = { newValue ->
                if (newValue.isBlank()) state.onPageSpacingChange(0)
                else newValue.toIntOrNull()?.let { state.onPageSpacingChange(it) }
            },
            label = { Text(strings.pageSpacing) },
            modifier = Modifier.fillMaxWidth(),
        )
    }


    val readingDirection = state.readingDirection.collectAsState()
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(readingDirection.value, strings.forReadingDirection(readingDirection.value)),
        options = remember {
            ContinuousReaderState.ReadingDirection.entries.map { LabeledEntry(it, strings.forReadingDirection(it)) }
        },
        onOptionChange = { state.onReadingDirectionChange(it.value) },
        inputFieldModifier = Modifier.fillMaxWidth(),
        label = { Text(strings.readingDirection) },
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
    )


    var visiblePages by remember { mutableStateOf<List<PageMetadata>>(emptyList()) }
    LaunchedEffect(state.lazyListState) {
        snapshotFlow { state.lazyListState.layoutInfo }.collect { layout ->
            visiblePages = layout.visibleItemsInfo
                .mapNotNull { lazyItem ->
                    if (lazyItem.key is PageMetadata) lazyItem.key as PageMetadata
                    else null
                }

        }
    }

    var showPagesInfo by remember { mutableStateOf(false) }
    val readerStrings = LocalStrings.current.reader
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { showPagesInfo = !showPagesInfo }
            .cursorForHand()
            .padding(10.dp)
    ) {
        Text(readerStrings.pagesInfo)
        if (showPagesInfo) Icon(Icons.Default.ExpandLess, null)
        else Icon(Icons.Default.ExpandMore, null)
    }

    AnimatedVisibility(showPagesInfo) {
        Column {
            HorizontalDivider(Modifier.padding(vertical = 5.dp))
            visiblePages.forEach { page ->
                Text("${readerStrings.pageNumber} ${page.pageNumber}.", style = MaterialTheme.typography.bodyMedium)

                var displaySize by remember { mutableStateOf<IntSize?>(null) }
                LaunchedEffect(Unit) {
                    state.getPageDisplaySize(page).collect { displaySize = it }
                }
                displaySize?.let {
                    Text("${readerStrings.pageDisplaySize} ${it.width} x ${it.height}")
                }

                if (page.size != null) {
                    Text("${readerStrings.pageOriginalSize}: ${page.size.width} x ${page.size.height}")
                }

                HorizontalDivider(Modifier.padding(vertical = 5.dp))
            }
        }
    }
}
