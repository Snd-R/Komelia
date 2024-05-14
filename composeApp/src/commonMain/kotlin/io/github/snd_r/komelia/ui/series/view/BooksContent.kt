package io.github.snd_r.komelia.ui.series.view

import androidx.compose.animation.Animatable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
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
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.PageSizeSelectionDropdown
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.ItemCard
import io.github.snd_r.komelia.ui.common.itemlist.BooksGrid
import io.github.snd_r.komelia.ui.common.itemlist.BooksList
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.bulk.BooksBulkActionsContent
import io.github.snd_r.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.snd_r.komelia.ui.common.menus.bulk.BulkActionsContainer
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.series.SeriesBooksState
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.series.KomgaSeries
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
            WindowWidth.COMPACT -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    }

    var scrollToPosition by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .onGloballyPositioned {
                scrollToPosition = it.positionInParent().y
            },
        horizontalAlignment = alignment
    ) {
        BooksToolBar(
            series = series.value,
            booksLayout = layout,
            onBooksLayoutChange = booksState::onBookLayoutChange,
            selectionMode = booksState.booksSelectionMode,
            isLoading = isLoading,
            booksPageSize = booksState.booksPageSize.collectAsState().value,
            onBooksPageSizeChange = booksState::onBookPageSizeChange,
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
            booksPageSize = booksState.booksPageSize.collectAsState().value,
            totalBookPages = booksState.totalBookPages,
            currentBookPage = booksState.currentBookPage,
            onPageNumberChange = {
                coroutineScope.launch {
                    booksState.onPageChange(it)
                    scrollState.animateScrollTo(scrollToPosition.roundToInt())
                }
            },
            bookCardWidth = booksState.cardWidth.collectAsState().value,
            scrollState = scrollState,
        )
    }

    if ((width == WindowWidth.COMPACT || width == WindowWidth.MEDIUM) && booksState.selectedBooks.isNotEmpty()) {
        BottomPopupBulkActionsPanel { BooksBulkActionsContent(booksState.books, false) }
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

    booksPageSize: Int,
    totalBookPages: Int,
    currentBookPage: Int,
    onPageNumberChange: (Int) -> Unit,
    bookCardWidth: Dp,
    scrollState: ScrollState
) {
    var contentSize by remember { mutableStateOf(400) }
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(500),
//        modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessVeryLow))
        modifier = Modifier.animateContentSize(animationSpec = tween(500))
    ) { loading ->
        if (loading) {
            val background = MaterialTheme.colorScheme.surfaceVariant
            val animatedColor = remember { Animatable(background.copy(alpha = .0f)) }
            LaunchedEffect(Unit) {
                while (true) {
                    animatedColor.animateTo(background, tween(1000, 200))
                    animatedColor.animateTo(background.copy(alpha = .1f), tween(1500, 1000))
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
            Column(Modifier.onSizeChanged { contentSize = it.height }) {
                when (layout) {
                    BooksLayout.GRID -> {
                        BooksGrid(
                            books = books,
                            onBookClick = if (selectionMode) onBookSelect else onBookClick,
                            onBookReadClick = if (selectionMode) null else onBookReadClick,
                            bookMenuActions = if (selectionMode) null else bookMenuActions,

                            selectedBooks = selectedBooks,
                            onBookSelect = onBookSelect,

                            loadPlaceholder = {
                                for (i in 0 until booksPageSize) {
                                    ItemCard(Modifier.width(bookCardWidth), onClick = {}, image = {})
                                }
                            },
                            cardWidth = bookCardWidth,
                            isLoading = false,
                        )
                    }

                    BooksLayout.LIST -> BooksList(
                        books = books,
                        onBookClick = if (selectionMode) onBookSelect else onBookClick,
                        onBookReadClick = if (selectionMode) null else onBookReadClick,
                        bookMenuActions = if (selectionMode) null else bookMenuActions,
                        isLoading = false,
                        selectedBooks = selectedBooks,
                        onBookSelect = onBookSelect,
                    )
                }

                if (!selectionMode && scrollState.maxValue > 0) {
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
    isLoading: Boolean,

    totalBookPages: Int,
    currentBookPage: Int,
    onPageChange: (Int) -> Unit,
) {
    val width = LocalWindowWidth.current
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
                when (width) {
                    WindowWidth.EXPANDED, WindowWidth.FULL -> Pagination(
                        totalPages = totalBookPages,
                        currentPage = currentBookPage,
                        onPageChange = onPageChange,
                        modifier = Modifier.weight(1f)
                    )

                    else -> {
                        Spacer(Modifier.weight(1f))
                    }
                }

                PageSizeSelectionDropdown(booksPageSize, onBooksPageSizeChange)
            }

            Row {
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

//        var showFilters by remember { mutableStateOf(false) }
//        AnimatedVisibility(showFilters) {
//
//        }

        when (width) {
            WindowWidth.COMPACT, WindowWidth.MEDIUM -> Pagination(
                totalPages = totalBookPages,
                currentPage = currentBookPage,
                onPageChange = onPageChange,
            )

            else -> {}
        }
    }
}


@Composable
fun BooksBulkActionsToolbar(
    onCancel: () -> Unit,
    books: List<KomgaBook>,
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
            WindowWidth.FULL -> {
                Text("Selection mode: Click on items to select or deselect them")
                if (selectedBooks.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    BooksBulkActionsContent(selectedBooks, true)
                }
            }

            WindowWidth.EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    Text("Selection mode: Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    BooksBulkActionsContent(selectedBooks, true)
                }
            }

            WindowWidth.COMPACT, WindowWidth.MEDIUM -> {}
        }
    }
}
