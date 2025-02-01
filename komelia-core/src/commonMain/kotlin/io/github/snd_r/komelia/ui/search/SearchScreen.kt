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
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.ScreenPullToRefreshBox
import io.github.snd_r.komelia.ui.series.seriesScreen

class SearchScreen(
    private val initialQuery: String?,
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(initialQuery) {
            viewModelFactory.getSearchViewModel()
        }
        LaunchedEffect(initialQuery) { vm.initialize(initialQuery) }

        val navigator = LocalNavigator.currentOrThrow

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (LocalPlatform.current == PlatformType.MOBILE)
                    SearchField(vm)

                when (val state = vm.state.collectAsState().value) {
                    is LoadState.Error -> ErrorContent(
                        state.exception.message ?: "Error",
                        onReload = vm::reload
                    )

                    LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                    is LoadState.Success -> {

                        SearchContent(
                            query = vm.query,
                            searchType = vm.currentTab,
                            onSearchTypeChange = vm::onSearchTypeChange,

                            seriesResults = vm.seriesResults,
                            seriesCurrentPage = vm.seriesCurrentPage,
                            seriesTotalPages = vm.seriesTotalPages,
                            onSeriesPageChange = vm::onSeriesPageChange,
                            onSeriesClick = { navigator.push(seriesScreen(it)) },

                            bookResults = vm.bookResults,
                            bookCurrentPage = vm.bookCurrentPage,
                            bookTotalPages = vm.bookTotalPages,
                            onBookPageChange = vm::onBookPageChange,
                            onBookClick = { navigator.push(bookScreen(it)) },
                        )

                    }
                }
            }
            BackPressHandler { navigator.pop() }
        }
    }

    @Composable
    private fun SearchField(vm: SearchViewModel) {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

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