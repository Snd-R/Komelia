package io.github.snd_r.komelia.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalReloadEvents
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.ReloadableScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.ScreenPullToRefreshBox
import io.github.snd_r.komelia.ui.reader.image.readerScreen
import io.github.snd_r.komelia.ui.series.seriesScreen
import snd.komga.client.library.KomgaLibraryId

class HomeScreen(private val libraryId: KomgaLibraryId? = null) : ReloadableScreen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(libraryId?.value) { viewModelFactory.getHomeViewModel() }
        val navigator = LocalNavigator.currentOrThrow
        val reloadEvents = LocalReloadEvents.current

        LaunchedEffect(Unit) {
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
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
                            navigator.parent?.push(readerScreen(book, markProgress))
                        },
                    )
                }
            }
        }
    }
}
