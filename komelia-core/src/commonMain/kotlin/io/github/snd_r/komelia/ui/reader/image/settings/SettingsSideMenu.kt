package io.github.snd_r.komelia.ui.reader.image.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.NumberFieldWithIncrements
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.settings.imagereader.DeviceSelector
import io.github.snd_r.komelia.ui.settings.imagereader.OnnxRuntimeModeSelector
import io.github.snd_r.komelia.ui.settings.imagereader.OnnxRuntimeSettingsState
import io.github.snd_r.komelia.ui.settings.imagereader.TileSizeSelector
import io.github.snd_r.komelia.ui.settings.imagereader.isOnnxRuntimeInstalled
import kotlinx.coroutines.Dispatchers
import snd.komelia.image.ReduceKernel
import snd.komga.client.book.KomgaBook
import kotlin.math.roundToInt

@Composable
fun SettingsSideMenuOverlay(
    book: KomgaBook?,
    readerType: ReaderType,
    onReaderTypeChange: (ReaderType) -> Unit,
    isColorCorrectionsActive: Boolean,
    onColorCorrectionClick: () -> Unit,

    availableUpsamplingModes: List<UpsamplingMode>,
    upsamplingMode: UpsamplingMode,
    onUpsamplingModeChange: (UpsamplingMode) -> Unit,
    availableDownsamplingKernels: List<ReduceKernel>,
    downsamplingKernel: ReduceKernel,
    onDownsamplingKernelChange: (ReduceKernel) -> Unit,
    linearLightDownsampling: Boolean,
    onLinearLightDownsamplingChange: (Boolean) -> Unit,
    stretchToFit: Boolean,
    onStretchToFitChange: (Boolean) -> Unit,
    cropBorders: Boolean,
    onCropBordersChange: (Boolean) -> Unit,
    zoom: Float,
    showImageSettings: Boolean,
    onShowImageSettingsChange: (Boolean) -> Unit,

    flashEnabled: Boolean,
    onFlashEnabledChange: (Boolean) -> Unit,
    flashEveryNPages: Int,
    onFlashEveryNPagesChange: (Int) -> Unit,
    flashWith: ReaderFlashColor,
    onFlashWithChange: (ReaderFlashColor) -> Unit,
    flashDuration: Long,
    onFlashDurationChange: (Long) -> Unit,

    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    onnxRuntimeSettingsState: OnnxRuntimeSettingsState?,

    onBackPress: () -> Unit,
    onShowHelpMenu: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .windowInsetsPadding(WindowInsets.statusBars)
        )
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {}
                .width(350.dp)
                .padding(horizontal = 10.dp)
                .imePadding()
                .fillMaxHeight()
                .align(Alignment.End)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row {
                TextButton(
                    onClick = { onBackPress() },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Spacer(Modifier.width(3.dp))
                    Text("Close Book")
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onShowHelpMenu() }) { Icon(Icons.AutoMirrored.Default.Help, null) }
            }
            if (book != null) {
                BookTitles(book)
            }

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
            val strings = LocalStrings.current
            val readerStrings = strings.reader
            val zoomPercentage = remember(zoom) { (zoom * 100).roundToInt() }
            Text("${readerStrings.zoom}: $zoomPercentage%")
            Column {
                DropdownChoiceMenu(
                    selectedOption = LabeledEntry(readerType, readerStrings.forReaderType(readerType)),
                    options = remember { ReaderType.entries.map { LabeledEntry(it, readerStrings.forReaderType(it)) } },
                    onOptionChange = { onReaderTypeChange(it.value) },
                    inputFieldModifier = Modifier.fillMaxWidth(),
                    label = { Text(readerStrings.readerType) },
                    inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
                )
                when (readerType) {
                    PAGED -> PagedReaderSettingsContent(pagedReaderState)
                    CONTINUOUS -> ContinuousReaderSettingsContent(continuousReaderState)
                }
            }

            HorizontalDivider()
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
                Column {
                    SamplingModeSettings(
                        availableUpsamplingModes = availableUpsamplingModes,
                        upsamplingMode = upsamplingMode,
                        onUpsamplingModeChange = onUpsamplingModeChange,
                        availableDownsamplingKernels = availableDownsamplingKernels,
                        downsamplingKernel = downsamplingKernel,
                        onDownsamplingKernelChange = onDownsamplingKernelChange,
                        linearLightDownsampling = linearLightDownsampling,
                        onLinearLightDownsamplingChange = onLinearLightDownsamplingChange,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    CommonImageSettings(
                        modifier = Modifier.padding(start = 5.dp),
                        stretchToFit = stretchToFit,
                        onStretchToFitChange = onStretchToFitChange,
                        cropBorders = cropBorders,
                        onCropBordersChange = onCropBordersChange,
                        isColorCorrectionsActive = isColorCorrectionsActive,
                        onColorCorrectionClick = onColorCorrectionClick,
                        flashEnabled = flashEnabled,
                        onFlashEnabledChange = onFlashEnabledChange,
                        flashEveryNPages = flashEveryNPages,
                        onFlashEveryNPagesChange = onFlashEveryNPagesChange,
                        flashWith = flashWith,
                        onFlashWithChange = onFlashWithChange,
                        flashDuration = flashDuration,
                        onFlashDurationChange = onFlashDurationChange,
                    )
                }
            }
            if (onnxRuntimeSettingsState != null && isOnnxRuntimeInstalled()) {
                HorizontalDivider()
                var showOnnxRuntimeSettings by remember { mutableStateOf(false) }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showOnnxRuntimeSettings = !showOnnxRuntimeSettings }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Text("OnnxRuntime")
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier.rotate(if (showOnnxRuntimeSettings) 180f else 0f)
                    )
                }
                AnimatedVisibility(showOnnxRuntimeSettings) {
                    Column(Modifier.padding(start = 10.dp)) {
                        OnnxRuntimeModeSelector(
                            currentMode = onnxRuntimeSettingsState.onnxRuntimeMode.collectAsState().value,
                            onModeChange = onnxRuntimeSettingsState::onOnnxRuntimeUpscaleModeChange,
                            currentModelPath = onnxRuntimeSettingsState.onnxModelPath.collectAsState().value,
                            onModelPathChange = onnxRuntimeSettingsState::onOnnxModelSelect
                        )
                        DeviceSelector(
                            availableDevices = onnxRuntimeSettingsState.availableDevices.collectAsState().value,
                            executionProvider = onnxRuntimeSettingsState.currentExecutionProvider,
                            currentDeviceId = onnxRuntimeSettingsState.deviceId.collectAsState().value,
                            onDeviceIdChange = onnxRuntimeSettingsState::onDeviceIdChange
                        )

                        TileSizeSelector(
                            tileSize = onnxRuntimeSettingsState.tileSize.collectAsState().value,
                            onTileSizeChange = onnxRuntimeSettingsState::onTileSizeChange
                        )
                    }
                }
            }
            HorizontalDivider()
            when (readerType) {
                PAGED -> PagedReaderPagesInfo(
                    pagedReaderState.currentSpread.collectAsState().value,
                    modifier = Modifier.padding(start = 10.dp)
                )

                CONTINUOUS -> {
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
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            null,
                            Modifier.rotate(if (showPagesInfo) 180f else 0f)
                        )
                    }

                    AnimatedVisibility(showPagesInfo) {
                        ContinuousReaderPagesInfo(
                            lazyListState = continuousReaderState.lazyListState,
                            waitForImage = continuousReaderState::waitForImage,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.padding(bottom = 60.dp).navigationBarsPadding())
        }
    }

}


