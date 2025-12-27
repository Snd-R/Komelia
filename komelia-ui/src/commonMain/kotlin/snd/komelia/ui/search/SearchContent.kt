package snd.komelia.ui.search

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
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.cards.BookDetailedListCard
import snd.komelia.ui.common.cards.SeriesDetailedListCard
import snd.komelia.ui.common.components.Pagination
import snd.komelia.ui.platform.VerticalScrollbar
import snd.komelia.ui.platform.WindowSizeClass
import snd.komelia.ui.search.SearchViewModel.SearchResultsTab
import snd.komga.client.series.KomgaSeries

@Composable
fun SearchContent(
    query: String,
    searchType: SearchResultsTab,
    onSearchTypeChange: (SearchResultsTab) -> Unit,

    bookResults: List<KomeliaBook>,
    bookCurrentPage: Int,
    bookTotalPages: Int,
    onBookPageChange: (Int) -> Unit,
    onBookClick: (KomeliaBook) -> Unit,

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
            WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> Modifier.fillMaxWidth()
            WindowSizeClass.EXPANDED -> Modifier.fillMaxWidth(.8f)
            WindowSizeClass.FULL -> Modifier.width(1200.dp)
        }
        val scrollState = rememberLazyListState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchToolBar(
                searchType = searchType,
                onSearchTypeChange = onSearchTypeChange,
                hasSeries = seriesResults.isNotEmpty(),
                hasBooks = bookResults.isNotEmpty(),
                modifier = widthModifier
            )

            LazyColumn(
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (searchType) {
                    SearchResultsTab.SERIES -> {
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

                    SearchResultsTab.BOOKS -> {
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
    searchType: SearchResultsTab,
    onSearchTypeChange: (SearchResultsTab) -> Unit,
    hasSeries: Boolean,
    hasBooks: Boolean,
    modifier: Modifier
) {
    if (!hasSeries && !hasBooks) return
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
        if (hasSeries) {
            FilterChip(
                onClick = { onSearchTypeChange(SearchResultsTab.SERIES) },
                selected = searchType == SearchResultsTab.SERIES,
                label = { Text("Series") },
                colors = chipColors,
                border = null,
            )
        }
        if (hasBooks) {
            FilterChip(
                onClick = { onSearchTypeChange(SearchResultsTab.BOOKS) },
                selected = searchType == SearchResultsTab.BOOKS,
                label = { Text("Books") },
                colors = chipColors,
                border = null,
            )
        }
    }
}

