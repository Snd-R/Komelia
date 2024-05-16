package io.github.snd_r.komelia.ui.series.view

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.WindowWidth.*
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.FilterDropdownChoice
import io.github.snd_r.komelia.ui.common.FilterDropdownMultiChoice
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.PageSizeSelectionDropdown
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.TagFiltersDropdownMenu
import io.github.snd_r.komelia.ui.common.itemlist.BooksGrid
import io.github.snd_r.komelia.ui.common.itemlist.BooksList
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.bulk.BookBulkActions
import io.github.snd_r.komelia.ui.common.menus.bulk.BooksBulkActionsContent
import io.github.snd_r.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.snd_r.komelia.ui.common.menus.bulk.BulkActionsContainer
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.series.SeriesBooksState
import io.github.snd_r.komelia.ui.series.SeriesBooksState.BooksFilterState
import io.github.snd_r.komelia.ui.series.SeriesBooksState.BooksFilterState.BooksSort
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaReadStatus
import io.github.snd_r.komga.series.KomgaSeries
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SeriesBooksContent(
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook) -> Unit,
    booksState: SeriesBooksState,
    scrollState: ScrollState,
) {
    val coroutineScope = rememberCoroutineScope()
    val series = booksState.series.collectAsState()
    val isLoading = when (booksState.state.collectAsState().value) {
        Uninitialized, Loading -> true
        else -> false
    }
    val layout = booksState.booksLayout.collectAsState().value
    val width = LocalWindowWidth.current
    val alignment = remember(width) {
        when (width) {
            COMPACT -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    }

    var scrollToPosition by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .onGloballyPositioned { scrollToPosition = it.positionInParent().y },
        horizontalAlignment = alignment
    ) {
        BooksToolBar(
            series = series.value,

            booksLayout = layout,
            onBooksLayoutChange = booksState::onBookLayoutChange,
            booksPageSize = booksState.booksPageSize.collectAsState().value,
            onBooksPageSizeChange = booksState::onBookPageSizeChange,
            selectionMode = booksState.booksSelectionMode,
            booksFilterState = booksState.filterState,

            totalBookPages = booksState.totalBookPages,
            currentBookPage = booksState.currentBookPage,
            onPageChange = { coroutineScope.launch { booksState.onPageChange(it) } }
        )
        BooksContent(
            books = booksState.books,
            isLoading = isLoading,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            bookMenuActions = booksState.bookMenuActions(),
            selectionMode = booksState.booksSelectionMode,
            selectedBooks = booksState.selectedBooks,
            onBookSelect = booksState::onBookSelect,
            layout = booksState.booksLayout.collectAsState().value,
            totalBookPages = booksState.totalBookPages,
            currentBookPage = booksState.currentBookPage,
            onPageNumberChange = {
                coroutineScope.launch {
                    booksState.onPageChange(it)
                    scrollState.animateScrollTo(scrollToPosition.roundToInt())
                }
            },
            bookCardWidth = booksState.cardWidth.collectAsState().value,
        )
    }

    if ((width == COMPACT || width == MEDIUM) && booksState.selectedBooks.isNotEmpty()) {
        BottomPopupBulkActionsPanel {
            BooksBulkActionsContent(
                books = booksState.selectedBooks,
                actions = booksState.bookBulkMenuActions(),
                iconOnly = false
            )
        }
    }
}

