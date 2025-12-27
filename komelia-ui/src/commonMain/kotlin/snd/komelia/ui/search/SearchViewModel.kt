package snd.komelia.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LoadState
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesSearch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val seriesApi: KomgaSeriesApi,
    private val bookApi: KomgaBookApi,
    private val appNotifications: AppNotifications,
    private val libraries: StateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var seriesResults by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var seriesCurrentPage by mutableStateOf(1)
        private set
    var seriesTotalPages by mutableStateOf(1)
        private set

    var bookResults by mutableStateOf<List<KomeliaBook>>(emptyList())
        private set
    var bookCurrentPage by mutableStateOf(1)
        private set
    var bookTotalPages by mutableStateOf(1)
        private set

    var query by mutableStateOf("")

    private var userSelectedTab by mutableStateOf(SearchResultsTab.SERIES)
    var currentTab by mutableStateOf(SearchResultsTab.SERIES)
        private set

    suspend fun initialize(initialQuery: String?) {
        if (state.value != LoadState.Uninitialized && initialQuery == query) return
        mutableState.value = LoadState.Loading
        initialQuery?.let { query = it }
        loadSearchResults()

        snapshotFlow { query }
            .drop(if (initialQuery != null) 1 else 0)
            .debounce {
                if (it.isBlank()) 0
                else 500
            }
            .distinctUntilChanged()
            .onEach { loadSearchResults() }
            .launchIn(screenModelScope)
        mutableState.value = LoadState.Success(Unit)
    }

    fun reload() {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            loadSearchResults()
            mutableState.value = LoadState.Success(Unit)
        }
    }

    private suspend fun loadSearchResults() {
        currentTab = userSelectedTab
        loadSeriesPage(1)
        loadBooksPage(1)
        if (seriesResults.isEmpty() && bookResults.isNotEmpty() && currentTab == SearchResultsTab.SERIES) {
            currentTab = SearchResultsTab.BOOKS
        }
    }

    fun onSeriesPageChange(pageNumber: Int) {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            loadSeriesPage(pageNumber)
            mutableState.value = LoadState.Success(Unit)
        }
    }

    private suspend fun loadSeriesPage(pageNumber: Int) {
        appNotifications.runCatchingToNotifications {
            val page = seriesApi.getSeriesList(
                KomgaSeriesSearch(fullTextSearch = query),
                KomgaPageRequest(
                    pageIndex = pageNumber - 1,
                    size = 10,
                    sort = if (query.isBlank()) KomgaSort.KomgaSeriesSort.byLastModifiedDateDesc() else KomgaSort.Unsorted
                )
            )

            seriesCurrentPage = page.number + 1
            seriesTotalPages = page.totalPages
            seriesResults = page.content
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun onBookPageChange(pageNumber: Int) {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            loadBooksPage(pageNumber)
            mutableState.value = LoadState.Success(Unit)
        }
    }

    private suspend fun loadBooksPage(pageNumber: Int) {
        appNotifications.runCatchingToNotifications {
            val page = bookApi.getBookList(
                KomgaBookSearch(fullTextSearch = query),
                KomgaPageRequest(
                    pageIndex = pageNumber - 1,
                    size = 10,
                    sort = if (query.isBlank()) KomgaSort.KomgaBooksSort.byLastModifiedDateDesc() else KomgaSort.Unsorted
                )
            )

            bookCurrentPage = page.number + 1
            bookTotalPages = page.totalPages
            bookResults = page.content
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun onSearchTypeChange(type: SearchResultsTab) {
        this.currentTab = type
        this.userSelectedTab = type
    }

    enum class SearchResultsTab {
        SERIES,
        BOOKS,
    }
}

data class SearchResults(
    val series: List<KomgaSeries>,
    val books: List<KomeliaBook>
)
