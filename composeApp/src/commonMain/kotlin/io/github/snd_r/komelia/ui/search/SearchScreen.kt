package io.github.snd_r.komelia.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.series.SeriesScreen

class SearchScreen(
    private val initialQuery: String?,
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm =
            rememberScreenModel(initialQuery) { viewModelFactory.getSearchViewModel(initialQuery) }
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchField(vm)

            when (val state = vm.state.collectAsState().value) {
                is LoadState.Error -> ErrorContent(
                    state.exception.message ?: "Error",
                    onReload = vm::load
                )

                LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                is LoadState.Success -> {

                    SearchContent(
                        query = vm.query,
                        searchType = vm.searchType,
                        onSearchTypeChange = vm::onSearchTypeChange,

                        seriesResults = vm.seriesResults,
                        seriesCurrentPage = vm.seriesCurrentPage,
                        seriesTotalPages = vm.seriesTotalPages,
                        onSeriesPageChange = vm::loadSeries,
                        onSeriesClick = { navigator.replaceAll(SeriesScreen(it)) },

                        bookResults = vm.bookResults,
                        bookCurrentPage = vm.bookCurrentPage,
                        bookTotalPages = vm.bookTotalPages,
                        onBookPageChange = vm::loadBooks,
                        onBookClick = { navigator.replaceAll(BookScreen(it)) },
                    )

                }
            }
        }
        BackPressHandler { navigator.pop() }
    }

    @Composable
    private fun SearchField(vm: SearchViewModel) {
        val width = LocalWindowWidth.current

        if (width == COMPACT || width == MEDIUM) {
            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            LaunchedEffect(Unit) {

            }
            SearchTextField(
                query = vm.query,
                onQueryChange = vm::query::set,
                onDone = { focusManager.clearFocus() },
                onDismiss = { vm.query = "" },
                modifier = Modifier.focusRequester(focusRequester)
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}