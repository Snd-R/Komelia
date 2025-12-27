package snd.komelia.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalReloadEvents
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.ReloadableScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.home.edit.FilterEditScreen
import snd.komelia.ui.platform.ScreenPullToRefreshBox
import snd.komelia.ui.reader.readerScreen
import snd.komelia.ui.series.seriesScreen
import snd.komga.client.library.KomgaLibraryId

class HomeScreen(private val libraryId: KomgaLibraryId? = null) : ReloadableScreen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val isOffline = LocalOfflineMode.current.value
        val serverUrl = LocalKomgaState.current.serverUrl.value

        val vmKey = remember(libraryId, isOffline, serverUrl) {
            buildString {
                libraryId?.let { append(it.value) }
                append(serverUrl)
                append(isOffline.toString())
            }
        }
        val vm = rememberScreenModel(vmKey) { viewModelFactory.getHomeViewModel() }
        val navigator = LocalNavigator.currentOrThrow
        val reloadEvents = LocalReloadEvents.current

        LaunchedEffect(Unit) {
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }

        DisposableEffect(Unit) {
            vm.startKomgaEventsHandler()
            onDispose { vm.stopKomgaEventsHandler() }
        }

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                is LoadState.Error -> ErrorContent(
                    message = state.exception.message ?: "Unknown Error",
                    onReload = vm::reload
                )

                else ->
                    HomeContent(
                        filters = vm.currentFilters.collectAsState().value,
                        activeFilterNumber = vm.activeFilterNumber.collectAsState().value,
                        onFilterChange = vm::onFilterChange,
                        onEditStart = { navigator.replaceAll(FilterEditScreen(vm.currentFilters.value)) },

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
