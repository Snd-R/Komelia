package io.github.snd_r.komelia.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookQuery
import snd.komga.client.book.KomgaBooksSort
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesQuery
import snd.komga.client.series.KomgaSeriesSort

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>,
    initialQuery: String?
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var seriesResults by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var seriesCurrentPage by mutableStateOf(1)
        private set
    var seriesTotalPages by mutableStateOf(1)
        private set

    var bookResults by mutableStateOf<List<KomgaBook>>(emptyList())
        private set
    var bookCurrentPage by mutableStateOf(1)
        private set
    var bookTotalPages by mutableStateOf(1)
        private set

    var query by mutableStateOf(initialQuery ?: "")

    //    private val searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    var searchType by mutableStateOf(SearchType.SERIES)
        private set

    init {
//        loadSeries(1)
        load()

        snapshotFlow { query }
            .drop(if (initialQuery != null) 1 else 0)
            .debounce {
                if (it.isBlank()) 0
                else 500
            }
            .distinctUntilChanged()
            .onEach { load() }
            .launchIn(screenModelScope)
    }


    fun load() {
        when (searchType) {
            SearchType.SERIES -> loadSeries(1)
            SearchType.BOOKS -> loadBooks(1)
        }
    }

    fun loadSeries(pageNumber: Int) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications {

                mutableState.value = LoadState.Loading
                val page = seriesClient.getAllSeries(
                    KomgaSeriesQuery(searchTerm = query),
                    KomgaPageRequest(
                        pageIndex = pageNumber - 1,
                        size = 10,
                        sort = if (query.isBlank()) KomgaSeriesSort.byLastModifiedDateDesc() else KomgaSort.UNSORTED
                    )
                )

                seriesCurrentPage = page.number + 1
                seriesTotalPages = page.totalPages
                seriesResults = page.content
                mutableState.value = LoadState.Success(Unit)

            }.onFailure { mutableState.value = LoadState.Error(it) }
        }
    }

    fun loadBooks(pageNumber: Int) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications {

                mutableState.value = LoadState.Loading
                val page = bookClient.getAllBooks(
                    KomgaBookQuery(searchTerm = query),
                    KomgaPageRequest(
                        pageIndex = pageNumber - 1,
                        size = 10,
                        sort = if (query.isBlank()) KomgaBooksSort.byLastModifiedDateDesc() else KomgaSort.UNSORTED
                    )
                )

                bookCurrentPage = page.number + 1
                bookTotalPages = page.totalPages
                bookResults = page.content
                mutableState.value = LoadState.Success(Unit)

            }.onFailure { mutableState.value = LoadState.Error(it) }
        }
    }

//    private suspend fun handle(query: String) {
//        if (query.isBlank()) {
//            searchState.value = SearchState.Empty
//            return
//        }
//
//        searchState.value = SearchState.Loading
//
//        val series = seriesClient.getAllSeries(
//            KomgaSeriesQuery(searchTerm = query),
//            KomgaPageRequest(size = 10)
//        ).content
//
//        val books = bookClient.getAllBooks(
//            KomgaBookQuery(searchTerm = query),
//            KomgaPageRequest(size = 10)
//        ).content
//
//        if (series.isEmpty() && books.isEmpty()) {
//            searchState.value = SearchState.Empty
//        } else {
//            searchState.value = SearchState.Finished(SearchResults(series, books))
//        }
//    }

//    fun onQueryChange(newQuery: String) {
//        currentQuery = newQuery
//    }

    fun getLibraryById(id: KomgaLibraryId): KomgaLibrary? {
        return libraries.value.firstOrNull { it.id == id }
    }

//    fun searchState(): StateFlow<SearchState> = searchState

    fun onSearchTypeChange(type: SearchType) {
        this.searchType = type
        load()
    }

    fun onQueryChange() {

    }

    enum class SearchType {
        SERIES,
        BOOKS,
    }
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
