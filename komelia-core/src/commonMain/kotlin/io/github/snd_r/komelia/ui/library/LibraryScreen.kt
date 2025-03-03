package io.github.snd_r.komelia.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalReloadEvents
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.ReloadableScreen
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.AppFilterChipDefaults
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.ScreenPullToRefreshBox
import io.github.snd_r.komelia.ui.common.menus.LibraryActionsMenu
import io.github.snd_r.komelia.ui.common.menus.LibraryMenuActions
import io.github.snd_r.komelia.ui.library.LibraryTab.COLLECTIONS
import io.github.snd_r.komelia.ui.library.LibraryTab.READ_LISTS
import io.github.snd_r.komelia.ui.library.LibraryTab.SERIES
import io.github.snd_r.komelia.ui.library.view.LibraryCollectionsContent
import io.github.snd_r.komelia.ui.library.view.LibraryReadListsContent
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.series.list.SeriesListContent
import io.github.snd_r.komelia.ui.series.seriesScreen
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesStatus
import kotlin.jvm.Transient

class LibraryScreen(
    val libraryId: KomgaLibraryId? = null,
    @Transient
    private val seriesFilter: SeriesScreenFilter? = null
) : ReloadableScreen {

    override val key: ScreenKey = "${libraryId}_${seriesFilter.hashCode()}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(libraryId?.value) { viewModelFactory.getLibraryViewModel(libraryId) }
        val reloadEvents = LocalReloadEvents.current

        LaunchedEffect(libraryId) {
            vm.initialize(seriesFilter)
            reloadEvents.collect { vm.reload() }
        }
        DisposableEffect(Unit) {
            vm.startKomgaEventHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                is Error -> ErrorContent(message = state.exception.message ?: "Unknown Error", onReload = vm::reload)
                Uninitialized, Loading, is Success -> {
                    Column {
                        if (vm.showToolbar.collectAsState().value) {
                            LibraryToolBar(
                                library = vm.library.collectAsState().value,
                                currentTab = vm.currentTab,
                                libraryActions = vm.libraryActions(),
                                collectionsCount = vm.collectionsCount,
                                readListsCount = vm.readListsCount,
                                onBrowseClick = vm::toBrowseTab,
                                onCollectionsClick = vm::toCollectionsTab,
                                onReadListsClick = vm::toReadListsTab
                            )
                        }

                        when (vm.currentTab) {
                            SERIES -> BrowseTab(vm.seriesTabState)
                            COLLECTIONS -> CollectionsTab(vm.collectionsTabState)
                            READ_LISTS -> ReadListsTab(vm.readListsTabState)
                        }
                    }
                }
            }
            BackPressHandler { navigator.pop() }
        }
    }

    @Composable
    private fun BrowseTab(seriesTabState: LibrarySeriesTabState) {
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(libraryId) { seriesTabState.initialize(seriesFilter) }
        DisposableEffect(Unit) {
            seriesTabState.startKomgaEventHandler()
            onDispose { seriesTabState.stopKomgaEventHandler() }
        }

        when (val state = seriesTabState.state.collectAsState().value) {
            is Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = seriesTabState::reload
            )

            else -> {
                val loading = state is Loading || state is Uninitialized
                SeriesListContent(
                    series = seriesTabState.series,
                    seriesActions = seriesTabState.seriesMenuActions(),
                    seriesTotalCount = seriesTabState.totalSeriesCount,
                    onSeriesClick = { navigator.push(seriesScreen(it)) },

                    editMode = seriesTabState.isInEditMode.collectAsState().value,
                    onEditModeChange = seriesTabState::onEditModeChange,
                    selectedSeries = seriesTabState.selectedSeries,
                    onSeriesSelect = seriesTabState::onSeriesSelect,

                    isLoading = loading,
                    filterState = seriesTabState.filterState,

                    currentPage = seriesTabState.currentSeriesPage,
                    totalPages = seriesTabState.totalSeriesPages,
                    pageSize = seriesTabState.pageLoadSize.collectAsState().value,
                    onPageSizeChange = seriesTabState::onPageSizeChange,
                    onPageChange = seriesTabState::onPageChange,

                    minSize = seriesTabState.cardWidth.collectAsState().value,
                )
            }
        }
    }

    @Composable
    private fun CollectionsTab(collectionsTabState: LibraryCollectionsTabState) {
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(libraryId) { collectionsTabState.initialize() }
        DisposableEffect(Unit) {
            collectionsTabState.startKomgaEventHandler()
            onDispose { collectionsTabState.stopKomgaEventHandler() }
        }

        when (val state = collectionsTabState.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = collectionsTabState::reload
            )

            else -> {
                val loading = state is Loading
                LibraryCollectionsContent(
                    collections = collectionsTabState.collections,
                    collectionsTotalCount = collectionsTabState.totalCollections,
                    onCollectionClick = { navigator push CollectionScreen(it) },
                    onCollectionDelete = collectionsTabState::onCollectionDelete,
                    isLoading = loading,

                    totalPages = collectionsTabState.totalPages,
                    currentPage = collectionsTabState.currentPage,
                    pageSize = collectionsTabState.pageSize,
                    onPageChange = collectionsTabState::onPageChange,
                    onPageSizeChange = collectionsTabState::onPageSizeChange,

                    minSize = collectionsTabState.cardWidth.collectAsState().value
                )

            }
        }

    }

    @Composable
    private fun ReadListsTab(readListTabState: LibraryReadListsTabState) {
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(libraryId) { readListTabState.initialize() }
        DisposableEffect(Unit) {
            readListTabState.startKomgaEventHandler()
            onDispose { readListTabState.stopKomgaEventHandler() }
        }

        when (val state = readListTabState.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> Text("Error")
            else -> {
                val loading = state is Loading
                LibraryReadListsContent(
                    readLists = readListTabState.readLists,
                    readListsTotalCount = readListTabState.totalReadLists,
                    onReadListClick = { navigator push ReadListScreen(it) },
                    onReadListDelete = readListTabState::onReadListDelete,
                    isLoading = loading,

                    totalPages = readListTabState.totalPages,
                    currentPage = readListTabState.currentPage,
                    pageSize = readListTabState.pageSize,
                    onPageChange = readListTabState::onPageChange,
                    onPageSizeChange = readListTabState::onPageSizeChange,

                    minSize = readListTabState.cardWidth.collectAsState().value
                )
            }
        }
    }

}

