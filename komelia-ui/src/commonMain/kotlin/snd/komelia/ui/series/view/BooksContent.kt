package snd.komelia.ui.series.view

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.model.BooksLayout
import snd.komelia.settings.model.BooksLayout.GRID
import snd.komelia.settings.model.BooksLayout.LIST
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.book.BooksFilterState
import snd.komelia.ui.book.BooksFilterState.BooksSort
import snd.komelia.ui.common.cards.BookDetailedListCard
import snd.komelia.ui.common.cards.BookImageCard
import snd.komelia.ui.common.components.FilterDropdownChoice
import snd.komelia.ui.common.components.FilterDropdownMultiChoice
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.PageSizeSelectionDropdown
import snd.komelia.ui.common.components.Pagination
import snd.komelia.ui.common.components.TagFiltersDropdownMenu
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.common.menus.bulk.BookBulkActions
import snd.komelia.ui.common.menus.bulk.BooksBulkActionsContent
import snd.komelia.ui.common.menus.bulk.BulkActionsContainer
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.series.SeriesBooksState.BooksData
import snd.komelia.ui.series.SeriesFilterState.TagExclusionMode
import snd.komelia.ui.series.SeriesFilterState.TagInclusionMode
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.series.KomgaSeries

fun LazyGridScope.SeriesBooksContent(
    series: KomgaSeries?,
    booksLoadState: LoadState<BooksData>,
    onBookClick: (KomeliaBook) -> Unit,
    onBookReadClick: (KomeliaBook, Boolean) -> Unit,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    onBooksPageSizeChange: (Int) -> Unit,
    onPageChange: (Int) -> Unit,
    onBookSelect: (KomeliaBook) -> Unit,
    booksFilterState: BooksFilterState,
    bookContextMenuActions: BookMenuActions,
    scrollState: LazyGridState,
) {
    if (booksLoadState is LoadState.Success<BooksData>) {
        val booksState = booksLoadState.value
        item(span = { GridItemSpan(maxLineSpan) }) {
            BooksToolBar(
                series = series,
                booksLayout = booksState.layout,
                onBooksLayoutChange = onBooksLayoutChange,
                booksPageSize = booksState.pageSize,
                onBooksPageSizeChange = onBooksPageSizeChange,
                selectionMode = booksState.selectionMode,
                booksFilterState = booksFilterState,
                totalBookPages = booksState.totalPages,
                currentBookPage = booksState.currentPage,
                onPageChange = onPageChange
            )
        }
        BooksContent(
            books = booksState.books,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            bookMenuActions = bookContextMenuActions,
            selectionMode = booksState.selectionMode,
            selectedBooks = booksState.selectedBooks,
            onBookSelect = onBookSelect,
            layout = booksState.layout,
        )

        if (!booksState.selectionMode) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                val coroutineScope = rememberCoroutineScope()
                Pagination(
                    totalPages = booksState.totalPages,
                    currentPage = booksState.currentPage,
                    onPageChange = {
                        coroutineScope.launch {
                            scrollState.scrollToItem(scrollState.layoutInfo.totalItemsCount - (booksState.books.size + 2))
                            onPageChange(it)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    } else {
        item(span = { GridItemSpan(maxLineSpan) }) { LoadIndicator() }
    }
}

@Composable
private fun LoadIndicator() {
    val background = MaterialTheme.colorScheme.surfaceVariant
    val animatedColor = remember { Animatable(background.copy(alpha = .0f)) }
    LaunchedEffect(Unit) {
        while (true) {
            animatedColor.animateTo(background, tween(1000, 200))
            delay(1000)
            animatedColor.animateTo(background.copy(alpha = .1f), tween(1500))
        }
    }
    Box(
        modifier = Modifier
            .padding(vertical = 30.dp)
            .height(500.dp)
            .fillMaxWidth()
            .background(animatedColor.value)
            .clip(RoundedCornerShape(10.dp))
    )

}

private fun LazyGridScope.BooksContent(
    books: List<KomeliaBook>,
    onBookClick: (KomeliaBook) -> Unit,
    onBookReadClick: (KomeliaBook, Boolean) -> Unit,
    bookMenuActions: BookMenuActions,

    selectionMode: Boolean,
    selectedBooks: List<KomeliaBook>,
    onBookSelect: (KomeliaBook) -> Unit,
    layout: BooksLayout,
) {
    if (books.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("No books", modifier = Modifier.fillMaxWidth())
        }
    } else
        when (layout) {
            GRID -> {
                BooksGrid(
                    books = books,
                    onBookClick = if (selectionMode) onBookSelect else onBookClick,
                    onBookReadClick = if (selectionMode) null else onBookReadClick,
                    bookMenuActions = if (selectionMode) null else bookMenuActions,

                    selectedBooks = selectedBooks,
                    onBookSelect = onBookSelect,
                )
            }

            LIST -> BooksList(
                books = books,
                onBookClick = if (selectionMode) onBookSelect else onBookClick,
                onBookReadClick = if (selectionMode) null else onBookReadClick,
                bookMenuActions = if (selectionMode) null else bookMenuActions,
                selectedBooks = selectedBooks,
                onBookSelect = onBookSelect,
            )
        }

}

@Composable
private fun BooksToolBar(
    series: KomgaSeries?,

    booksLayout: BooksLayout,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    booksPageSize: Int,
    onBooksPageSizeChange: (Int) -> Unit,
    selectionMode: Boolean,
    booksFilterState: BooksFilterState,

    totalBookPages: Int,
    currentBookPage: Int,
    onPageChange: (Int) -> Unit,
) {
    val width = LocalWindowWidth.current
    var showFilters by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 5.dp)
        ) {
            val booksLabel = remember(series) {
                if (series == null) null
                else buildString {
                    append(series.booksCount)
                    if (series.metadata.totalBookCount != null) append(" / ${series.metadata.totalBookCount}")
                    if (series.booksCount > 1) append(" books")
                    else append(" book")
                }
            }

            booksLabel?.let {
                SuggestionChip(
                    onClick = {},
                    label = { Text(booksLabel, style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.padding(10.dp, 0.dp)
                )
            }

            if (selectionMode) {
                Spacer(Modifier.weight(1f))
            } else {

                if (width == EXPANDED || width == FULL) {
                    ExpandableBookFiltersRow(filterState = booksFilterState)
                }
                Spacer(Modifier.weight(1f))

            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!selectionMode) {
                    if (width == COMPACT || width == MEDIUM) {
                        IconButton(onClick = { showFilters = !showFilters }, modifier = Modifier.cursorForHand()) {
                            Icon(
                                Icons.Default.FilterList,
                                null,
                                tint = if (booksFilterState.isChanged) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                            )
                        }
                    }

                    PageSizeSelectionDropdown(booksPageSize, onBooksPageSizeChange)
                }

                Box(
                    Modifier
                        .background(
                            if (booksLayout == LIST) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onBooksLayoutChange(LIST) }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ViewList,
                        null,
                    )
                }

                Box(
                    Modifier
                        .background(
                            if (booksLayout == GRID) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onBooksLayoutChange(GRID) }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.GridView,
                        null,
                    )
                }
            }
        }
        if (showFilters) {
            BookFilterDialog(
                filterState = booksFilterState,
                onDismiss = { showFilters = false }
            )
        }

        AnimatedVisibility(!selectionMode) {
            Pagination(
                totalPages = totalBookPages,
                currentPage = currentBookPage,
                onPageChange = onPageChange,
            )
        }
    }
}


