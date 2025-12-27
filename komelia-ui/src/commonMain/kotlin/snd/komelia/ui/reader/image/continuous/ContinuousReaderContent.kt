package snd.komelia.ui.reader.image.continuous

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import snd.komelia.image.ReaderImageResult
import snd.komelia.settings.model.ContinuousReadingDirection.LEFT_TO_RIGHT
import snd.komelia.settings.model.ContinuousReadingDirection.RIGHT_TO_LEFT
import snd.komelia.settings.model.ContinuousReadingDirection.TOP_TO_BOTTOM
import snd.komelia.ui.reader.image.PageMetadata
import snd.komelia.ui.reader.image.ScreenScaleState
import snd.komelia.ui.reader.image.common.ContinuousReaderHelpDialog
import snd.komelia.ui.reader.image.common.ReaderControlsOverlay
import snd.komelia.ui.reader.image.common.ReaderImageContent
import snd.komelia.ui.reader.image.common.ScalableContainer
import snd.komelia.ui.reader.image.continuous.ContinuousReaderState.BookPagesInterval

@Composable
fun BoxScope.ContinuousReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,
    screenScaleState: ScreenScaleState,
    continuousReaderState: ContinuousReaderState,
    volumeKeysNavigation: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val readingDirection = continuousReaderState.readingDirection.collectAsState().value

    val layoutDirection = remember(readingDirection) {
        when (readingDirection) {
            TOP_TO_BOTTOM -> Ltr
            LEFT_TO_RIGHT -> Ltr
            RIGHT_TO_LEFT -> Rtl
        }
    }

    if (showHelpDialog) {
        ContinuousReaderHelpDialog(
            readingDirection = readingDirection,
            onDismissRequest = { onShowHelpDialogChange(false) }
        )
    }

    val areaSize = screenScaleState.areaSize.collectAsState().value
    val keysState = remember(readingDirection, volumeKeysNavigation) {
        KeyMapState(
            readingDirection = readingDirection,
            volumeKeysNavigation = volumeKeysNavigation,
            scrollBy = continuousReaderState::scrollBy,
            scrollForward = { coroutineScope.launch { continuousReaderState.scrollScreenForward() } },
            scrollBackward = { coroutineScope.launch { continuousReaderState.scrollScreenBackward() } },
            scrollToFirstPage = { coroutineScope.launch { continuousReaderState.scrollToBookPage(0) } },
            scrollToLastPage = { coroutineScope.launch { continuousReaderState.scrollToLastPage() } },
            changeReadingDirection = continuousReaderState::onReadingDirectionChange
        )
    }
    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = { coroutineScope.launch { continuousReaderState.scrollScreenForward() } },
        onPrevPageClick = { coroutineScope.launch { continuousReaderState.scrollScreenBackward() } },
        contentAreaSize = areaSize,
        isSettingsMenuOpen = showSettingsMenu,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
        modifier = Modifier.onKeyEvent { event ->
            var consumed = true

            when (event.type) {
                KeyDown -> {
                    consumed = when (event.key) {
                        Key.DirectionLeft -> keysState.onLeftKeyDown()
                        Key.DirectionRight -> keysState.onRightKeyDown()
                        Key.DirectionDown -> keysState.onDownKeyDown()
                        Key.DirectionUp -> keysState.onUpKeyDown()
                        Key.VolumeUp -> keysState.onVolumeUpKeyDown()
                        Key.VolumeDown -> keysState.onVolumeDownKeyDown()
                        else -> false
                    }
                }

                KeyUp -> {
                    consumed = when (event.key) {
                        Key.MoveHome -> keysState.onScrollToFirstPage()
                        Key.MoveEnd -> keysState.onScrollToLastPage()
                        Key.V -> keysState.onReadingDirectionChange(TOP_TO_BOTTOM)
                        Key.L -> keysState.onReadingDirectionChange(LEFT_TO_RIGHT)
                        Key.R -> keysState.onReadingDirectionChange(RIGHT_TO_LEFT)
                        Key.DirectionDown -> keysState.onDownKeyUp()
                        Key.DirectionUp -> keysState.onUpKeyUp()
                        Key.DirectionRight -> keysState.onRightKeyUp()
                        Key.DirectionLeft -> keysState.onLeftKeyUp(event.isAltPressed)
                        Key.VolumeUp -> keysState.onVolumeUpKeyUp()
                        Key.VolumeDown -> keysState.onVolumeDownKeyUp()
                        else -> false
                    }
                }
            }

            consumed
        }
    ) {
        ScalableContainer(continuousReaderState.screenScaleState) {
            ReaderPages(state = continuousReaderState)
        }
    }
}

@Composable
private fun ReaderPages(state: ContinuousReaderState) {
    val pageIntervals = state.pageIntervals.collectAsState().value
    if (pageIntervals.isEmpty()) return
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
}

@Composable
private fun VerticalLayout(
    state: ContinuousReaderState,
    pageIntervals: List<BookPagesInterval>,
    sidePadding: Dp
) {
    LazyColumn(
        state = state.lazyListState,
        contentPadding = PaddingValues(start = sidePadding, end = sidePadding),
        userScrollEnabled = false,
    ) {
        continuousPagesLayout(pageIntervals) { page ->
            var displaySize by remember { mutableStateOf(state.guessPageDisplaySize(page)) }
            LaunchedEffect(Unit) {
                state.getPageDisplaySize(page).collect { displaySize = it }
            }
            val height = displaySize.height
            Column(
                modifier = Modifier
                    .animateContentSize(spring(stiffness = Spring.StiffnessVeryLow))
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceDim)
            ) {
                ContinuousReaderImage(
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
    LazyRow(
        state = state.lazyListState,
        contentPadding = PaddingValues(top = sidePadding, bottom = sidePadding),
        userScrollEnabled = false,
        reverseLayout = reversed
    ) {
        continuousPagesLayout(pageIntervals) { page ->
            var displaySize by remember { mutableStateOf(state.guessPageDisplaySize(page)) }
            LaunchedEffect(Unit) {
                state.getPageDisplaySize(page).collect { displaySize = it }
            }
            val width = displaySize.width
            Row(
                Modifier
                    .animateContentSize(spring(stiffness = Spring.StiffnessVeryLow))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceDim)
            ) {
                ContinuousReaderImage(
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
                            Text(
                                previous.book.metadata.title,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Spacer(Modifier.size(50.dp))
                    Column {
                        Text("Current:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            interval.book.metadata.title,
                            style = MaterialTheme.typography.titleLarge
                        )
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
    var previousFistPage = state.lazyListState.layoutInfo.visibleItemsInfo
        .first { it.key is PageMetadata }.key as PageMetadata
    var previousLastPage = state.lazyListState.layoutInfo.visibleItemsInfo
        .last { it.key is PageMetadata }.key as PageMetadata

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

@Composable
private fun ContinuousReaderImage(
    state: ContinuousReaderState,
    page: PageMetadata,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var imageResult by remember { mutableStateOf<ReaderImageResult?>(null) }
    DisposableEffect(Unit) {
        coroutineScope.launch {
            val result = state.getImage(page)
            result.image?.let { state.onPageDisplay(page, it) }
            imageResult = result
        }

        onDispose {
            imageResult?.image?.let { state.onPageDispose(page) }
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) { ReaderImageContent(imageResult) }
}

