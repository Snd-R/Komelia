package snd.komelia.ui.reader.image.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_close_book
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_continuous_page_spacing
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_continuous_reading_direction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_continuous_side_padding
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_downsampling_kernel
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_linear_light_downsampling
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_linear_light_downsampling_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_settings
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_upsampling_mode
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_layout
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_offset_pages
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_reading_direction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_paged_scale_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_pages_info
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_zoom
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.model.ContinuousReadingDirection
import snd.komelia.settings.model.LayoutScaleType
import snd.komelia.settings.model.PageDisplayLayout
import snd.komelia.settings.model.PagedReadingDirection
import snd.komelia.settings.model.ReaderFlashColor
import snd.komelia.settings.model.ReaderType
import snd.komelia.settings.model.ReaderType.CONTINUOUS
import snd.komelia.settings.model.ReaderType.PAGED
import snd.komelia.settings.model.ReaderType.PANELS
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.NumberFieldWithIncrements
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.reader.image.continuous.ContinuousReaderState
import snd.komelia.ui.reader.image.paged.PagedReaderState
import snd.komelia.ui.reader.image.panels.PanelsReaderState
import snd.komelia.ui.settings.imagereader.onnxruntime.DeviceSelector
import snd.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsState
import snd.komelia.ui.settings.imagereader.onnxruntime.TileSizeSelector
import snd.komelia.ui.settings.imagereader.onnxruntime.UpscaleModeSelector
import snd.komelia.ui.settings.imagereader.onnxruntime.isOnnxRuntimeInstalled
import snd.komelia.ui.strings.AppStrings
import snd.komelia.ui.strings.stringLabels
import kotlin.math.roundToInt

@Composable
fun SettingsSideMenuOverlay(
    book: KomeliaBook?,
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
    panelsReaderState: PanelsReaderState?,
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
                    Text(stringResource(Res.string.reader_close_book))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onShowHelpMenu() }) { Icon(Icons.AutoMirrored.Default.Help, null) }
            }
            if (book != null) {
                BookTitles(book)
            }

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
            val zoomPercentage = remember(zoom) { (zoom * 100).roundToInt() }
            Text(stringResource(Res.string.reader_zoom, zoomPercentage))

            Column {
                val readerTypes = stringLabels(ReaderType.entries) { AppStrings.forReaderType(it) }
                DropdownChoiceMenu(
                    selectedOption = LabeledEntry(
                        readerType,
                        stringResource(AppStrings.forReaderType(readerType))
                    ),
                    options = derivedStateOf { if (panelsReaderState == null) readerTypes.filter { it.value != PANELS } else readerTypes }.value,
                    onOptionChange = { onReaderTypeChange(it.value) },
                    inputFieldModifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.reader_type)) },
                    inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
                )
                when (readerType) {
                    PAGED -> PagedReaderSettingsContent(pagedReaderState)
                    PANELS -> {
                        if (panelsReaderState != null) {
                            PanelsReaderSettingsContent(
                                readingDirection = panelsReaderState.readingDirection.collectAsState().value,
                                onReadingDirectionChange = panelsReaderState::onReadingDirectionChange
                            )
                        }
                    }

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
                Text(stringResource(Res.string.reader_image_settings))
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
                    Text(stringResource(Res.string.settings_image_onnxruntime))
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        null,
                        Modifier.rotate(if (showOnnxRuntimeSettings) 180f else 0f)
                    )
                }
                AnimatedVisibility(showOnnxRuntimeSettings) {
                    Column(Modifier.padding(start = 10.dp)) {
                        UpscaleModeSelector(
                            currentMode = onnxRuntimeSettingsState.upscaleMode.collectAsState().value,
                            onModeChange = onnxRuntimeSettingsState::onUpscaleModeChange,
                            currentModelPath = onnxRuntimeSettingsState.upscaleModelPath.collectAsState().value,
                            onModelPathChange = onnxRuntimeSettingsState::onUpscaleModelPathChange
                        )
                        DeviceSelector(
                            availableDevices = onnxRuntimeSettingsState.availableDevices,
                            executionProvider = onnxRuntimeSettingsState.currentExecutionProvider,
                            currentDeviceId = onnxRuntimeSettingsState.deviceId.collectAsState().value,
                            onDeviceIdChange = onnxRuntimeSettingsState::onDeviceIdChange
                        )

                        TileSizeSelector(
                            tileSize = onnxRuntimeSettingsState.upscalerTileSize.collectAsState().value,
                            onTileSizeChange = onnxRuntimeSettingsState::onTileSizeChange
                        )
                    }
                }
            }
            HorizontalDivider()
            when (readerType) {
                PAGED -> {
                    PagedReaderPagesInfo(
                        pages = pagedReaderState.currentSpread.collectAsState().value.pages,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                PANELS -> {
                    if (panelsReaderState != null) {
                        val panelsPage = panelsReaderState.currentPage.collectAsState().value
                        val pages = remember(panelsPage) {
                            panelsPage?.let { listOf(PagedReaderState.Page(it.metadata, it.imageResult)) }
                                ?: emptyList()
                        }
                        PagedReaderPagesInfo(pages, modifier = Modifier.padding(start = 10.dp))
                    }
                }

                CONTINUOUS -> {
                    var showPagesInfo by remember { mutableStateOf(false) }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showPagesInfo = !showPagesInfo }
                            .cursorForHand()
                            .padding(10.dp)
                    ) {
                        Text(stringResource(Res.string.reader_pages_info))
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

    val readingDirection = state.readingDirection.collectAsState()
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(
            readingDirection.value,
            stringResource(AppStrings.forReadingDirection(readingDirection.value))
        ),
        options = stringLabels(ContinuousReadingDirection.entries) { AppStrings.forReadingDirection(it) },
        onOptionChange = { state.onReadingDirectionChange(it.value) },
        inputFieldModifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.reader_continuous_reading_direction)) },
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        val padding = state.sidePaddingFraction.collectAsState().value
        NumberFieldWithIncrements(
            value = padding * 200,
            label = {
                Text(
                    stringResource(Res.string.reader_continuous_side_padding),
                    style = MaterialTheme.typography.labelMedium
                )
            },
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
            label = {
                Text(
                    stringResource(Res.string.reader_continuous_page_spacing),
                    style = MaterialTheme.typography.labelMedium
                )
            },
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
    val scaleType = pageState.scaleType.collectAsState().value
    Column {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                scaleType,
                stringResource(AppStrings.forScaleType(scaleType))
            ),
            options = stringLabels(LayoutScaleType.entries) { AppStrings.forScaleType(it) },
            onOptionChange = { pageState.onScaleTypeChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.reader_paged_scale_type)) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val readingDirection = pageState.readingDirection.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                readingDirection,
                stringResource(AppStrings.forReadingDirection(readingDirection))
            ),
            options = stringLabels(PagedReadingDirection.entries) { AppStrings.forReadingDirection(it) },
            onOptionChange = { pageState.onReadingDirectionChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.reader_paged_reading_direction)) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val layout = pageState.layout.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                layout,
                stringResource(AppStrings.forLayout(layout))
            ),
            options = stringLabels(PageDisplayLayout.entries) { AppStrings.forLayout(it) },
            onOptionChange = { pageState.onLayoutChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.reader_paged_layout)) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val layoutOffset = pageState.layoutOffset.collectAsState().value
        AnimatedVisibility(
            visible = layout == PageDisplayLayout.DOUBLE_PAGES || layout == PageDisplayLayout.DOUBLE_PAGES_NO_COVER,
        ) {
            SwitchWithLabel(
                checked = layoutOffset,
                onCheckedChange = pageState::onLayoutOffsetChange,
                label = { Text(stringResource(Res.string.reader_paged_offset_pages)) },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }
    }
}

