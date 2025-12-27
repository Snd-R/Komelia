package snd.komelia.ui.series.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.model.BooksLayout
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.collection.SeriesCollectionsContent
import snd.komelia.ui.collection.SeriesCollectionsState
import snd.komelia.ui.common.TagList
import snd.komelia.ui.common.components.AppFilterChipDefaults
import snd.komelia.ui.common.components.DescriptionChips
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LabeledEntry.Companion.stringEntry
import snd.komelia.ui.common.images.SeriesThumbnail
import snd.komelia.ui.common.menus.SeriesActionsMenu
import snd.komelia.ui.common.menus.SeriesMenuActions
import snd.komelia.ui.common.menus.bulk.BooksBulkActionsContent
import snd.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.permissions.DownloadNotificationRequestDialog
import snd.komelia.ui.dialogs.series.edit.SeriesEditDialog
import snd.komelia.ui.library.SeriesScreenFilter
import snd.komelia.ui.platform.VerticalScrollbarWithFullSpans
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.series.SeriesBooksState
import snd.komelia.ui.series.SeriesBooksState.BooksData
import snd.komelia.ui.series.SeriesViewModel.SeriesTab
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.series.KomgaSeries
import kotlin.math.max

@Composable
fun SeriesContent(
    series: KomgaSeries?,
    library: KomgaLibrary?,
    onLibraryClick: (KomgaLibrary) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    currentTab: SeriesTab,
    onTabChange: (SeriesTab) -> Unit,

    booksState: SeriesBooksState,
    onBookClick: (KomeliaBook) -> Unit,
    onBookReadClick: (KomeliaBook, Boolean) -> Unit,

    collectionsState: SeriesCollectionsState,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,

    onDownload: () -> Unit,
) {
    val windowWidth = LocalWindowWidth.current
    val contentPadding = when (windowWidth) {
        COMPACT, MEDIUM -> Modifier.padding(5.dp)
        EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
        FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
    }
    val gridMinWidth = booksState.cardWidth.collectAsState().value
    val width = LocalWindowWidth.current
    val booksLoadState = booksState.state.collectAsState().value
    val bookMenuActions = remember { booksState.bookMenuActions() }

    val booksData = remember(booksLoadState) {
        if (booksLoadState is LoadState.Success<BooksData>) booksLoadState.value
        else BooksData()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (booksData.selectionMode) {
            BooksBulkActionsToolbar(
                onCancel = { booksState.setSelectionMode(false) },
                books = booksData.books,
                actions = booksState.bookBulkMenuActions(),
                selectedBooks = booksData.selectedBooks,
                onBookSelect = booksState::onBookSelect
            )
        } else SeriesToolBar(
            series = series,
            seriesMenuActions = seriesMenuActions,
            onDownload = onDownload,
        )

        val scrollState = rememberLazyGridState()

        Box {
            LazyVerticalGrid(
                state = scrollState,
                columns = GridCells.Adaptive(gridMinWidth),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = contentPadding,
            ) {

                if (series != null && library != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Series(
                            series = series,
                            library = library,
                            onLibraryClick = onLibraryClick,
                            onFilterClick = onFilterClick,
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TabRow(
                            currentTab = currentTab,
                            onTabChange = onTabChange,
                            showCollectionsTab = collectionsState.collections.isNotEmpty() && !booksData.selectionMode
                        )
                    }

                    when (currentTab) {
                        SeriesTab.BOOKS -> SeriesBooksContent(
                            series = series,
                            onBookClick = onBookClick,
                            onBookReadClick = onBookReadClick,
                            scrollState = scrollState,
                            booksLoadState = booksLoadState,
                            onBooksLayoutChange = booksState::onBookLayoutChange,
                            onBooksPageSizeChange = booksState::onBookPageSizeChange,
                            onPageChange = booksState::onPageChange,
                            onBookSelect = booksState::onBookSelect,
                            booksFilterState = booksState.filterState,
                            bookContextMenuActions = bookMenuActions,
                        )

                        SeriesTab.COLLECTIONS -> item(span = { GridItemSpan(maxLineSpan) }) {
                            SeriesCollectionsContent(
                                collections = collectionsState.collections,
                                onCollectionClick = onCollectionClick,
                                onSeriesClick = onSeriesClick,
                                cardWidth = collectionsState.cardWidth.collectAsState().value
                            )
                        }
                    }
                }

            }

            val gridFullSpansCount = remember(currentTab, booksLoadState) {
                if (currentTab == SeriesTab.COLLECTIONS) return@remember 3

                val fullSpans = when (booksLoadState) {
                    is LoadState.Success<BooksData> -> if (booksLoadState.value.totalPages > 1) 4 else 3
                    else -> 2
                }
                when (booksData.layout) {
                    BooksLayout.GRID -> fullSpans
                    BooksLayout.LIST -> fullSpans + booksData.books.size
                }
            }
            VerticalScrollbarWithFullSpans(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd),
                fullSpanLines = gridFullSpansCount,
            )
        }
    }

    if (currentTab == SeriesTab.BOOKS &&
        (width == COMPACT || width == MEDIUM) && booksData.selectedBooks.isNotEmpty()
    ) {
        BottomPopupBulkActionsPanel {
            BooksBulkActionsContent(
                books = booksData.selectedBooks,
                actions = booksState.bookBulkMenuActions(),
                compact = true
            )
        }
    }
}

