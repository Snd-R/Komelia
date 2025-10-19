package io.github.snd_r.komelia.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.series.KomgaSeries

@Composable
fun HomeContent(
    filters: List<HomeFilterData>,
    onEditStart: () -> Unit,

    activeFilterNumber: Int,
    onFilterChange: (Int) -> Unit,

    cardWidth: Dp,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook, Boolean) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    Column {
        Toolbar(
            filters = filters,
            currentFilterNumber = activeFilterNumber,
            onEditStart = onEditStart,
            onFilterChange = {
                onFilterChange(it)
                coroutineScope.launch { gridState.animateScrollToItem(0) }
            },
        )
        DisplayContent(
            filters = filters,
            activeFilterNumber = activeFilterNumber,

            gridState = gridState,
            cardWidth = cardWidth,
            onSeriesClick = onSeriesClick,
            seriesMenuActions = seriesMenuActions,
            bookMenuActions = bookMenuActions,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
        )
    }
}

@Composable
private fun Toolbar(
    filters: List<HomeFilterData>,
    currentFilterNumber: Int,
    onFilterChange: (Int) -> Unit,
    onEditStart: () -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )
    val nonEmptyFilters = remember(filters) {
        filters.filter {
            when (it) {
                is BookFilterData -> it.books.isNotEmpty()
                is SeriesFilterData -> it.series.isNotEmpty()
            }
        }
    }
    Box {
        val lazyRowState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        LazyRow(
            state = lazyRowState,
            modifier = Modifier.animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                Spacer(Modifier.width(20.dp))
            }

            item {
                FilterChip(
                    onClick = onEditStart,
                    selected = false,
                    label = {
                        Icon(Icons.Default.Tune, null)
                    },
                    colors = chipColors,
                    border = null,
                )
            }

            if (filters.size > 1) {
                item {
                    FilterChip(
                        onClick = { onFilterChange(0) },
                        selected = currentFilterNumber == 0,
                        label = { Text("All") },
                        colors = chipColors,
                        border = null,
                    )
                }
            }
            items(nonEmptyFilters) { data ->
                val display = remember(data.filter) {
                    when (data) {
                        is BookFilterData -> data.books.isNotEmpty()
                        is SeriesFilterData -> data.series.isNotEmpty()
                    }
                }
                if (display) {
                    FilterChip(
                        onClick = { onFilterChange(data.filter.order) },
                        selected = currentFilterNumber == data.filter.order || filters.size == 1,
                        label = { Text(data.filter.label) },
                        colors = chipColors,
                        border = null,
                    )
                }
            }
            item {
                Spacer(Modifier.width(40.dp))
            }
        }

        if (LocalPlatform.current != PlatformType.MOBILE) {
            Row {
                if (lazyRowState.canScrollBackward) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { coroutineScope.launch { lazyRowState.animateScrollBy(-200.0f) } },
                    ) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (lazyRowState.canScrollForward) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { coroutineScope.launch { lazyRowState.animateScrollBy(200.0f) } },
                    ) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayContent(
    filters: List<HomeFilterData>,
    activeFilterNumber: Int,
    gridState: LazyGridState,
    cardWidth: Dp,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook, Boolean) -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(horizontal = 20.dp),
        state = gridState,
        columns = GridCells.Adaptive(cardWidth),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(bottom = 50.dp)
    ) {
        for (data in filters) {
            if (activeFilterNumber == 0 || data.filter.order == activeFilterNumber) {
                when (data) {
                    is BookFilterData -> BookFilterEntry(
                        label = data.filter.label,
                        books = data.books,
                        bookMenuActions = bookMenuActions,
                        onBookClick = onBookClick,
                        onBookReadClick = onBookReadClick,
                    )

                    is SeriesFilterData -> SeriesFilterEntries(
                        label = data.filter.label,
                        series = data.series,
                        onSeriesClick = onSeriesClick,
                        seriesMenuActions = seriesMenuActions,
                    )

                }
            }
        }
    }
}

private fun LazyGridScope.BookFilterEntry(
    label: String,
    books: List<KomgaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook, Boolean) -> Unit,
) {
    if (books.isEmpty()) return

    item(span = { GridItemSpan(maxLineSpan) }) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            HorizontalDivider()
        }
    }
    items(books) { book ->
        BookImageCard(
            book = book,
            onBookClick = { onBookClick(book) },
            onBookReadClick = { onBookReadClick(book, it) },
            bookMenuActions = bookMenuActions,
            showSeriesTitle = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun LazyGridScope.SeriesFilterEntries(
    label: String,
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
) {
    if (series.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            HorizontalDivider()
        }
    }

    items(series) {
        SeriesImageCard(
            series = it,
            onSeriesClick = { onSeriesClick(it) },
            seriesMenuActions = seriesMenuActions,
            modifier = Modifier.fillMaxSize()
        )
    }
}