@Composable
private fun PanelsReaderSettingsContent(
    readingDirection: PagedReadingDirection,
    onReadingDirectionChange: (PagedReadingDirection) -> Unit,
) {
    Column {

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                readingDirection,
                stringResource(AppStrings.forReadingDirection(readingDirection))
            ),
            options = stringLabels(PagedReadingDirection.entries) { AppStrings.forReadingDirection(it) },
            onOptionChange = { onReadingDirectionChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.reader_paged_reading_direction)) },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun BookTitles(book: KomeliaBook) {
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
    Column(modifier) {
        if (availableUpsamplingModes.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(
                    upsamplingMode,
                    stringResource(AppStrings.forUpsamplingMode(upsamplingMode))
                ),
                options = stringLabels(availableUpsamplingModes) { AppStrings.forUpsamplingMode(it) },
                onOptionChange = { onUpsamplingModeChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.reader_image_upsampling_mode)) },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        if (availableDownsamplingKernels.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(
                    downsamplingKernel,
                    stringResource(AppStrings.forDownsamplingKernel(downsamplingKernel))
                ),
                options = stringLabels(availableDownsamplingKernels) { AppStrings.forDownsamplingKernel(it) },
                onOptionChange = { onDownsamplingKernelChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.reader_image_downsampling_kernel)) },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }


        val platform = LocalPlatform.current
        if (platform != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = linearLightDownsampling,
                onCheckedChange = onLinearLightDownsamplingChange,
                label = { Text(stringResource(Res.string.reader_image_linear_light_downsampling)) },
                supportingText = {
                    Text(
                        stringResource(Res.string.reader_image_linear_light_downsampling_desc),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }
    }
}
