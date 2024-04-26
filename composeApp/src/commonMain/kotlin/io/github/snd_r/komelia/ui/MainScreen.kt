package io.github.snd_r.komelia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.DrawerValue.Open
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.home.HomeScreen
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.navigation.AppBar
import io.github.snd_r.komelia.ui.navigation.NavBarContent
import io.github.snd_r.komelia.ui.search.SearchScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komelia.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

class MainScreen(
    private val defaultScreen: Screen = HomeScreen()
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val width = LocalWindowWidth.current

        Navigator(
            screen = defaultScreen,
            onBackPressed = null,
        ) { navigator ->
            val vm = rememberScreenModel { viewModelFactory.getNavigationViewModel(navigator) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(width) {
                when (width) {
                    FULL -> vm.navBarState.snapTo(Open)
                    else -> vm.navBarState.snapTo(Closed)
                }
            }

            Column {
                if (width == EXPANDED || width == FULL) {
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
                        if (vm.navBarState.targetValue == Open) NavBar(vm, navigator, width)
                        CurrentScreen()
                    }
                }

                if (width == COMPACT || width == MEDIUM) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.surface,
                        bottomBar = {
                            BottomNavigationBar(
                                navigator = navigator,
                                toggleLibrariesDrawer = { coroutineScope.launch { vm.toggleNavBar() } },
                                modifier = Modifier
                            )
                        },
                    ) { paddingValues ->
                        val layoutDirection = LocalLayoutDirection.current

                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    start = paddingValues.calculateStartPadding(layoutDirection),
                                    end = paddingValues.calculateEndPadding(layoutDirection),
                                    top = paddingValues.calculateTopPadding(),
                                    bottom = paddingValues.calculateBottomPadding(),
                                )
                        ) {
                            ModalNavigationDrawer(
                                drawerState = vm.navBarState,
                                drawerContent = { NavBar(vm, navigator, width) },
                                content = { CurrentScreen() }
                            )
                        }
                    }
                }
            }
        }

    }

    @Composable
    private fun BottomNavigationBar(
        navigator: Navigator,
        toggleLibrariesDrawer: () -> Unit,
        modifier: Modifier
    ) {
        Column {
            HorizontalDivider()
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.Center
            ) {
                CompactNavButton(
                    text = "Libraries",
                    icon = Icons.Default.LocalLibrary,
                    onClick = { toggleLibrariesDrawer() },
                    isSelected = false,
                    modifier = Modifier.weight(1f)
                )

                CompactNavButton(
                    text = "Home",
                    icon = Icons.Default.Home,
                    onClick = { navigator.replaceAll(HomeScreen()) },
                    isSelected = navigator.lastItem is HomeScreen,
                    modifier = Modifier.weight(1f)
                )


                CompactNavButton(
                    text = "Search",
                    icon = Icons.Default.Search,
                    onClick = { navigator.push(SearchScreen(null)) },
                    isSelected = navigator.lastItem is SearchScreen,
                    modifier = Modifier.weight(1f)
                )

                CompactNavButton(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = { navigator.parent!!.push(SettingsScreen()) },
                    isSelected = navigator.lastItem is SettingsScreen,
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }

    @Composable
    private fun CompactNavButton(
        text: String,
        icon: ImageVector,
        onClick: () -> Unit,
        isSelected: Boolean,
        modifier: Modifier
    ) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surface,
            contentColor =
            if (isSelected) MaterialTheme.colorScheme.secondary
            else contentColorFor(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .clickable { onClick() }
                    .cursorForHand()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, null)
                Text(text, style = MaterialTheme.typography.bodySmall)
            }
        }
    }


    @Composable
    private fun NavBar(
        vm: MainScreenViewModel,
        navigator: Navigator,
        width: WindowWidth
    ) {
        val coroutineScope = rememberCoroutineScope()
        NavBarContent(
            currentScreen = navigator.lastItem,
            libraries = vm.libraries.collectAsState().value,
            libraryActions = vm.getLibraryActions(),
            onHomeClick = {
                navigator.replaceAll(HomeScreen())
                if (width != FULL) coroutineScope.launch { vm.navBarState.snapTo(Closed) }
            },
            onLibrariesClick = {
                navigator.replaceAll(LibraryScreen())
                if (width != FULL) coroutineScope.launch { vm.navBarState.snapTo(Closed) }
            },

            onLibraryClick = {
                navigator.replaceAll(LibraryScreen(it))
                if (width != FULL) coroutineScope.launch { vm.navBarState.snapTo(Closed) }
            },
            onSettingsClick = { navigator.parent!!.push(SettingsScreen()) },
            taskQueueStatus = vm.komgaTaskQueueStatus.collectAsState().value
        )
    }
}
