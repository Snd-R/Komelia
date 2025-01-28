package io.github.snd_r.komelia.ui.reader.image.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.AppSliderDefaults
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout
import kotlinx.coroutines.Dispatchers
import snd.komga.client.book.KomgaBook
import kotlin.math.roundToInt

@Composable
fun SettingsSideMenuOverlay(
    book: KomgaBook?,
    readerType: ReaderType,
    onReaderTypeChange: (ReaderType) -> Unit,
    isColorCorrectionsActive: Boolean,
    onColorCorrectionClick: () -> Unit,
    onSeriesPress: () -> Unit,
    onBookPress: () -> Unit,
    decoder: PlatformDecoderSettings?,
    decoderDescriptor: PlatformDecoderDescriptor?,
    onUpscaleMethodChange: (UpscaleOption) -> Unit,
    stretchToFit: Boolean,
    onStretchToFitChange: (Boolean) -> Unit,
    cropBorders: Boolean,
    onCropBordersChange: (Boolean) -> Unit,
    zoom: Float,
    showImageSettings: Boolean,
    onShowImageSettingsChange: (Boolean) -> Unit,

    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,

    onShowHelpMenu: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {}
                .width(350.dp)
                .padding(10.dp)
                .imePadding()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))

            Row {
                IconButton(onClick = { onDismiss() }) { Icon(Icons.Default.Close, null) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onShowHelpMenu() }) { Icon(Icons.AutoMirrored.Default.Help, null) }
            }
            if (book != null) {
                if (book.oneshot) {
                    ReturnLink(Icons.AutoMirrored.Default.MenuBook, book.seriesTitle, onSeriesPress)
                } else {
                    ReturnLink(Icons.AutoMirrored.Default.MenuBook, book.seriesTitle, onSeriesPress)
                    ReturnLink(Icons.Default.Book, book.metadata.title, onBookPress)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            val strings = LocalStrings.current.reader
            val zoomPercentage = remember(zoom) { (zoom * 100).roundToInt() }
            Text("${strings.zoom}: $zoomPercentage%")
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onShowImageSettingsChange(!showImageSettings) }
                    .cursorForHand()
                    .padding(10.dp)
            ) {
                Text("Image Settings")
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.ArrowDropDown,
                    null,
                    Modifier.rotate(if (showImageSettings) 180f else 0f)
                )
            }
            AnimatedVisibility(showImageSettings) {
                CommonImageSettings(
                    modifier = Modifier.padding(start = 10.dp),
                    decoder = decoder,
                    decoderDescriptor = decoderDescriptor,
                    onUpscaleMethodChange = onUpscaleMethodChange,
                    stretchToFit = stretchToFit,
                    onStretchToFitChange = onStretchToFitChange,
                    cropBorders = cropBorders,
                    onCropBordersChange = onCropBordersChange,
                    isColorCorrectionsActive = isColorCorrectionsActive,
                    onColorCorrectionClick = onColorCorrectionClick
                )

            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
            Column {
                DropdownChoiceMenu(
                    selectedOption = LabeledEntry(readerType, strings.forReaderType(readerType)),
                    options = remember { ReaderType.entries.map { LabeledEntry(it, strings.forReaderType(it)) } },
                    onOptionChange = { onReaderTypeChange(it.value) },
                    inputFieldModifier = Modifier.fillMaxWidth(),
                    label = { Text(strings.readerType) },
                    inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
                )
                when (readerType) {
                    PAGED -> PagedReaderSettingsContent(pagedReaderState)
                    CONTINUOUS -> ContinuousReaderSettingsContent(continuousReaderState)
                }
            }

            Spacer(Modifier.height(60.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
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
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(35.dp).padding(end = 10.dp)
        )

        Text(
            text = text,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CommonImageSettings(
    decoder: PlatformDecoderSettings?,
    decoderDescriptor: PlatformDecoderDescriptor?,
    onUpscaleMethodChange: (UpscaleOption) -> Unit,

    stretchToFit: Boolean,
    onStretchToFitChange: (Boolean) -> Unit,
    cropBorders: Boolean,
    onCropBordersChange: (Boolean) -> Unit,

    isColorCorrectionsActive: Boolean,
    onColorCorrectionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current.reader
    Column(modifier = modifier) {
        if (decoder != null && decoderDescriptor != null && decoderDescriptor.upscaleOptions.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(decoder.upscaleOption, decoder.upscaleOption.value),
                options = remember { decoderDescriptor.upscaleOptions.map { LabeledEntry(it, it.value) } },
                onOptionChange = { onUpscaleMethodChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text("Upscale method") },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        SwitchWithLabel(
            checked = stretchToFit,
            onCheckedChange = onStretchToFitChange,
            label = { Text(strings.stretchToFit) },
            contentPadding = PaddingValues(horizontal = 10.dp)
        )

        if (LocalPlatform.current != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = cropBorders,
                onCheckedChange = onCropBordersChange,
                label = { Text("Crop borders") },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }
        Row(
            modifier = Modifier
                .clickable { onColorCorrectionClick() }
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(horizontal = 10.dp, vertical = 15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color Correction")
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = if (isColorCorrectionsActive) MaterialTheme.colorScheme.secondary
                else LocalContentColor.current
            )
            if (isColorCorrectionsActive) {
                Text(
                    "active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ContinuousReaderSettingsContent(state: ContinuousReaderState) {
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
                if (newValue.length > 5) return@TextField
                if (newValue.isBlank()) state.onPageSpacingChange(0)
                else newValue.toIntOrNull()?.let { state.onPageSpacingChange(it) }
            },
            label = { Text(strings.pageSpacing) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        ContinuousReaderPagesInfo(state.lazyListState, state::waitForImage)
    }
}

@Composable
private fun ColumnScope.PagedReaderSettingsContent(
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

        val layout = pageState.layout.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(layout, strings.forLayout(layout)),
            options = remember { PageDisplayLayout.entries.map { LabeledEntry(it, strings.forLayout(it)) } },
            onOptionChange = { pageState.onLayoutChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.layout) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val layoutOffset = pageState.layoutOffset.collectAsState().value
        AnimatedVisibility(
            visible = layout == PageDisplayLayout.DOUBLE_PAGES || layout == PageDisplayLayout.DOUBLE_PAGES_NO_COVER,
        ) {
            SwitchWithLabel(
                checked = layoutOffset,
                onCheckedChange = pageState::onLayoutOffsetChange,
                label = { Text(strings.offsetPages) },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }

        val currentSpread = pageState.currentSpread.collectAsState().value
        PagedReaderPagesInfo(currentSpread)
    }
}
