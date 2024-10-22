package io.github.snd_r.komelia.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.ALL
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.KEEP_READING_BOOKS
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.ON_DECK_BOOKS
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.RECENTLY_ADDED_BOOKS
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.RECENTLY_ADDED_SERIES
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.RECENTLY_READ_BOOKS
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.RECENTLY_RELEASED_BOOKS
import io.github.snd_r.komelia.ui.home.HomeViewModel.HomeScreenFilter.RECENTLY_UPDATED_SERIES
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeries

@Composable
fun HomeContent(
    keepReadingBooks: List<KomgaBook>,
    onDeckBooks: List<KomgaBook>,
    recentlyReleasedBooks: List<KomgaBook>,
    recentlyAddedBooks: List<KomgaBook>,
    recentlyReadBooks: List<KomgaBook>,

    recentlyAddedSeries: List<KomgaSeries>,
    recentlyUpdatedSeries: List<KomgaSeries>,

    currentFilter: HomeScreenFilter,
    onFilterChange: (HomeScreenFilter) -> Unit,

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
            currentFilter = currentFilter,
            onFilterChange = {
                onFilterChange(it)
                coroutineScope.launch { gridState.animateScrollToItem(0) }
            },
            keepReadingBooks = keepReadingBooks,
            onDeckBooks = onDeckBooks,
            recentlyReleasedBooks = recentlyReleasedBooks,
            recentlyAddedBooks = recentlyAddedBooks,
            recentlyReadBooks = recentlyReadBooks,
            recentlyAddedSeries = recentlyAddedSeries,
            recentlyUpdatedSeries = recentlyUpdatedSeries,
        )
        MainContent(
            currentFilter = currentFilter,
            keepReadingBooks = keepReadingBooks,
            onDeckBooks = onDeckBooks,
            recentlyReleasedBooks = recentlyReleasedBooks,
            recentlyAddedBooks = recentlyAddedBooks,
            recentlyReadBooks = recentlyReadBooks,
            recentlyAddedSeries = recentlyAddedSeries,
            recentlyUpdatedSeries = recentlyUpdatedSeries,
            gridState = gridState,

            cardWidth = cardWidth,
            onSeriesClick = onSeriesClick,
            seriesMenuActions = seriesMenuActions,
            bookMenuActions = bookMenuActions,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick
        )
    }
}

@Composable
private fun Toolbar(
    currentFilter: HomeScreenFilter,
    onFilterChange: (HomeScreenFilter) -> Unit,
    keepReadingBooks: List<KomgaBook>,
    onDeckBooks: List<KomgaBook>,
    recentlyReleasedBooks: List<KomgaBook>,
    recentlyAddedBooks: List<KomgaBook>,
    recentlyReadBooks: List<KomgaBook>,
    recentlyAddedSeries: List<KomgaSeries>,
    recentlyUpdatedSeries: List<KomgaSeries>,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            Spacer(Modifier.width(20.dp))
        }

        item {
            FilterChip(
                onClick = { onFilterChange(ALL) },
                selected = currentFilter == ALL,
                label = { Text("All") },
                colors = chipColors,
                border = null,
            )
        }

        if (keepReadingBooks.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(KEEP_READING_BOOKS) },
                    selected = currentFilter == KEEP_READING_BOOKS,
                    label = { Text("Keep Reading") },
                    colors = chipColors,
                    border = null,
                )
            }
        if (onDeckBooks.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(ON_DECK_BOOKS) },
                    selected = currentFilter == ON_DECK_BOOKS,
                    label = { Text("On deck") },
                    colors = chipColors,
                    border = null,
                )
            }

        if (recentlyReleasedBooks.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(RECENTLY_RELEASED_BOOKS) },
                    selected = currentFilter == RECENTLY_RELEASED_BOOKS,
                    label = { Text("Recently released books") },
                    colors = chipColors,
                    border = null,

                    )
            }

        if (recentlyAddedBooks.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(RECENTLY_ADDED_BOOKS) },
                    selected = currentFilter == RECENTLY_ADDED_BOOKS,
                    label = { Text("Recently added books") },
                    colors = chipColors,
                    border = null,
                )
            }

        if (recentlyAddedSeries.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(RECENTLY_ADDED_SERIES) },
                    selected = currentFilter == RECENTLY_ADDED_SERIES,
                    label = { Text("Recently added series") },
                    colors = chipColors,
                    border = null,
                )
            }

        if (recentlyUpdatedSeries.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(RECENTLY_UPDATED_SERIES) },
                    selected = currentFilter == RECENTLY_UPDATED_SERIES,
                    label = { Text("Recently updated series") },
                    colors = chipColors,
                    border = null,
                )
            }
        if (recentlyReadBooks.isNotEmpty())
            item {
                FilterChip(
                    onClick = { onFilterChange(RECENTLY_READ_BOOKS) },
                    selected = currentFilter == RECENTLY_READ_BOOKS,
                    label = { Text("Recently read books") },
                    colors = chipColors,
                    border = null,
                )
            }

        item {
            Spacer(Modifier.width(40.dp))
        }

    }
}

