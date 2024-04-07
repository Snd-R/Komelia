package io.github.snd_r.komelia.ui.common.itemlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.SeriesDetailedListCard
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId
import kotlinx.coroutines.launch


@Composable
fun SeriesCardSlider(
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    seriesActions: SeriesMenuActions? = null,
    cardWidth: Dp = 200.dp,
    scrollState: LazyListState = rememberLazyListState(),
) {
    Column {
        LazyRow(state = scrollState, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            items(series) { series ->
                SeriesImageCard(
                    modifier = Modifier.width(cardWidth),
                    series = series,
                    onSeriesClick = { onSeriesClick(series.id) },
                    seriesMenuActions = seriesActions
                )
            }
        }
        HorizontalScrollbar(
            scrollState,
            Modifier.align(Alignment.End).height(10.dp),
        )
    }
}


@Composable
fun SeriesLazyCardGrid(
    series: List<KomgaSeries>,
    seriesMenuActions: SeriesMenuActions?,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    minSize: Dp = 200.dp,
    scrollState: LazyGridState = rememberLazyGridState(),
    beforeContent: @Composable () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 10.dp).fillMaxHeight()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                beforeContent()
            }

            items(items = series) {
                SeriesImageCard(
                    series = it,
                    onSeriesClick = { onSeriesClick(it.id) },
                    seriesMenuActions = seriesMenuActions,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Pagination(
                    totalPages = totalPages,
                    currentPage = currentPage,
                    onPageChange = {
                        coroutineScope.launch {
                            onPageChange(it)
                            scrollState.scrollToItem(0)
                        }
                    }
                )
            }
        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
fun SeriesVerticalList(
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    scrollState: LazyListState = rememberLazyListState(),
) {
    Box {
        LazyColumn(state = scrollState) {
            items(series) {
                SeriesDetailedListCard(series = it, onClick = { onSeriesClick(it.id) })
            }
        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }

}