@Composable
fun SeriesToolBar(
    series: KomgaSeries?,
    seriesMenuActions: SeriesMenuActions,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (series != null) {
            Text(
                series.metadata.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, false)
            )

            Box {
                var expandActions by remember { mutableStateOf(false) }
                IconButton(onClick = { expandActions = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = null)
                }

                SeriesActionsMenu(
                    series = series,
                    actions = seriesMenuActions,
                    expanded = expandActions,
                    showEditOption = false,
                    showDownloadOption = false,
                    onDismissRequest = { expandActions = false },
                )
            }

            val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
            val isOffline = LocalOfflineMode.current.collectAsState().value
            var showEditDialog by remember { mutableStateOf(false) }
            if (isAdmin && !isOffline) {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                }
            }
            var showDownloadConfirmationDialog by remember { mutableStateOf(false) }
            if (!isOffline) {
                IconButton(
                    onClick = { showDownloadConfirmationDialog = true },
                ) {
                    Icon(Icons.Default.Download, null)
                }
            }
            if (showDownloadConfirmationDialog) {
                var permissionRequested by remember { mutableStateOf(false) }
                DownloadNotificationRequestDialog { permissionRequested = true }

                if (permissionRequested) {
                    ConfirmationDialog(
                        "Download series \"${series.metadata.title}\"?",
                        onDialogConfirm = onDownload,
                        onDialogDismiss = { showDownloadConfirmationDialog = false }
                    )
                }
            }

            if (showEditDialog)
                SeriesEditDialog(series = series, onDismissRequest = { showEditDialog = false })

        }
    }
}