@Composable
private fun MainContent(
    currentFilter: HomeScreenFilter,
    keepReadingBooks: List<KomgaBook>,
    onDeckBooks: List<KomgaBook>,
    recentlyReleasedBooks: List<KomgaBook>,
    recentlyAddedBooks: List<KomgaBook>,
    recentlyReadBooks: List<KomgaBook>,
    recentlyAddedSeries: List<KomgaSeries>,
    recentlyUpdatedSeries: List<KomgaSeries>,
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
        if (keepReadingBooks.isNotEmpty() && (currentFilter == ALL || currentFilter == KEEP_READING_BOOKS)) {
            BookFilterEntry(
                label = "Keep reading",
                books = keepReadingBooks,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
        }
        if (onDeckBooks.isNotEmpty() && (currentFilter == ALL || currentFilter == ON_DECK_BOOKS)) {
            BookFilterEntry(
                label = "On deck",
                books = onDeckBooks,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
        }
        if (recentlyReleasedBooks.isNotEmpty() && (currentFilter == ALL || currentFilter == RECENTLY_RELEASED_BOOKS)) {
            BookFilterEntry(
                label = "Recently released books",
                books = recentlyReleasedBooks,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
        }
        if (recentlyAddedBooks.isNotEmpty() && (currentFilter == ALL || currentFilter == RECENTLY_ADDED_BOOKS)) {
            BookFilterEntry(
                label = "Recently added books",
                books = recentlyAddedBooks,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
        }
        if (recentlyAddedSeries.isNotEmpty() && (currentFilter == ALL || currentFilter == RECENTLY_ADDED_SERIES)) {
            SeriesFilterEntries(
                label = "Recently added series",
                series = recentlyAddedSeries,
                onSeriesClick = onSeriesClick,
                seriesMenuActions = seriesMenuActions
            )
        }
        if (recentlyUpdatedSeries.isNotEmpty() && (currentFilter == ALL || currentFilter == RECENTLY_UPDATED_SERIES)) {
            SeriesFilterEntries(
                label = "Recently updated series",
                series = recentlyUpdatedSeries,
                onSeriesClick = onSeriesClick,
                seriesMenuActions = seriesMenuActions
            )
        }
        if (recentlyReadBooks.isNotEmpty() && (currentFilter == ALL || currentFilter == RECENTLY_READ_BOOKS)) {
            BookFilterEntry(
                label = "Recently read books",
                books = recentlyReadBooks,
                bookMenuActions = bookMenuActions,
                onBookClick = onBookClick,
                onBookReadClick = onBookReadClick
            )
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
    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
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

interface FilterEntry {
    val label: String
    val show: Boolean
    val entries: List<*>
}

class BookFilterEntry(
    override val label: String,
    override val show: Boolean,
    override val entries: List<KomgaBook>
) : FilterEntry

data class SeriesFilterEntry(
    override val label: String,
    override val show: Boolean,
    override val entries: List<KomgaSeries>,
) : FilterEntry