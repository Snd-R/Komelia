package io.github.snd_r.komelia.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookQuery
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.series.KomgaSeriesQuery
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    initialQuery: String?
) : ScreenModel {

    var currentQuery by mutableStateOf(initialQuery ?: "")
    private val searchState = MutableStateFlow<SearchState>(SearchState.Empty)


    init {
        screenModelScope.launch {
            if (initialQuery != null) launch { handle(initialQuery) }

            snapshotFlow { currentQuery }
                .drop(if (initialQuery != null) 1 else 0)
                .debounce {
                    if (it.isBlank()) 0
                    else 500
                }
                .distinctUntilChanged()
                .collect { handle(it) }
        }
    }

    private suspend fun handle(query: String) {
        if (query.isBlank()) {
            searchState.value = SearchState.Empty
            return
        }

        searchState.value = SearchState.Loading

        val series = seriesClient.getAllSeries(
            KomgaSeriesQuery(searchTerm = query),
            KomgaPageRequest(size = 10)
        ).content

        val books = bookClient.getAllBooks(
            KomgaBookQuery(searchTerm = query),
            KomgaPageRequest(size = 10)
        ).content

        if (series.isEmpty() && books.isEmpty()) {
            searchState.value = SearchState.Empty
        } else {
            searchState.value = SearchState.Finished(SearchResults(series, books))
        }
    }

    fun onQueryChange(newQuery: String) {
        currentQuery = newQuery
    }

    fun getLibraryById(id: KomgaLibraryId): KomgaLibrary? {
        return libraries.value.firstOrNull { it.id == id }
    }

    fun searchState(): StateFlow<SearchState> = searchState

}

sealed interface SearchState {
    data object Empty : SearchState
    data object Loading : SearchState
    data class Finished(val searchResults: SearchResults) : SearchState
}

data class SearchResults(
    val series: List<KomgaSeries>,
    val books: List<KomgaBook>
)
