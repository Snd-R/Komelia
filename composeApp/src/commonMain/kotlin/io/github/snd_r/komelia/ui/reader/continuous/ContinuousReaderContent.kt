package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ErrorResult
import coil3.request.ImageResult
import coil3.request.SuccessResult
import io.github.snd_r.komelia.platform.ReaderImage
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import io.github.snd_r.komelia.ui.reader.common.ContinuousReaderHelpDialog
import io.github.snd_r.komelia.ui.reader.common.ProgressSlider
import io.github.snd_r.komelia.ui.reader.common.ReaderControlsOverlay
import io.github.snd_r.komelia.ui.reader.common.ScalableContainer
import io.github.snd_r.komelia.ui.reader.common.SettingsMenu
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.BookPagesInterval
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.*
import io.github.snd_r.komga.book.KomgaBook
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BoxScope.ContinuousReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,

    screenScaleState: ScreenScaleState,
    continuousReaderState: ContinuousReaderState,
    readerState: ReaderState,

    book: KomgaBook?,
    onBookBackClick: () -> Unit,
    onSeriesBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val readingDirection = continuousReaderState.readingDirection.collectAsState().value

    val layoutDirection = remember(readingDirection) {
        when (readingDirection) {
            TOP_TO_BOTTOM -> LayoutDirection.Ltr
            LEFT_TO_RIGHT -> LayoutDirection.Ltr
            RIGHT_TO_LEFT -> LayoutDirection.Rtl
        }
    }
    val orientation = remember(readingDirection) {
        when (readingDirection) {
            TOP_TO_BOTTOM -> Orientation.Vertical
            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> Orientation.Horizontal
        }
    }

    if (showHelpDialog) {
        ContinuousReaderHelpDialog(
            orientation = orientation,
            onDismissRequest = { onShowHelpDialogChange(false) }
        )
    }

    val areaSize = screenScaleState.areaSize.collectAsState().value

    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = {
            when (orientation) {
                Orientation.Vertical -> continuousReaderState.scrollForward(areaSize.height.toFloat())
                Orientation.Horizontal -> continuousReaderState.scrollForward(areaSize.width.toFloat())
            }
        },
        onPrevPageClick = {
            when (orientation) {
                Orientation.Vertical -> continuousReaderState.scrollBackward(areaSize.height.toFloat())
                Orientation.Horizontal -> continuousReaderState.scrollBackward(areaSize.width.toFloat())
            }
        },
        contentAreaSize = areaSize,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
    ) {
        ScalableContainer(continuousReaderState.screenScaleState) {
            ReaderPages(state = continuousReaderState)
        }
    }

    SettingsMenu(
        book = book,
        onMenuDismiss = { onShowSettingsMenuChange(false) },
        onShowHelpMenu = { onShowHelpDialogChange(true) },
        show = showSettingsMenu,
        settingsState = readerState,
        screenScaleState = screenScaleState,
        onSeriesPress = onSeriesBackClick,
        onBookClick = onBookBackClick,
        readerSettingsContent = { ContinuousReaderSettingsContent(continuousReaderState) }
    )

    ProgressSlider(
        pages = continuousReaderState.currentBookPages.collectAsState(emptyList()).value,
        currentPageIndex = continuousReaderState.currentBookPageIndex.collectAsState(0).value,
        onPageNumberChange = { coroutineScope.launch { continuousReaderState.scrollToBookPage(it + 1) } },
        show = showSettingsMenu,
        layoutDirection = layoutDirection,
        modifier = Modifier.align(Alignment.BottomStart),
    )
}


@Composable
private fun ReaderPages(state: ContinuousReaderState, ) {
    val pageIntervals = state.pageIntervals.collectAsState().value
    val sidePadding = with(LocalDensity.current) { state.sidePaddingPx.collectAsState().value.toDp() }
    val readingDirection = state.readingDirection.collectAsState().value
    when (readingDirection) {
        TOP_TO_BOTTOM -> VerticalLayout(
            state = state,
            pageIntervals = pageIntervals,
            sidePadding = sidePadding
        )

        LEFT_TO_RIGHT -> HorizontalLayout(
            state = state,
            pageIntervals = pageIntervals,
            sidePadding = sidePadding,
            reversed = false
        )

        RIGHT_TO_LEFT -> HorizontalLayout(
            state = state,
            pageIntervals = pageIntervals,
            sidePadding = sidePadding,
            reversed = true
        )
    }
    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(readingDirection) {
        registerKeyboardEvents(
            keyEvents = keyEvents,
            state = state,
        )
    }
}