@Composable
private fun BooksContent(
    books: List<KomgaBook>,
    isLoading: Boolean,
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook) -> Unit,
    bookMenuActions: BookMenuActions,

    selectionMode: Boolean,
    selectedBooks: List<KomgaBook>,
    onBookSelect: (KomgaBook) -> Unit,
    layout: BooksLayout,

    totalBookPages: Int,
    currentBookPage: Int,
    onPageNumberChange: (Int) -> Unit,
    bookCardWidth: Dp,
) {
    var contentSize by remember { mutableStateOf(400) }
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(500),
        modifier = Modifier.animateContentSize(animationSpec = tween(500))
    ) { loading ->
        if (loading) {
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
                    .padding(bottom = 30.dp)
                    .height(contentSize.dp)
                    .fillMaxWidth()
                    .background(animatedColor.value)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else
            if (books.isEmpty()) {
                Text("No books", modifier = Modifier.fillMaxWidth())
            } else

                Column(
                    modifier = Modifier.onSizeChanged { contentSize = it.height },
                    horizontalAlignment = if (LocalWindowWidth.current == COMPACT) Alignment.CenterHorizontally else Alignment.Start
                ) {
                    when (layout) {
                        BooksLayout.GRID -> {
                            BooksGrid(
                                books = books,
                                onBookClick = if (selectionMode) onBookSelect else onBookClick,
                                onBookReadClick = if (selectionMode) null else onBookReadClick,
                                bookMenuActions = if (selectionMode) null else bookMenuActions,

                                selectedBooks = selectedBooks,
                                onBookSelect = onBookSelect,
                                cardWidth = bookCardWidth,
                            )
                        }

                        BooksLayout.LIST -> BooksList(
                            books = books,
                            onBookClick = if (selectionMode) onBookSelect else onBookClick,
                            onBookReadClick = if (selectionMode) null else onBookReadClick,
                            bookMenuActions = if (selectionMode) null else bookMenuActions,
                            selectedBooks = selectedBooks,
                            onBookSelect = onBookSelect,
                        )
                    }
                    if (!selectionMode && contentSize > 700) {
                        Pagination(
                            totalPages = totalBookPages,
                            currentPage = currentBookPage,
                            onPageChange = onPageNumberChange,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
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
                        .background(if (booksLayout == BooksLayout.LIST) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable { onBooksLayoutChange(BooksLayout.LIST) }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ViewList, null)
                }

                Box(
                    Modifier
                        .background(if (booksLayout == BooksLayout.GRID) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable { onBooksLayoutChange(BooksLayout.GRID) }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Icon(Icons.Default.GridView, null)
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
    books: List<KomgaBook>,
    actions: BookBulkActions,
    selectedBooks: List<KomgaBook>,
    onBookSelect: (KomgaBook) -> Unit,
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
            FULL -> {
                Text("Selection mode: Click on items to select or deselect them")
                if (selectedBooks.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    BooksBulkActionsContent(
                        books = selectedBooks,
                        actions= actions,
                        iconOnly = true
                    )
                }
            }

            EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    Text("Selection mode: Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    BooksBulkActionsContent(
                        books = selectedBooks,
                        actions = actions,
                        iconOnly = true
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val widthModifier = Modifier.width(200.dp)

            SortOrder(filterState, widthModifier, false)
            ReadStatusFilter(filterState, widthModifier, false)

            AnimatedVisibility(showFilters && filterState.authorsOptions.isNotEmpty()) {
                AuthorsFilter(filterState, widthModifier, false)
            }

            AnimatedVisibility(showFilters && filterState.tagOptions.isNotEmpty()) {
                TagsFilter(filterState, widthModifier, false)
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
    AppDialog(
        modifier = Modifier.fillMaxWidth(.8f),
        content = {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SortOrder(filterState, Modifier.fillMaxWidth(), true)
                ReadStatusFilter(filterState, Modifier.fillMaxWidth(), true)

                if (filterState.authorsOptions.isNotEmpty())
                    AuthorsFilter(filterState, Modifier.fillMaxWidth(), true)

                if (filterState.tagOptions.isNotEmpty())
                    TagsFilter(filterState, Modifier.fillMaxWidth(), true)

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
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownChoice(
        selectedOption = LabeledEntry(filterState.sortOrder, strings.forBookSort(filterState.sortOrder)),
        options = BooksSort.entries.map { LabeledEntry(it, strings.forBookSort(it)) },
        onOptionChange = { filterState.onSortOrderChange(it.value) },
        label = if (withLabel) strings.sort else null,
        modifier = modifier
    )
}

@Composable
private fun ReadStatusFilter(
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownMultiChoice(
        selectedOptions = filterState.readStatus.map { LabeledEntry(it, strings.forReadStatus(it)) },
        options = KomgaReadStatus.entries.map { LabeledEntry(it, strings.forReadStatus(it)) },
        onOptionSelect = { changed -> filterState.onReadStatusSelect(changed.value) },
        label = if (withLabel) strings.readStatus else null,
        placeholder = if (withLabel) null else strings.readStatus,
        modifier = modifier
    )
}

@Composable
private fun AuthorsFilter(
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownMultiChoice(
        selectedOptions = filterState.authors.map { LabeledEntry(it, it.name) },
        options = filterState.authorsOptions.map { LabeledEntry(it, it.name) },
        onOptionSelect = { changed -> filterState.onAuthorSelect(changed.value) },
        label = if (withLabel) strings.authors else null,
        placeholder = if (withLabel) null else strings.authors,
        modifier = modifier
    )
}

@Composable
private fun TagsFilter(
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    TagFiltersDropdownMenu(
        selectedTags = filterState.tags,
        tagOptions = filterState.tagOptions,
        onTagSelect = filterState::onTagSelect,
        onReset = filterState::resetTagFilters,

        label = if (withLabel) strings.tags else null,
        placeholder = if (withLabel) null else strings.tags,
        contentPadding = PaddingValues(5.dp),
        modifier = modifier.clip(RoundedCornerShape(5.dp)),
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
        inputFieldModifier = Modifier.fillMaxWidth()
    )
}
