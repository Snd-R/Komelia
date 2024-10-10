package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
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
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.LEFT_TO_RIGHT
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.RIGHT_TO_LEFT
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.TOP_TO_BOTTOM
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ImageResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook

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
            TOP_TO_BOTTOM -> Ltr
            LEFT_TO_RIGHT -> Ltr
            RIGHT_TO_LEFT -> Rtl
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
        onNexPageClick = { coroutineScope.launch { continuousReaderState.scrollScreenForward() } },
        onPrevPageClick = { coroutineScope.launch { continuousReaderState.scrollScreenBackward() } },
        contentAreaSize = areaSize,
        isSettingsMenuOpen = showSettingsMenu,
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
    val coroutineScope = rememberCoroutineScope()
    val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
    LaunchedEffect(readingDirection) {
        registerKeyboardEvents(
            keyEvents = keyEvents,
            state = state,
            scrollScope = coroutineScope
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
    var imageResult by remember { mutableStateOf<ImageResult?>(null) }
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
    ) {

        when (val result = imageResult) {
            is ImageResult.Error -> Text(result.throwable.message ?: "Page load error")
            is ImageResult.Success ->
                Image(
                    modifier = modifier.background(Color.White),
                    painter = result.image.painter.collectAsState().value,
                    contentDescription = null,
                )

            null -> Box(
                modifier = Modifier.fillMaxSize().background(Color.White),
                contentAlignment = Alignment.TopCenter,
                content = { CircularProgressIndicator(color = Color.Black) }
            )
        }
    }
}

private suspend fun registerKeyboardEvents(
    keyEvents: SharedFlow<KeyEvent>,
    state: ContinuousReaderState,
    scrollScope: CoroutineScope
) {

    var upKeyPressed = false
    var downKeyPressed = false
    var leftKeyPressed = false
    var rightKeyPressed = false

    keyEvents.collect { event ->
        val readingDirection = state.readingDirection.value
        when (event.type) {
            KeyDown -> {
                when {
                    event.key == Key.DirectionLeft && readingDirection == LEFT_TO_RIGHT -> state.scrollBy(100f)
                    event.key == Key.DirectionRight && readingDirection == LEFT_TO_RIGHT -> state.scrollBy(-100f)

                    event.key == Key.DirectionLeft && readingDirection == RIGHT_TO_LEFT -> state.scrollBy(100f)
                    event.key == Key.DirectionRight && readingDirection == RIGHT_TO_LEFT -> state.scrollBy(-100f)

                    event.key == Key.DirectionDown && readingDirection == TOP_TO_BOTTOM -> state.scrollBy(-100f)
                    event.key == Key.DirectionUp && readingDirection == TOP_TO_BOTTOM -> state.scrollBy(100f)

                    event.key == Key.DirectionDown && readingDirection == LEFT_TO_RIGHT -> {
                        if (!downKeyPressed) scrollScope.launch { state.scrollScreenForward() }
                        downKeyPressed = true
                    }

                    event.key == Key.DirectionUp && readingDirection == LEFT_TO_RIGHT -> {
                        if (!upKeyPressed) scrollScope.launch { state.scrollScreenBackward() }
                        upKeyPressed = true
                    }

                    event.key == Key.DirectionDown && readingDirection == RIGHT_TO_LEFT -> {
                        if (!downKeyPressed) scrollScope.launch { state.scrollScreenForward() }
                        downKeyPressed = true
                    }

                    event.key == Key.DirectionUp && readingDirection == RIGHT_TO_LEFT -> {
                        if (!upKeyPressed) scrollScope.launch { state.scrollScreenBackward() }
                        upKeyPressed = true
                    }

                    event.key == Key.DirectionRight && readingDirection == TOP_TO_BOTTOM -> {
                        if (!rightKeyPressed) scrollScope.launch { state.scrollScreenForward() }
                        rightKeyPressed = true
                    }

                    event.key == Key.DirectionLeft && readingDirection == TOP_TO_BOTTOM -> {
                        if (!leftKeyPressed) scrollScope.launch { state.scrollScreenBackward() }
                        leftKeyPressed = true
                    }
                }
            }

            KeyUp -> {
                when (event.key) {
                    Key.MoveHome -> state.scrollToBookPage(0)
                    Key.MoveEnd -> state.scrollToBookPage(state.currentBookPages.first().size)
                    Key.V -> state.onReadingDirectionChange(TOP_TO_BOTTOM)
                    Key.L -> state.onReadingDirectionChange(LEFT_TO_RIGHT)
                    Key.R -> state.onReadingDirectionChange(RIGHT_TO_LEFT)

                    Key.DirectionDown -> downKeyPressed = false
                    Key.DirectionUp -> upKeyPressed = false
                    Key.DirectionRight -> rightKeyPressed = false
                    Key.DirectionLeft -> leftKeyPressed = false
                }
            }
        }
    }
}