@Composable
private fun VerticalLayout(
    state: ContinuousReaderState,
    pageIntervals: List<BookPagesInterval>,
    sidePadding: Dp
) {
    val areaSize = state.screenScaleState.areaSize.collectAsState()
    val targetSize = state.screenScaleState.targetSize.collectAsState()
    LazyColumn(
        state = state.lazyListState,
        contentPadding = PaddingValues(start = sidePadding, end = sidePadding),
        userScrollEnabled = false,
    ) {
        continuousPagesLayout(pageIntervals) { page ->
            val height = remember(page.size, areaSize.value, targetSize.value) {
                state.getContentSizePx(page).height
            }
            Column(Modifier.animateContentSize(spring(stiffness = Spring.StiffnessVeryLow)).fillMaxWidth()) {
                Image(
                    state = state,
                    page = page,
                    modifier = Modifier.height(with(LocalDensity.current) { height.toDp() })
                )
                Spacer(Modifier.height(state.pageSpacing.collectAsState().value.dp))
            }
        }

    }

    LaunchedEffect(Unit) { handlePageScrollEvents(state) }
}

@Composable
private fun HorizontalLayout(
    state: ContinuousReaderState,
    pageIntervals: List<BookPagesInterval>,
    sidePadding: Dp,
    reversed: Boolean
) {

    val areaSize = state.screenScaleState.areaSize.collectAsState()
    val targetSize = state.screenScaleState.targetSize.collectAsState()

    LazyRow(
        state = state.lazyListState,
        contentPadding = PaddingValues(top = sidePadding, bottom = sidePadding),
        userScrollEnabled = false,
        reverseLayout = reversed
    ) {
        continuousPagesLayout(pageIntervals) { page ->
            val width = remember(page.size, areaSize.value, targetSize.value) { state.getContentSizePx(page).width }
            Row(Modifier.animateContentSize(spring(stiffness = Spring.StiffnessVeryLow)).fillMaxHeight()) {
                Image(
                    state = state,
                    page = page,
                    modifier = Modifier.width(with(LocalDensity.current) { width.toDp() })
                )
                Spacer(Modifier.width(state.pageSpacing.collectAsState().value.dp))
            }

        }
    }

    LaunchedEffect(Unit) { handlePageScrollEvents(state) }
}