@Composable
fun BooksBulkActionsToolbar(
    onCancel: () -> Unit,
    books: List<KomeliaBook>,
    actions: BookBulkActions,
    selectedBooks: List<KomeliaBook>,
    onBookSelect: (KomeliaBook) -> Unit,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedBooks.size,
        allSelected = books.size == selectedBooks.size,
        onSelectAll = {
            if (books.size == selectedBooks.size) books.forEach { onBookSelect(it) }
            else books.filter { it !in selectedBooks }.forEach { onBookSelect(it) }
        }
    ) {
        when (LocalWindowWidth.current) {
            FULL, EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    Text("Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    BooksBulkActionsContent(
                        books = selectedBooks,
                        actions = actions,
                        compact = false
                    )
                }
            }

            COMPACT, MEDIUM -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableBookFiltersRow(filterState: BooksFilterState) {
    var showFilters by remember { mutableStateOf(false) }
    val currentFilter = filterState.state.collectAsState().value
    Row(verticalAlignment = Alignment.CenterVertically) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val widthModifier = Modifier.width(200.dp)

            SortOrder(
                sortOrder = currentFilter.sortOrder,
                filterState = filterState,
                modifier = widthModifier,
                withLabel = false
            )
            ReadStatusFilter(
                readStatus = currentFilter.readStatus,
                filterState = filterState,
                modifier = widthModifier,
                withLabel = false
            )

            AnimatedVisibility(showFilters && filterState.authorsOptions.isNotEmpty()) {
                AuthorsFilter(
                    authors = currentFilter.authors,
                    filterState = filterState,
                    modifier = widthModifier,
                    withLabel = false
                )
            }

            AnimatedVisibility(showFilters && filterState.tagOptions.isNotEmpty()) {
                TagsFilter(
                    includeTags = currentFilter.includeTags,
                    excludeTags = currentFilter.excludeTags,
                    inclusionMode = currentFilter.inclusionMode,
                    exclusionMode = currentFilter.exclusionMode,
                    filterState = filterState,
                    modifier = widthModifier,
                    withLabel = false
                )
            }
        }

        if (filterState.authorsOptions.isNotEmpty() || filterState.tagOptions.isNotEmpty()) {
            IconButton(onClick = { showFilters = !showFilters }, modifier = Modifier.cursorForHand()) {
                Icon(
                    imageVector = if (showFilters) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun BookFilterDialog(
    filterState: BooksFilterState,
    onDismiss: () -> Unit,
) {
    val currentFilter = filterState.state.collectAsState().value
    AppDialog(
        modifier = Modifier.fillMaxWidth(.8f),
        content = {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SortOrder(
                    sortOrder = currentFilter.sortOrder,
                    filterState = filterState,
                    modifier = Modifier.fillMaxWidth(),
                    withLabel = true
                )
                ReadStatusFilter(
                    readStatus = currentFilter.readStatus,
                    filterState = filterState,
                    modifier = Modifier.fillMaxWidth(),
                    withLabel = true
                )

                if (filterState.authorsOptions.isNotEmpty())
                    AuthorsFilter(
                        authors = currentFilter.authors,
                        filterState = filterState,
                        modifier = Modifier.fillMaxWidth(),
                        withLabel = true
                    )

                if (filterState.tagOptions.isNotEmpty())
                    TagsFilter(
                        includeTags = currentFilter.includeTags,
                        excludeTags = currentFilter.excludeTags,
                        inclusionMode = currentFilter.inclusionMode,
                        exclusionMode = currentFilter.exclusionMode,
                        filterState = filterState,
                        modifier = Modifier.fillMaxWidth(),
                        withLabel = true
                    )

            }
        },
        header = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Book Filters", modifier = Modifier.padding(start = 10.dp))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },
        onDismissRequest = onDismiss
    )

}

@Composable
private fun SortOrder(
    sortOrder: BooksSort,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownChoice(
        selectedOption = LabeledEntry(sortOrder, strings.forBookSort(sortOrder)),
        options = BooksSort.entries.map { LabeledEntry(it, strings.forBookSort(it)) },
        onOptionChange = { filterState.onSortOrderChange(it.value) },
        label = if (withLabel) strings.sort else null,
        modifier = modifier
    )
}

@Composable
private fun ReadStatusFilter(
    readStatus: List<KomgaReadStatus>,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownMultiChoice(
        selectedOptions = readStatus.map { LabeledEntry(it, strings.forReadStatus(it)) },
        options = KomgaReadStatus.entries.map { LabeledEntry(it, strings.forReadStatus(it)) },
        onOptionSelect = { changed -> filterState.onReadStatusSelect(changed.value) },
        label = if (withLabel) strings.readStatus else null,
        placeholder = if (withLabel) null else strings.readStatus,
        modifier = modifier
    )
}

@Composable
private fun AuthorsFilter(
    authors: List<KomgaAuthor>,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownMultiChoice(
        selectedOptions = authors.map { LabeledEntry(it, it.name) },
        options = filterState.authorsOptions.map { LabeledEntry(it, it.name) },
        onOptionSelect = { changed -> filterState.onAuthorSelect(changed.value) },
        label = if (withLabel) strings.authors else null,
        placeholder = if (withLabel) null else strings.authors,
        modifier = modifier
    )
}

@Composable
private fun TagsFilter(
    includeTags: List<String>,
    excludeTags: List<String>,
    inclusionMode: TagInclusionMode,
    exclusionMode: TagExclusionMode,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    TagFiltersDropdownMenu(
        allTags = filterState.tagOptions,
        includeTags = includeTags,
        excludeTags = excludeTags,
        onTagSelect = filterState::onTagSelect,
        onReset = filterState::resetTagFilters,

        inclusionMode = inclusionMode,
        onInclusionModeChange = filterState::onInclusionModeChange,
        exclusionMode = exclusionMode,
        onExclusionModeChange = filterState::onExclusionModeChange,

        label = if (withLabel) strings.tags else null,
        placeholder = if (withLabel) null else strings.tags,
        contentPadding = PaddingValues(5.dp),
        modifier = modifier.clip(RoundedCornerShape(5.dp)),
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
        inputFieldModifier = Modifier.fillMaxWidth()
    )
}

private fun LazyGridScope.BooksGrid(
    books: List<KomeliaBook>,
    onBookClick: ((KomeliaBook) -> Unit)? = null,
    onBookReadClick: ((KomeliaBook, Boolean) -> Unit)? = null,
    bookMenuActions: BookMenuActions? = null,

    selectedBooks: List<KomeliaBook> = emptyList(),
    onBookSelect: ((KomeliaBook) -> Unit)? = null,
) {
    items(books) { book ->
        Column {
            BookImageCard(
                book = book,
                onBookClick = onBookClick?.let { { onBookClick(book) } },
                onBookReadClick = onBookReadClick?.let { { onBookReadClick(book, it) } },
                bookMenuActions = bookMenuActions,
                isSelected = selectedBooks.any { it.id == book.id },
                onSelect = onBookSelect?.let { { onBookSelect(book) } },
            )
            Spacer(Modifier.height(15.dp))
        }

    }
}

fun LazyGridScope.BooksList(
    books: List<KomeliaBook>,
    bookMenuActions: BookMenuActions? = null,
    onBookClick: ((KomeliaBook) -> Unit)? = null,
    onBookReadClick: ((KomeliaBook, Boolean) -> Unit)? = null,

    selectedBooks: List<KomeliaBook> = emptyList(),
    onBookSelect: ((KomeliaBook) -> Unit)? = null,
) {
    books.forEach { book ->
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                BookDetailedListCard(
                    book = book,
                    onClick = onBookClick?.let { { onBookClick(book) } },
                    onBookReadClick = onBookReadClick?.let { { onBookReadClick(book, it) } },
                    bookMenuActions = bookMenuActions,
                    isSelected = selectedBooks.any { it.id == book.id },
                    onSelect = onBookSelect?.let { { onBookSelect(book) } },
                )
                Spacer(Modifier.height(15.dp))
            }
        }
    }
}
