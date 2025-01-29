package io.github.snd_r.komelia.ui.reader.image.settings

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.image.ReaderImage
import io.github.snd_r.komelia.platform.PlatformType.DESKTOP
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.reader.image.PageMetadata
import io.github.snd_r.komelia.ui.reader.image.ReaderState
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.image.common.PageSpreadProgressSlider
import io.github.snd_r.komelia.ui.reader.image.common.ProgressSlider
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import kotlinx.coroutines.launch

@Composable
fun BoxScope.SettingsOverlay(
    show: Boolean,
    onDismiss: () -> Unit,
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    screenScaleState: ScreenScaleState,
    isColorCorrectionsActive: Boolean,
    onColorCorrectionClick: () -> Unit,
    onSeriesPress: () -> Unit,
    onBookPress: () -> Unit,
    ohShowHelpDialogChange: (Boolean) -> Unit,
) {
    if (!show) return
    val windowWidth = LocalWindowWidth.current
    val platform = LocalPlatform.current
    val book = commonReaderState.booksState.collectAsState().value?.currentBook
    val readerType = commonReaderState.readerType.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val zoom = screenScaleState.zoom.collectAsState().value
    val decoder = commonReaderState.decoderSettings.collectAsState().value
    val decoderDescriptor = commonReaderState.currentDecoderDescriptor.collectAsState().value
    val stretchToFit = commonReaderState.imageStretchToFit.collectAsState().value
    val cropBorders = commonReaderState.cropBorders.collectAsState().value
    if ((windowWidth == COMPACT || windowWidth == MEDIUM) && platform != DESKTOP) {
        BottomSheetSettingsOverlay(
            book = book,
            readerType = readerType,
            onReaderTypeChange = commonReaderState::onReaderTypeChange,
            isColorCorrectionsActive = isColorCorrectionsActive,
            onColorCorrectionClick = onColorCorrectionClick,
            onSeriesPress = onSeriesPress,
            decoder = decoder,
            decoderDescriptor = decoderDescriptor,
            onUpscaleMethodChange = commonReaderState::onUpscaleMethodChange,
            stretchToFit = stretchToFit,
            onStretchToFitChange = commonReaderState::onStretchToFitChange,
            cropBorders = cropBorders,
            onCropBordersChange = commonReaderState::onCropBordersChange,
            zoom = zoom,
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,
        )
    } else {
        SettingsSideMenuOverlay(
            book = book,
            readerType = readerType,
            onReaderTypeChange = commonReaderState::onReaderTypeChange,
            isColorCorrectionsActive = isColorCorrectionsActive,
            onColorCorrectionClick = onColorCorrectionClick,
            onSeriesPress = onSeriesPress,
            onBookPress = onBookPress,
            decoder = decoder,
            decoderDescriptor = decoderDescriptor,
            onUpscaleMethodChange = commonReaderState::onUpscaleMethodChange,
            stretchToFit = stretchToFit,
            onStretchToFitChange = commonReaderState::onStretchToFitChange,
            cropBorders = cropBorders,
            onCropBordersChange = commonReaderState::onCropBordersChange,
            zoom = zoom,
            showImageSettings = commonReaderState.expandImageSettings.collectAsState().value,
            onShowImageSettingsChange = { commonReaderState.expandImageSettings.value = it },
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,
            onShowHelpMenu = { ohShowHelpDialogChange(true) },
            onDismiss = onDismiss,
        )
    }

    when (readerType) {
        PAGED -> {
            val readingDirection = pagedReaderState.readingDirection.collectAsState().value
            val layoutDirection = remember(readingDirection) {
                when (readingDirection) {
                    PagedReaderState.ReadingDirection.LEFT_TO_RIGHT -> Ltr
                    PagedReaderState.ReadingDirection.RIGHT_TO_LEFT -> Rtl
                }
            }
            PageSpreadProgressSlider(
                pageSpreads = pagedReaderState.pageSpreads.collectAsState().value,
                currentSpreadIndex = pagedReaderState.currentSpreadIndex.collectAsState().value,
                onPageNumberChange = pagedReaderState::onPageChange,
                show = show,
                layoutDirection = layoutDirection,
                modifier = Modifier.align(Alignment.BottomStart)
            )


        }

        CONTINUOUS -> {
            val readingDirection = continuousReaderState.readingDirection.collectAsState().value
            val layoutDirection = remember(readingDirection) {
                when (readingDirection) {
                    ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM -> Ltr
                    ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT -> Ltr
                    ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT -> Rtl
                }
            }

            ProgressSlider(
                pages = continuousReaderState.currentBookPages.collectAsState(emptyList()).value,
                currentPageIndex = continuousReaderState.currentBookPageIndex.collectAsState(0).value,
                onPageNumberChange = { coroutineScope.launch { continuousReaderState.scrollToBookPage(it + 1) } },
                show = show,
                layoutDirection = layoutDirection,
                modifier = Modifier.align(Alignment.BottomStart)
            )

        }
    }
}

@Composable
fun PagedReaderPagesInfo(
    spread: PagedReaderState.PageSpread,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        HorizontalDivider(Modifier.padding(vertical = 5.dp))

        val readerStrings = LocalStrings.current.reader
        spread.pages.forEach { page ->
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

@Composable
fun ContinuousReaderPagesInfo(
    lazyListState: LazyListState,
    waitForImage: suspend (page: PageMetadata) -> ReaderImage?,
    modifier: Modifier = Modifier,
) {
    var visiblePages by remember { mutableStateOf<List<Pair<PageMetadata, ReaderImage?>>>(emptyList()) }
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layout ->
                visiblePages = layout.visibleItemsInfo
                    .mapNotNull { lazyItem ->
                        if (lazyItem.key is PageMetadata) lazyItem.key as PageMetadata
                        else null
                    }
                    .map { it to waitForImage(it) }
            }
    }

    val readerStrings = LocalStrings.current.reader
    Column(modifier) {
        HorizontalDivider(Modifier.padding(vertical = 5.dp))
        for ((page, image) in visiblePages) {
            Text("${readerStrings.pageNumber} ${page.pageNumber}.", style = MaterialTheme.typography.bodyMedium)

            val currentSize = image?.currentSize?.collectAsState()?.value
            if (currentSize != null) {
                Text("${readerStrings.pageDisplaySize} ${currentSize.width} x ${currentSize.height}")
            }

            if (page.size != null) {
                Text("${readerStrings.pageOriginalSize}: ${page.size.width} x ${page.size.height}")
            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
        }
    }
}