package io.github.snd_r.komelia.ui.reader.continuous

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ErrorResult
import coil3.request.ImageResult
import coil3.request.SuccessResult
import io.github.snd_r.komelia.platform.ReaderImage
import io.github.snd_r.komelia.ui.LocalKeyEvents
import io.github.snd_r.komelia.ui.reader.PageMetadata
import io.github.snd_r.komelia.ui.reader.common.ScalableContainer
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState.ReadingDirection.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first

@Composable
fun ContinuousReaderContent(
    state: ContinuousReaderState,
) {
    ScalableContainer(state.screenScaleState) {

        val pages = state.pages.collectAsState().value
        if (pages.isEmpty()) return@ScalableContainer


        val sidePadding = with(LocalDensity.current) { state.sidePaddingPx.collectAsState().value.toDp() }

        val readingDirection = state.readingDirection.collectAsState().value
        when (readingDirection) {
            TOP_TO_BOTTOM -> VerticalLayout(state, pages, sidePadding)
            LEFT_TO_RIGHT -> HorizontalLayout(state, pages, sidePadding, false)
            RIGHT_TO_LEFT -> HorizontalLayout(state, pages, sidePadding, true)
        }
        val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
        LaunchedEffect(readingDirection) {
            registerPagedReaderKeyboardEvents(
                keyEvents = keyEvents,
                state = state,
            )
        }
    }
}

@Composable
private fun VerticalLayout(
    state: ContinuousReaderState,
    pages: List<PageMetadata>,
    sidePadding: Dp
) {
    val areaSize = state.screenScaleState.areaSize.collectAsState()
    val targetSize = state.screenScaleState.targetSize.collectAsState()
    LazyColumn(
        state = state.lazyListState,
        contentPadding = PaddingValues(start = sidePadding, end = sidePadding),
        userScrollEnabled = false,
    ) {
        items(pages) { page ->
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
    pages: List<PageMetadata>,
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
        items(pages) { page ->
            val width = remember(page.size, areaSize.value, targetSize.value) {
                state.getContentSizePx(page).width
            }
            println("page width $width")

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

private suspend fun handlePageScrollEvents(state: ContinuousReaderState) {
    state.lazyListState.scrollToItem(state.currentPageIndex.first())

    var previousFistIndex = state.lazyListState.layoutInfo.visibleItemsInfo.first().index
    var previousLastIndex = state.lazyListState.layoutInfo.visibleItemsInfo.last().index
    snapshotFlow { state.lazyListState.layoutInfo }
        .collect {
            val firstVisibleIndex = it.visibleItemsInfo.first().index
            val lastVisibleIndex = it.visibleItemsInfo.last().index

            when {
                // scrolled back
                previousFistIndex > firstVisibleIndex -> state.onPageIndexChange(firstVisibleIndex)
                // scrolled through more than 1 item (possible navigation jump)
                (firstVisibleIndex - previousFistIndex) > 2 -> state.onPageIndexChange(firstVisibleIndex)
                // scrolled forward
                previousLastIndex < lastVisibleIndex -> state.onPageIndexChange(lastVisibleIndex)

                else -> return@collect
            }

            previousFistIndex = firstVisibleIndex
            previousLastIndex = lastVisibleIndex
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

private suspend fun registerPagedReaderKeyboardEvents(
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
                    Key.MoveHome -> state.scrollToPage(0)
                    Key.MoveEnd -> state.scrollToPage(state.pages.value.size - 1)
                    Key.V -> state.onReadingDirectionChange(TOP_TO_BOTTOM)
                    Key.L -> state.onReadingDirectionChange(LEFT_TO_RIGHT)
                    Key.R -> state.onReadingDirectionChange(RIGHT_TO_LEFT)
                    else -> {}
                }
            }
        }
    }
}
