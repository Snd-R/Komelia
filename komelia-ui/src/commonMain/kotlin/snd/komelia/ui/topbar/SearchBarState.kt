package snd.komelia.ui.topbar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.search.SearchResults
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesSearch

@OptIn(FlowPreview::class)
class SearchBarState(
    private val seriesApi: KomgaSeriesApi,
    private val bookApi: KomgaBookApi,
    private val appNotifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>
) : ScreenModel {

    private var currentQuery by mutableStateOf("")

    var series by mutableStateOf<List<KomgaSeries>>(emptyList())
    var books by mutableStateOf<List<KomeliaBook>>(emptyList())
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
                series = seriesApi.getSeriesList(
                    KomgaSeriesSearch(fullTextSearch = query),
                    pageRequest = KomgaPageRequest(size = 10)
                ).content

                books = bookApi.getBookList(
                    KomgaBookSearch(fullTextSearch = query),
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