@Composable
private fun ColumnScope.ContinuousReaderSettingsContent(state: ContinuousReaderState) {
    val strings = LocalStrings.current.continuousReader

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

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        val padding = state.sidePaddingFraction.collectAsState().value
        NumberFieldWithIncrements(
            value = padding * 200,
            label = { Text("side padding", style = MaterialTheme.typography.labelMedium) },
            onvValueChange = { state.onSidePaddingChange(it / 200) },
            stepSize = 5f,
            minValue = 0f,
            maxValue = 80f,
            digitsAfterDecimal = 1,
            modifier = Modifier.weight(1f)
        )
        val spacing = state.pageSpacing.collectAsState(Dispatchers.Main.immediate).value
        NumberFieldWithIncrements(
            value = spacing.toFloat(),
            label = { Text("page spacing", style = MaterialTheme.typography.labelMedium) },
            onvValueChange = { state.onPageSpacingChange(it.roundToInt()) },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 9999f,
            digitsAfterDecimal = 0,
            modifier = Modifier.weight(1f).padding(end = 10.dp)
        )
    }
    Spacer(Modifier.height(10.dp))
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
    }
}

@Composable
private fun BookTitles(book: KomgaBook) {
    Column {
        if (!book.oneshot) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(35.dp).padding(end = 10.dp)
                )

                Text(
                    text = book.seriesTitle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(35.dp).padding(end = 10.dp)
            )

            Text(
                text = book.metadata.title,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SamplingModeSettings(
    availableUpsamplingModes: List<UpsamplingMode>,
    upsamplingMode: UpsamplingMode,
    onUpsamplingModeChange: (UpsamplingMode) -> Unit,
    availableDownsamplingKernels: List<ReduceKernel>,
    downsamplingKernel: ReduceKernel,
    onDownsamplingKernelChange: (ReduceKernel) -> Unit,
    linearLightDownsampling: Boolean,
    onLinearLightDownsamplingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current.imageSettings

    Column(modifier) {
        if (availableUpsamplingModes.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(
                    upsamplingMode,
                    strings.forUpsamplingMode(upsamplingMode)
                ),
                options = remember(availableUpsamplingModes) {
                    availableUpsamplingModes.map {
                        LabeledEntry(
                            it,
                            strings.forUpsamplingMode(it)
                        )
                    }
                },
                onOptionChange = { onUpsamplingModeChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.upsamplingMode) },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        if (availableDownsamplingKernels.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(
                    downsamplingKernel,
                    strings.forDownsamplingKernel(downsamplingKernel)
                ),
                options = remember(availableDownsamplingKernels) {
                    availableDownsamplingKernels.map {
                        LabeledEntry(
                            it,
                            strings.forDownsamplingKernel(it)
                        )
                    }
                },
                onOptionChange = { onDownsamplingKernelChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.downsamplingKernel) },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }


        val platform = LocalPlatform.current
        if (platform != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = linearLightDownsampling,
                onCheckedChange = onLinearLightDownsamplingChange,
                label = { Text("Linear light downsampling") },
                supportingText = {
                    Text("slower but potentially more accurate", style = MaterialTheme.typography.labelMedium)
                },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }
    }
}