@Composable
fun LibraryToolBar(
    library: KomgaLibrary?,
    currentTab: LibraryTab,
    libraryActions: LibraryMenuActions,
    collectionsCount: Int,
    readListsCount: Int,
    onBrowseClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onReadListsClick: () -> Unit,
) {

    val chipColors = AppFilterChipDefaults.filterChipColors()
    var showOptionsMenu by remember { mutableStateOf(false) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            if (library != null) {
                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true }
                    ) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = null,
                        )
                    }

                    LibraryActionsMenu(
                        library = library,
                        actions = libraryActions,
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    )
                }
            }
            Text(library?.let { library.name } ?: "All Libraries")

            Spacer(Modifier.width(5.dp))
        }


        if (collectionsCount > 0 || readListsCount > 0)
            item {
                FilterChip(
                    onClick = onBrowseClick,
                    selected = currentTab == SERIES,
                    label = { Text("Series") },
                    colors = chipColors,
                    border = null,
                )
            }

        if (collectionsCount > 0)
            item {
                FilterChip(
                    onClick = onCollectionsClick,
                    selected = currentTab == COLLECTIONS,
                    label = { Text("Collections") },
                    colors = chipColors,
                    border = null,
                )
            }

        if (readListsCount > 0)
            item {
                FilterChip(
                    onClick = onReadListsClick,
                    selected = currentTab == READ_LISTS,
                    label = { Text("Read Lists") },
                    colors = chipColors,
                    border = null,
                )
            }

    }
}

@Composable
fun CompactLibraryToolBar(
    library: KomgaLibrary?,
    libraryActions: LibraryMenuActions,
) {

    Row {
        var showOptionsMenu by remember { mutableStateOf(false) }
        if (library != null) {
            Box {
                IconButton(
                    onClick = { showOptionsMenu = true }
                ) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                }

                LibraryActionsMenu(
                    library = library,
                    actions = libraryActions,
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                )
            }
        }
        val title = library?.let { library.name } ?: "All Libraries"
        Text(title, Modifier.align(Alignment.CenterVertically))


    }
}

@Composable
fun CompactLibraryNavigation(
    currentTab: LibraryTab,
    collectionsCount: Int,
    readListsCount: Int,
//    onRecommendedClick: () -> Unit,
    onBrowseClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onReadListsClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
//        CompactNavButton(
//            text = "Recommended",
//            icon = Icons.Default.Star,
//            onClick = onRecommendedClick,
//            isSelected = currentTab == RECOMMENDED,
//            modifier = Modifier.weight(1f)
//        )
        CompactNavButton(
            text = "Series",
            icon = Icons.Default.LocalLibrary,
            onClick = onBrowseClick,
            isSelected = currentTab == SERIES,
            modifier = Modifier.weight(1f)
        )
        if (collectionsCount > 0)
            CompactNavButton(
                text = "Collections",
                icon = Icons.AutoMirrored.Filled.List,
                onClick = onCollectionsClick,
                isSelected = currentTab == COLLECTIONS,
                modifier = Modifier.weight(1f)
            )
        if (readListsCount > 0)
            CompactNavButton(
                text = "Read Lists",
                icon = Icons.Default.Collections,
                onClick = onReadListsClick,
                isSelected = currentTab == READ_LISTS,
                modifier = Modifier.weight(1f)
            )
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
        color = MaterialTheme.colorScheme.surfaceVariant,
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

data class SeriesScreenFilter(
    val publicationStatus: List<KomgaSeriesStatus>? = null,
    val ageRating: List<Int>? = null,
    val language: List<String>? = null,
    val publisher: List<String>? = null,
    val genres: List<String>? = null,
    val tags: List<String>? = null,
    val authors: List<KomgaAuthor>? = null,
)