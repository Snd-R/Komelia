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
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.platform.formatDecimal
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.reader.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.PageDisplayLayout
import io.github.snd_r.komelia.ui.reader.PagedReaderPageState
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ReadingDirection
import io.github.snd_r.komga.book.KomgaBook

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    book: KomgaBook?,
    settingsState: ReaderState,
    pageState: PagedReaderPageState,

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
    book: KomgaBook?,
    settingsState: ReaderState,
    pageState: PagedReaderPageState,

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
    if (book != null) {
        ReturnLink(Icons.AutoMirrored.Default.MenuBook, book.seriesTitle, onSeriesPress)
        ReturnLink(Icons.Default.Book, book.metadata.title, onBookClick)
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

    val strings = LocalStrings.current.readerSettings
    val scaleType = pageState.scaleType.collectAsState().value
    Column {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(scaleType, strings.forScaleType(scaleType)),
            options = LayoutScaleType.entries.map { LabeledEntry(it, strings.forScaleType(it)) },
            onOptionChange = { pageState.onScaleTypeChange(it.value) },
            textFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.scaleType) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        if (scaleType != LayoutScaleType.ORIGINAL) {
            CheckboxWithLabel(
                settingsState.allowUpsample.collectAsState().value,
                settingsState::onAllowUpsampleChange,
                label = { Text(strings.upsample) },
                modifier = Modifier.widthIn(min = 100.dp)
            )
        }
    }

    val readingDirection = pageState.readingDirection.collectAsState().value
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(
            readingDirection,
            strings.forReadingDirection(readingDirection)
        ),
        options = ReadingDirection.entries.map { LabeledEntry(it, strings.forReadingDirection(it)) },
        onOptionChange = { pageState.onReadingDirectionChange(it.value) },
        textFieldModifier = Modifier.fillMaxWidth(),
        label = { Text(strings.readingDirection) },
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
    )

    val layout = pageState.layout.collectAsState().value
    Column {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(layout, strings.forLayout(layout)),
            options = PageDisplayLayout.entries.map { LabeledEntry(it, strings.forLayout(it)) },
            onOptionChange = { pageState.onLayoutChange(it.value) },
            textFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.layout) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val layoutOffset = pageState.layoutOffset.collectAsState().value
        if (layout == PageDisplayLayout.DOUBLE_PAGES) {
            CheckboxWithLabel(
                layoutOffset,
                pageState::onLayoutOffsetChange,
                label = { Text(strings.offsetPages) },
                modifier = Modifier.widthIn(min = 100.dp)
            )
        }
    }
    val decoder = settingsState.decoder.collectAsState().value
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