@Composable
fun Series(
    series: KomgaSeries,
    library: KomgaLibrary,
    onLibraryClick: (KomgaLibrary) -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
) {
    val width = LocalWindowWidth.current
    val animation: FiniteAnimationSpec<IntSize> = remember(series) {
        when (width) {
            COMPACT, MEDIUM -> spring(stiffness = Spring.StiffnessHigh)
            else -> spring(stiffness = Spring.StiffnessMediumLow)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Layout(
            content = {
                SeriesThumbnail(
                    seriesId = series.id,
                    modifier = Modifier
                        .animateContentSize(animationSpec = animation)
                        .heightIn(min = 100.dp, max = 400.dp)
                        .widthIn(min = 300.dp, max = 500.dp),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    SeriesDescriptionRow(
                        library = library,
                        onLibraryClick = onLibraryClick,
                        releaseDate = series.booksMetadata.releaseDate,
                        status = series.metadata.status,
                        ageRating = series.metadata.ageRating,
                        language = series.metadata.language,
                        readingDirection = series.metadata.readingDirection,
                        deleted = series.deleted || library.unavailable,
                        alternateTitles = series.metadata.alternateTitles,
                        onFilterClick = onFilterClick,
                        modifier = Modifier,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    SeriesSummary(
                        seriesSummary = series.metadata.summary,
                        bookSummary = series.booksMetadata.summary,
                        bookSummaryNumber = series.booksMetadata.summaryNumber,
                    )
                }

            }
        ) { measurables, constraints ->
            val spacing = 15.dp.roundToPx()
            val infoMinWidth = 350.dp.toPx().toInt()

            val thumbnail = measurables[0].measure(constraints)
            val availableWidth = (constraints.maxWidth - thumbnail.width).coerceAtMost(1200.dp.toPx().toInt())
            val isRow = availableWidth > infoMinWidth + spacing

            val infoConstraints = if (isRow) {
                constraints.copy(
                    minWidth = infoMinWidth.dp.toPx().toInt().coerceAtMost(availableWidth),
                    maxWidth = availableWidth
                )
            } else constraints

            val info = measurables[1].measure(infoConstraints)

            val (totalWidth, totalHeight) = if (isRow) {
                thumbnail.width + info.width + spacing to max(thumbnail.height, info.height)
            } else {
                max(thumbnail.width, info.width) to thumbnail.height + info.height + spacing

            }
            layout(totalWidth, totalHeight) {
                thumbnail.placeRelative(0, 0)
                if (isRow) {
                    info.placeRelative(thumbnail.width + spacing, 0)
                } else {
                    info.placeRelative(0, thumbnail.height + spacing)
                }
            }
        }

        SeriesChipTags(series, onFilterClick)
    }
}

@Composable
fun SeriesChipTags(
    series: KomgaSeries,
    onFilterClick: (SeriesScreenFilter) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (series.metadata.publisher.isNotBlank()) {
            DescriptionChips(
                label = "Publisher",
                chipValue = stringEntry(series.metadata.publisher),
                onClick = { onFilterClick(SeriesScreenFilter(publisher = listOf(it))) },
            )
        }

        DescriptionChips(
            label = "Genres",
            chipValues = series.metadata.genres.map { stringEntry(it) },
            onChipClick = { onFilterClick(SeriesScreenFilter(genres = listOf(it))) },
        )

        TagList(
            tags = series.metadata.tags,
            secondaryTags = series.booksMetadata.tags,
            onTagClick = { onFilterClick(SeriesScreenFilter(tags = listOf(it))) },
        )

        val uriHandler = LocalUriHandler.current
        DescriptionChips(
            label = "Links",
            chipValues = series.metadata.links.map { LabeledEntry(it, it.label) },
            onChipClick = { entry -> uriHandler.openUri(entry.url) },
            icon = Icons.Default.Link,
        )

        Spacer(Modifier.height(2.dp))

        series.booksMetadata.authors
            .filter { it.role == "writer" }
            .groupBy { it.role }
            .forEach { (_, author) ->
                DescriptionChips(
                    label = "Writers",
                    chipValues = author.map { LabeledEntry(it, it.name) },
                    onChipClick = { onFilterClick(SeriesScreenFilter(authors = listOf(it))) },
                    modifier = Modifier.cursorForHand()
                )
            }

        series.booksMetadata.authors
            .filter { it.role == "penciller" }
            .groupBy { it.role }
            .forEach { (_, author) ->
                DescriptionChips(
                    label = "Pencillers",
                    chipValues = author.map { LabeledEntry(it, it.name) },
                    onChipClick = { onFilterClick(SeriesScreenFilter(authors = listOf(it))) },
                    modifier = Modifier.cursorForHand()
                )
            }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun TabRow(
    currentTab: SeriesTab,
    onTabChange: (SeriesTab) -> Unit,
    showCollectionsTab: Boolean,
) {
    val chipColors = AppFilterChipDefaults.filterChipColors()
    Column {
        AnimatedVisibility(showCollectionsTab) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.clickable { onTabChange(SeriesTab.BOOKS) }) {

                }
                FilterChip(
                    onClick = { onTabChange(SeriesTab.BOOKS) },
                    selected = currentTab == SeriesTab.BOOKS,
                    label = { Text("Books") },
                    colors = chipColors,
                    border = null,
                )
                FilterChip(
                    onClick = { onTabChange(SeriesTab.COLLECTIONS) },
                    selected = currentTab == SeriesTab.COLLECTIONS,
                    label = { Text("Collections") },
                    colors = chipColors,
                    border = null,
                )
            }
        }
        HorizontalDivider()
    }
}