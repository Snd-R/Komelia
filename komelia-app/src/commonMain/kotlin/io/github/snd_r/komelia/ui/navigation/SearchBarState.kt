package io.github.snd_r.komelia.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.search.SearchResults
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchBarState(
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>
) : ScreenModel {

    private var currentQuery by mutableStateOf("")

    var series by mutableStateOf<List<KomgaSeries>>(emptyList())
    var books by mutableStateOf<List<KomgaBook>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        screenModelScope.launch {
            snapshotFlow { currentQuery }
                .debounce {
                    if (it.isBlank()) 0
                    else 500
                }
                .distinctUntilChanged()
                .collect { handleQuery(it) }
        }
    }

    private suspend fun handleQuery(query: String) {
        appNotifications.runCatchingToNotifications {
            isLoading = true

            if (query.isBlank()) {
                series = emptyList()
                books = emptyList()
            } else {
                series = seriesClient.getAllSeries(
                    KomgaSeriesQuery(searchTerm = query),
                    KomgaPageRequest(size = 10)
                ).content

                books = bookClient.getAllBooks(
                    KomgaBookQuery(searchTerm = query),
                    KomgaPageRequest(size = 10)
                ).content
            }

            isLoading = false
        }.onFailure { isLoading = false }
    }

    fun currentQuery(): String = currentQuery

    fun onQueryChange(newQuery: String) {
        currentQuery = newQuery
    }

    fun getLibraryById(id: KomgaLibraryId): KomgaLibrary? {
        return libraries.value.firstOrNull { it.id == id }
    }

    fun searchResults() = SearchResults(series, books)
}

