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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.AppFilterChipDefaults
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
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
) : Screen {

    override val key: ScreenKey = libraryId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(libraryId?.value) { viewModelFactory.getLibraryViewModel(libraryId) }

        LaunchedEffect(libraryId) { vm.initialize(seriesFilter) }

        when (val state = vm.state.collectAsState().value) {
            is Error -> ErrorContent(message = state.exception.message ?: "Unknown Error", onReload = vm::reload)
            Uninitialized, Loading, is Success -> {
                var showToolbar by remember { mutableStateOf(true) }
                Column {
                    if (showToolbar) {
                        LibraryToolBar(
                            library = vm.library?.value,
                            currentTab = vm.currentTab,
                            libraryActions = vm.libraryActions(),
                            collectionsCount = vm.collectionsCount,
                            readListsCount = vm.readListsCount,
                            onBrowseClick = vm::toBrowseTab,
                            onCollectionsClick = vm::toCollectionsTab,
                            onReadListsClick = vm::toReadListsTab
                        )
                    }
                    CurrentTab(
                        tab = vm.currentTab,
                        onLibraryToolbarToggle = { showToolbar = it }
                    )
                }
            }
        }
    }

    @Composable
    private fun CurrentTab(
        tab: LibraryTab,
        onLibraryToolbarToggle: (show: Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(modifier) {
            when (tab) {
                SERIES -> BrowseTab(onLibraryToolbarToggle)
                COLLECTIONS -> CollectionsTab()
                READ_LISTS -> ReadListsTab()
            }
        }

    }

    @Composable
    private fun BrowseTab(
        onLibraryToolbarToggle: (show: Boolean) -> Unit,
    ) {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel("browse_${libraryId?.value}") {
            viewModelFactory.getSeriesBrowseViewModel(libraryId)
        }
        LaunchedEffect(libraryId) { vm.initialize(seriesFilter) }

        LaunchedEffect(Unit) {
            snapshotFlow { vm.isInEditMode }
                .collect { editMode -> onLibraryToolbarToggle(!editMode) }
        }

        when (val state = vm.state.collectAsState().value) {
            is Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = vm::reload
            )

            else -> {
                val loading = state is Loading || state is Uninitialized
                SeriesListContent(
                    series = vm.series,
                    seriesActions = vm.seriesMenuActions(),
                    seriesTotalCount = vm.totalSeriesCount,
                    onSeriesClick = { navigator.push(seriesScreen(it)) },

                    editMode = vm.isInEditMode,
                    onEditModeChange = vm::onEditModeChange,
                    selectedSeries = vm.selectedSeries,
                    onSeriesSelect = vm::onSeriesSelect,

                    isLoading = loading,
                    filterState = vm.filterState,

                    currentPage = vm.currentSeriesPage,
                    totalPages = vm.totalSeriesPages,
                    pageSize = vm.pageLoadSize.collectAsState().value,
                    onPageSizeChange = vm::onPageSizeChange,
                    onPageChange = vm::onPageChange,

                    minSize = vm.cardWidth.collectAsState().value,
                )
            }
        }
    }

    @Composable
    private fun CollectionsTab() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel("collections_${libraryId?.value}") {
            viewModelFactory.getLibraryCollectionsViewModel(libraryId)
        }

        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(libraryId) { vm.initialize() }

        when (val state = vm.state.collectAsState().value) {

            Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = vm::reload
            )

            else -> {
                val loading = state is Loading
                LibraryCollectionsContent(
                    collections = vm.collections,
                    collectionsTotalCount = vm.totalCollections,
                    onCollectionClick = { navigator push CollectionScreen(it) },
                    onCollectionDelete = vm::onCollectionDelete,
                    isLoading = loading,

                    totalPages = vm.totalPages,
                    currentPage = vm.currentPage,
                    pageSize = vm.pageSize,
                    onPageChange = vm::onPageChange,
                    onPageSizeChange = vm::onPageSizeChange,

                    minSize = vm.cardWidth.collectAsState().value
                )

            }
        }

    }

    @Composable
    private fun ReadListsTab() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel("readLists_${libraryId?.value}") {
            viewModelFactory.getLibraryReadListsViewModel(libraryId)
        }
        LaunchedEffect(libraryId) { vm.initialize() }
        val navigator = LocalNavigator.currentOrThrow

        when (val state = vm.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> Text("Error")
            else -> {
                val loading = state is Loading
                LibraryReadListsContent(
                    readLists = vm.readLists,
                    readListsTotalCount = vm.totalReadLists,
                    onReadListClick = { navigator push ReadListScreen(it) },
                    onReadListDelete = vm::onReadListDelete,
                    isLoading = loading,

                    totalPages = vm.totalPages,
                    currentPage = vm.currentPage,
                    pageSize = vm.pageSize,
                    onPageChange = vm::onPageChange,
                    onPageSizeChange = vm::onPageSizeChange,

                    minSize = vm.cardWidth.collectAsState().value
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