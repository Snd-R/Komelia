package io.github.snd_r.komelia.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.cards.BookDetailedListCard
import io.github.snd_r.komelia.ui.common.cards.SeriesDetailedListCard
import io.github.snd_r.komelia.ui.platform.VerticalScrollbar
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId


@Composable
fun SearchContent(
    query: String,
    searchState: SearchState,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    onBackClick: () -> Unit,
    onBookClick: (KomgaBookId) -> Unit,
) {
    Column(Modifier.padding(horizontal = 50.dp)) {
        SearchToolBar(onBackClick)
        SearchResultsContent(
            query,
            searchState,
            onSeriesClick,
            onBookClick
        )
    }
}

@Composable
private fun SearchResultsContent(
    query: String,
    searchState: SearchState,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    onBookClick: (KomgaBookId) -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        when (searchState) {
            SearchState.Empty -> EmptySearchResults(query)
            SearchState.Loading -> LoadingMaxSizeIndicator()
            is SearchState.Finished -> SearchResults(
                results = searchState.searchResults,
                onSeriesClick = onSeriesClick,
                onBookClick = onBookClick
            )
        }
    }
}

@Composable
private fun EmptySearchResults(query: String) {
    Text("No results found for \"$query\"")
}

@Composable
private fun SearchResults(
    results: SearchResults,
    onSeriesClick: (KomgaSeriesId) -> Unit,
    onBookClick: (KomgaBookId) -> Unit,
) {
    val scrollState = rememberLazyListState()
    Box {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            seriesSearchResults(results.series, onSeriesClick = onSeriesClick)
            booksSearchResults(results.books, onBookClick = onBookClick)
        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}

private fun LazyListScope.seriesSearchResults(
    results: List<KomgaSeries>,
    onSeriesClick: (KomgaSeriesId) -> Unit,
) {
    if (results.isNotEmpty()) {
        item { Text("Series", modifier = Modifier.padding(top = 10.dp)) }
        items(results) {
            SeriesDetailedListCard(it, onClick = { onSeriesClick(it.id) })
        }
    }
}

private fun LazyListScope.booksSearchResults(
    results: List<KomgaBook>,
    onBookClick: (KomgaBookId) -> Unit,
) {
    if (results.isNotEmpty()) {
        item { Text("Books", modifier = Modifier.padding(top = 10.dp)) }
        items(results) {
            BookDetailedListCard(it, onClick = { onBookClick(it.id) })
        }
    }
}

@Composable
private fun SearchToolBar(
    onBackClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onBackClick() }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
        Spacer(Modifier.width(10.dp))

        Text("Search")
    }
}

