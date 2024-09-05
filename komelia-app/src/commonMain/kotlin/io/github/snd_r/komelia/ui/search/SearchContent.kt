package io.github.snd_r.komelia.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.BookDetailedListCard
import io.github.snd_r.komelia.ui.common.cards.SeriesDetailedListCard
import io.github.snd_r.komelia.ui.search.SearchViewModel.SearchType
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId


@Composable
fun SearchContent(
    query: String,
    searchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,

    bookResults: List<KomgaBook>,
    bookCurrentPage: Int,
    bookTotalPages: Int,
    onBookPageChange: (Int) -> Unit,
    onBookClick: (KomgaBook) -> Unit,

    seriesResults: List<KomgaSeries>,
    seriesCurrentPage: Int,
    seriesTotalPages: Int,
    onSeriesPageChange: (Int) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
) {
    if (query.isNotBlank() && bookResults.isEmpty() && seriesResults.isEmpty()) {
        EmptySearchResults()
        return
    }

    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        val widthModifier = when (LocalWindowWidth.current) {
            WindowWidth.COMPACT, WindowWidth.MEDIUM -> Modifier.fillMaxWidth()
            WindowWidth.EXPANDED -> Modifier.fillMaxWidth(.8f)
            WindowWidth.FULL -> Modifier.width(1200.dp)
        }
        val scrollState = rememberLazyListState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchToolBar(
                searchType = searchType,
                onSearchTypeChange = onSearchTypeChange,
                modifier = widthModifier
            )

            LazyColumn(
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (searchType) {
                    SearchType.SERIES -> {
                        items(seriesResults) { series ->
                            SeriesDetailedListCard(
                                series = series,
                                onClick = { onSeriesClick(series) },
                                modifier = widthModifier
                            )
                        }
                        item {
                            Pagination(
                                totalPages = seriesTotalPages,
                                currentPage = seriesCurrentPage,
                                onPageChange = onSeriesPageChange
                            )
                        }
                    }

                    SearchType.BOOKS -> {
                        items(bookResults) { book ->
                            BookDetailedListCard(
                                book = book,
                                onClick = { onBookClick(book) },
                                modifier = widthModifier
                            )
                        }
                        item {
                            Pagination(
                                totalPages = bookTotalPages,
                                currentPage = bookCurrentPage,
                                onPageChange = onBookPageChange
                            )
                        }

                    }
                }
            }
        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
fun BookSearchContent(
    bookResults: List<KomgaBook>,
    bookCurrentPage: Int,
    bookTotalPages: Int,
    onBookPageChange: (Int) -> Unit,
    onBookClick: (KomgaBookId) -> Unit,

//    scrollState: LazyListState,
) {
    val scrollState = rememberLazyListState()
    Box {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(bookResults) {
                BookDetailedListCard(it, onClick = { onBookClick(it.id) })
            }
            item {
                Pagination(
                    totalPages = bookTotalPages,
                    currentPage = bookCurrentPage,
                    onPageChange = onBookPageChange
                )
            }

        }
        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
fun SeriesSearchContent(
    seriesResults: List<KomgaSeries>,
    seriesCurrentPage: Int,
    seriesTotalPages: Int,
    onSeriesPageChange: (Int) -> Unit,
    onSeriesClick: (KomgaSeriesId) -> Unit,
//    scrollState: LazyListState,
) {
    val scrollState = rememberLazyListState()
    Box {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(seriesResults) {
                SeriesDetailedListCard(it, onClick = { onSeriesClick(it.id) })
            }
            item {
                Pagination(
                    totalPages = seriesTotalPages,
                    currentPage = seriesCurrentPage,
                    onPageChange = onSeriesPageChange
                )
            }
        }
        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun EmptySearchResults() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(100.dp))
        Text("The search returned no results", style = MaterialTheme.typography.titleLarge)
        Text("Try searching for something else")
    }
}

@Composable
fun SearchToolBar(
    searchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,
    modifier: Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Spacer(Modifier.width(20.dp))


        val chipColors = FilterChipDefaults.filterChipColors(

            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
        FilterChip(
            onClick = { onSearchTypeChange(SearchType.SERIES) },
            selected = searchType == SearchType.SERIES,
            label = { Text("Series") },
            colors = chipColors,
            border = null,
        )
        FilterChip(
            onClick = { onSearchTypeChange(SearchType.BOOKS) },
            selected = searchType == SearchType.BOOKS,
            label = { Text("Books") },
            colors = chipColors,
            border = null,
        )
    }
}

