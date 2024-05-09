package io.github.snd_r.komelia.ui.common.itemlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.DraggableImageCard
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.series.KomgaSeries
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
fun SeriesLazyCardGrid(
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions?,

    selectedSeries: List<KomgaSeries> = emptyList(),
    onSeriesSelect: ((KomgaSeries) -> Unit)? = null,

    reorderable: Boolean = false,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    minSize: Dp = 200.dp,
    gridState: LazyGridState = rememberLazyGridState(),

    modifier: Modifier = Modifier,

    beforeContent: @Composable () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val reorderableLazyGridState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        onMove = { from, to -> onReorder(from.index - 1, to.index - 1) }
    )
    LaunchedEffect(reorderableLazyGridState.isAnyItemDragging) {
        onReorderDragStateChange(reorderableLazyGridState.isAnyItemDragging)
    }


    Box(modifier) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier.padding(horizontal = 10.dp).fillMaxHeight()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                beforeContent()
            }

            items(items = series, key = { it.id.value }) { series ->
                val isSelected = remember(selectedSeries) { selectedSeries.any { it.id == series.id } }
                DraggableImageCard(
                    key = series.id.value,
                    dragEnabled = reorderable,
                    reorderableState = reorderableLazyGridState
                ) {
                    SeriesImageCard(
                        series = series,
                        onSeriesClick = { onSeriesClick(series) },
                        seriesMenuActions = seriesMenuActions,
                        isSelected = isSelected,
                        onSeriesSelect = onSeriesSelect?.let { { onSeriesSelect(series) } },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Pagination(
                    totalPages = totalPages,
                    currentPage = currentPage,
                    onPageChange = {
                        coroutineScope.launch {
                            onPageChange(it)
                            gridState.scrollToItem(0)
                        }
                    }
                )
            }
        }

        VerticalScrollbar(gridState, Modifier.align(Alignment.TopEnd))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyGridItemScope.DraggableSeriesCard(
    series: KomgaSeries,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions?,
    isSelected: Boolean = false,
    onSeriesSelect: ((KomgaSeries) -> Unit)?,
    reorderableState: ReorderableLazyGridState
) {
    val platform = LocalPlatform.current
    ReorderableItem(reorderableState, key = series.id.value) {
        if (platform == PlatformType.MOBILE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SeriesImageCard(
                    series = series,
                    onSeriesClick = { onSeriesClick(series) },
                    seriesMenuActions = seriesMenuActions,
                    isSelected = isSelected,
                    onSeriesSelect = onSeriesSelect?.let { { onSeriesSelect(series) } },
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .draggableHandle()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.DragHandle, null) }
            }
        } else {
            SeriesImageCard(
                series = series,
                onSeriesClick = { onSeriesClick(series) },
                seriesMenuActions = seriesMenuActions,
                isSelected = isSelected,
                onSeriesSelect = onSeriesSelect?.let { { onSeriesSelect(series) } },
                modifier = Modifier.fillMaxSize().draggableHandle()
            )
        }
    }
}