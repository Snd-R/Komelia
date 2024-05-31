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
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.reader.PageMetadata
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt

@Composable
fun ColumnScope.ContinuousReaderSettingsContent(state: ContinuousReaderState) {
    val strings = LocalStrings.current.readerSettings
    val padding = state.sidePaddingFraction.collectAsState().value
    Column {
        Text("Side padding ${(padding * 200).roundToInt()}%")
        Slider(
            value = padding,
            onValueChange = state::onSidePaddingChange,
            steps = 7,
            valueRange = 0f..0.4f,
            modifier = Modifier.cursorForHand(),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.tertiary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = MaterialTheme.colorScheme.surface,
            )
        )
        val spacing = state.pageSpacing.collectAsState(Dispatchers.Main.immediate).value
        TextField(
            value = if (spacing == 0) "" else spacing.toString(),
            onValueChange = { newValue ->
                if (newValue.isBlank()) state.onPageSpacingChange(0)
                else newValue.toIntOrNull()?.let { state.onPageSpacingChange(it) }
            },
            label = { Text("Page spacing") },
            modifier = Modifier.fillMaxWidth(),
        )
    }


    val readingDirection = state.readingDirection.collectAsState()
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(readingDirection.value, readingDirection.value.name),
        options = ContinuousReaderState.ReadingDirection.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { state.onReadingDirectionChange(it.value) },
        inputFieldModifier = Modifier.fillMaxWidth(),
        label = { Text("Reading Direction") },
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
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { showPagesInfo = !showPagesInfo }
            .cursorForHand()
            .padding(10.dp)
    ) {
        Text("Pages info")
        if (showPagesInfo) Icon(Icons.Default.ExpandLess, null)
        else Icon(Icons.Default.ExpandMore, null)
    }

    AnimatedVisibility(showPagesInfo) {
        Column {
            HorizontalDivider(Modifier.padding(vertical = 5.dp))
            val scaleFactor = state.screenScaleState.transformation.collectAsState().value.scale
            visiblePages.forEach { page ->
                Text("${strings.pageNumber} ${page.pageNumber}.", style = MaterialTheme.typography.bodyMedium)

                if (page.size != null) {
                    val scaledSize = state.getContentSizePx(page)
                    val width = (scaledSize.width * scaleFactor).roundToInt()
                    val height = (scaledSize.height * scaleFactor).roundToInt()
                    Text("${strings.pageScaledSize} $width x $height")
                    Text("${strings.pageOriginalSize}: ${page.size.width} x ${page.size.height}")
                }

                HorizontalDivider(Modifier.padding(vertical = 5.dp))
            }
        }
    }
}
