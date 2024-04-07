package io.github.snd_r.komelia.ui.reader.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import io.github.snd_r.komelia.image.SamplerType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.ReaderPageState
import io.github.snd_r.komelia.ui.reader.ReaderSettingsState
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komga.book.KomgaBook

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    book: KomgaBook,
    settingsState: ReaderSettingsState,
    pageState: ReaderPageState,

    onMenuDismiss: () -> Unit,
    onShowHelpMenu: () -> Unit,
    onSeriesPress: () -> Unit,
    onBookClick: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {}
                .width(350.dp)
                .padding(20.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsContent(
                book = book,
                settingsState = settingsState,
                pageState = pageState,
                onMenuDismiss = onMenuDismiss,
                onShowHelpMenu = onShowHelpMenu,
                onSeriesPress = onSeriesPress,
                onBookClick = onBookClick
            )

            Spacer(Modifier.height(30.dp))

        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ColumnScope.SettingsContent(
    book: KomgaBook,
    settingsState: ReaderSettingsState,
    pageState: ReaderPageState,

    onMenuDismiss: () -> Unit,
    onShowHelpMenu: () -> Unit,
    onSeriesPress: () -> Unit,
    onBookClick: () -> Unit,
) {
    Row {
        IconButton(onClick = { onMenuDismiss() }) { Icon(Icons.Default.Close, null) }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onShowHelpMenu() }) { Icon(Icons.AutoMirrored.Default.Help, null) }
    }
    ReturnLink(Icons.AutoMirrored.Default.MenuBook, book.seriesTitle, onSeriesPress)
    ReturnLink(Icons.Default.Book, book.metadata.title, onBookClick)

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

    val strings = LocalStrings.current.readerSettings
    Column {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(settingsState.scaleType, strings.forScaleType(settingsState.scaleType)),
            options = LayoutScaleType.entries.map { LabeledEntry(it, strings.forScaleType(it)) },
            onOptionChange = { settingsState.onScaleTypeChange(it.value) },
            textFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.scaleType) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (settingsState.scaleType != LayoutScaleType.ORIGINAL) {
            CheckboxWithLabel(
                settingsState.allowUpsample,
                settingsState::onAllowUpsampleChange,
                label = { Text(strings.upsample) },
                modifier = Modifier.widthIn(min = 100.dp)
            )
        }
    }

    DropdownChoiceMenu(
        selectedOption = LabeledEntry(
            settingsState.readingDirection,
            strings.forReadingDirection(settingsState.readingDirection)
        ),
        options = ReadingDirection.entries.map { LabeledEntry(it, strings.forReadingDirection(it)) },
        onOptionChange = { settingsState.onReadingDirectionChange(it.value) },
        textFieldModifier = Modifier.fillMaxWidth(),
        label = { Text(strings.readingDirection) },
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Column {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(settingsState.layout, strings.forLayout(settingsState.layout)),
            options = PageDisplayLayout.entries.map { LabeledEntry(it, strings.forLayout(it)) },
            onOptionChange = { settingsState.onLayoutChange(it.value) },
            textFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.layout) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (settingsState.layout == PageDisplayLayout.DOUBLE_PAGES) {
            CheckboxWithLabel(
                settingsState.layoutOffset,
                settingsState::onLayoutOffsetChange,
                label = { Text(strings.offsetPages) },
                modifier = Modifier.widthIn(min = 100.dp)
            )
        }
    }
    val decoder = settingsState.decoder
    if (decoder != null)
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(decoder, decoder.name),
            options = SamplerType.entries.map { LabeledEntry(it, it.name) },
            onOptionChange = { settingsState.onDecoderChange(it.value) },
            textFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.decoder) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

    HorizontalDivider(Modifier.padding(top = 60.dp))

    val currentSpread = pageState.currentSpread.collectAsState()
    Column {
        currentSpread.value.pages.forEach { page ->
            val sizeInMb = remember(currentSpread.value) {
                page.imageResult?.image?.size?.let { "%.2f".format(it.toFloat() / 1024 / 1024) }
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

@Composable
private fun ReturnLink(icon: ImageVector, text: String, onClick: () -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .cursorForHand(),
    ) {

        Icon(
            icon, null,
            modifier = Modifier.size(35.dp).padding(end = 10.dp)
        )

        Text(
            text = text,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
