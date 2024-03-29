package io.github.snd_r.komelia.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen

class SearchScreen(
    private val initialQuery: String?
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val searchVM = rememberScreenModel(initialQuery) { viewModelFactory.getSearchViewModel(initialQuery) }
        val navigator = LocalNavigator.currentOrThrow

        val searchState = searchVM.searchState().collectAsState()
        SearchContent(
            query = searchVM.currentQuery,
            searchState = searchState.value,
            onSeriesClick = { navigator.replaceAll(SeriesScreen(it)) },
            onBookClick = { navigator.replaceAll(BookScreen(it)) },
            onBackClick = { navigator.pop() }
        )

        BackPressHandler { navigator.pop() }
    }
}