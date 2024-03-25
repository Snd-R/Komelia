package io.github.snd_r.komelia.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.library.DashboardScreen
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.navigation.AppBar
import io.github.snd_r.komelia.ui.navigation.RegularNavBar
import io.github.snd_r.komelia.ui.search.SearchScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komelia.ui.settings.SettingsScreen

class MainScreen(
    private val defaultScreen: Screen = DashboardScreen()
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current

        Navigator(defaultScreen) { navigator ->
            val vm = rememberScreenModel { viewModelFactory.getNavigationViewModel(navigator) }
            Column {
                AppBar(
                    onMenuButtonPress = { vm.toggleNavBar() },
                    query = vm.searchBarState.currentQuery(),
                    onQueryChange = vm.searchBarState::onQueryChange,
                    isLoading = vm.searchBarState.isLoading,
                    onSearchAllClick = { navigator.push(SearchScreen(it)) },
                    searchResults = vm.searchBarState.searchResults(),
                    libraryById = vm.searchBarState::getLibraryById,
                    onBookClick = { navigator.replaceAll(BookScreen(it)) },
                    onSeriesClick = { navigator.replaceAll(SeriesScreen(it)) },
                )
                Row {
                    RegularNavBar(
                        currentScreen = navigator.lastItem,
                        isOpen = vm.isNavBarOpen,
                        libraries = vm.libraries.collectAsState().value,
                        libraryActions = vm.getLibraryActions(),
                        onHomeClick = { navigator.replaceAll(DashboardScreen()) },
                        onLibrariesClick = { navigator.replaceAll(LibraryScreen()) },

                        onLibraryClick = { navigator.replaceAll(LibraryScreen(it)) },
                        onSettingsClick = { navigator.parent!!.push(SettingsScreen()) },
                        taskQueueStatus = vm.komgaTaskQueueStatus.collectAsState().value
                    )
                    CurrentScreen()
                }
            }
        }
    }
}