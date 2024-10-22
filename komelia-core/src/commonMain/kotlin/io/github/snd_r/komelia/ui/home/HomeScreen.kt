package io.github.snd_r.komelia.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.reader.readerScreen
import io.github.snd_r.komelia.ui.series.seriesScreen
import snd.komga.client.library.KomgaLibraryId

class HomeScreen(private val libraryId: KomgaLibraryId? = null) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(libraryId?.value) { viewModelFactory.getHomeViewModel(null) }
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(Unit) { vm.initialize() }
        when (val state = vm.state.collectAsState().value) {
            is LoadState.Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = vm::reload
            )

            else -> {

                HomeContent(
                    keepReadingBooks = vm.keepReadingBooks,
                    onDeckBooks = vm.onDeckBooks,
                    recentlyReleasedBooks = vm.recentlyReleasedBooks,
                    recentlyAddedBooks = vm.recentlyAddedBooks,
                    recentlyReadBooks = vm.recentlyReadBooks,
                    recentlyAddedSeries = vm.recentlyAddedSeries,
                    recentlyUpdatedSeries = vm.recentlyUpdatedSeries,
                    currentFilter = vm.activeFilter,
                    onFilterChange = vm::onFilterChange,

                    cardWidth = vm.cardWidth.collectAsState().value,
                    onSeriesClick = { navigator push seriesScreen(it) },
                    seriesMenuActions = vm.seriesMenuActions(),
                    bookMenuActions = vm.bookMenuActions(),
                    onBookClick = { navigator push bookScreen(it) },
                    onBookReadClick = { book, markProgress ->
                        navigator.parent?.replace(readerScreen(book, markProgress))
                    },
                )
            }
        }
    }
}