private fun LazyListScope.continuousPagesLayout(
    pageIntervals: List<BookPagesInterval>,
    pageContent: @Composable (PageMetadata) -> Unit,
) {
    item {
        Box(
            modifier = Modifier.sizeIn(minHeight = 300.dp, minWidth = 300.dp).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Reached the start of the series", style = MaterialTheme.typography.titleLarge)
        }
    }
    pageIntervals.forEachIndexed { index, interval ->
        if (index != 0) {
            item {
                Column(
                    modifier = Modifier.sizeIn(minHeight = 300.dp, minWidth = 300.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    pageIntervals.getOrNull(index - 1)?.let { previous ->
                        Column {
                            Text("Previous:", style = MaterialTheme.typography.bodyMedium)
                            Text(previous.book.name, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(Modifier.size(50.dp))
                    Column {
                        Text("Current:", style = MaterialTheme.typography.bodyMedium)
                        Text(interval.book.name, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        items(interval.pages, key = { it }) { page -> pageContent(page) }
    }

    item {
        Box(
            modifier = Modifier.sizeIn(minHeight = 300.dp, minWidth = 300.dp).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Reached the end of the series", style = MaterialTheme.typography.titleLarge) }
    }

}

private suspend fun handlePageScrollEvents(state: ContinuousReaderState) {
    val visibleItems = state.lazyListState.layoutInfo.visibleItemsInfo
    var previousFistPage = visibleItems.first { it.key is PageMetadata }.key as PageMetadata
    var previousLastPage = visibleItems.last { it.key is PageMetadata }.key as PageMetadata

    snapshotFlow { state.lazyListState.layoutInfo }.collect { layout ->
        val firstPage = layout.visibleItemsInfo.first { it.key is PageMetadata }.key as PageMetadata
        val lastPage = layout.visibleItemsInfo.last { it.key is PageMetadata }.key as PageMetadata

        when {
            previousFistPage.bookId != firstPage.bookId -> state.onCurrentPageChange(firstPage)
            previousLastPage.bookId != lastPage.bookId -> state.onCurrentPageChange(lastPage)

            // scrolled back
            previousFistPage.pageNumber > firstPage.pageNumber -> state.onCurrentPageChange(firstPage)

            // scrolled through more than 1 item (possible navigation jump)
            (firstPage.pageNumber - previousFistPage.pageNumber) > 2 -> state.onCurrentPageChange(firstPage)

            // scrolled forward
            previousLastPage.pageNumber < lastPage.pageNumber -> state.onCurrentPageChange(lastPage)

            else -> return@collect
        }

        previousFistPage = firstPage
        previousLastPage = lastPage
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun Image(
    state: ContinuousReaderState,
    page: PageMetadata,
    modifier: Modifier
) {
    val transforms = state.screenScaleState.transformation.collectAsState().value
    val targetSize = state.screenScaleState.targetSize.collectAsState().value
    val readingDirection = state.readingDirection.collectAsState().value
    val allowUpsample = state.allowUpsample.collectAsState().value

    val imageScale = remember(readingDirection) {
        when (readingDirection) {
            TOP_TO_BOTTOM -> ContentScale.FillWidth
            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> ContentScale.FillHeight
        }
    }

    var image by remember { mutableStateOf<ImageResult?>(null) }
    LaunchedEffect(transforms.scale, targetSize, allowUpsample) {
        image = state.getImage(page)
    }
    Box(modifier) {
        when (val result = image) {
            is ErrorResult -> Text("Page load error")
            is SuccessResult -> {
                ReaderImage(
                    image = result.image,
                    contentScale = imageScale,
                    modifier = Modifier.fillMaxSize()

                )
            }

            null -> imagePlaceholder()
        }
    }
}

@Composable
private fun imagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.TopCenter
    ) {
        CircularProgressIndicator()
    }
}

private suspend fun registerKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    state: ContinuousReaderState,
) {

    keyEvents.collect { event ->
        when (event.type) {
            KeyDown -> {
                val readingDirection = state.readingDirection.value
                when {
                    readingDirection == LEFT_TO_RIGHT && event.key == Key.DirectionLeft -> state.scrollBackward(100f)
                    readingDirection == LEFT_TO_RIGHT && event.key == Key.DirectionRight -> state.scrollForward(100f)

                    readingDirection == RIGHT_TO_LEFT && event.key == Key.DirectionLeft -> state.scrollForward(100f)
                    readingDirection == RIGHT_TO_LEFT && event.key == Key.DirectionRight -> state.scrollBackward(100f)

                    readingDirection == TOP_TO_BOTTOM && event.key == Key.DirectionDown -> state.scrollForward(100f)
                    readingDirection == TOP_TO_BOTTOM && event.key == Key.DirectionUp -> state.scrollBackward(100f)
                }
            }

            KeyUp -> {
                when (event.key) {
                    Key.MoveHome -> state.scrollToBookPage(0)
                    Key.MoveEnd -> state.scrollToBookPage(state.currentBookPages.first().size)

                    Key.V -> state.onReadingDirectionChange(TOP_TO_BOTTOM)
                    Key.L -> state.onReadingDirectionChange(LEFT_TO_RIGHT)
                    Key.R -> state.onReadingDirectionChange(RIGHT_TO_LEFT)
                    else -> {}
                }
            }
        }
    }
}