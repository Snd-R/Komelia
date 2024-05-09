package io.github.snd_r.komelia.ui.common.itemlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.BookDetailedListCard
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.cards.DraggableImageCard
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyGridState


@Composable
fun BookCardSlider(
    books: List<KomgaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,
    cardWidth: Dp = 250.dp,
    scrollState: LazyListState = rememberLazyListState(),
) {
    Column {
        LazyRow(state = scrollState) {
            items(books) { book ->

                BookImageCard(
                    book = book,
                    onBookClick = { onBookClick(book.id) },
                    bookMenuActions = bookMenuActions,
                    onBookReadClick = { onBookReadClick(book.id) },
                    modifier = Modifier
                        .width(cardWidth)
//                        .height(cardWidth)
                        .padding(5.dp),
                )

            }
        }
        HorizontalScrollbar(
            scrollState,
            Modifier.align(Alignment.End).height(10.dp),
        )
    }
}


@Composable
fun BookLazyCardGrid(
    books: List<KomgaBook>,
    onBookClick: ((KomgaBook) -> Unit)?,
    onBookReadClick: ((KomgaBook) -> Unit)?,
    bookMenuActions: BookMenuActions?,


    selectedBooks: List<KomgaBook> = emptyList(),
    onBookSelect: ((KomgaBook) -> Unit)? = null,

    reorderable: Boolean = false,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,

    minSize: Dp = 200.dp,
    gridState: LazyGridState = rememberLazyGridState(),
) {

    val coroutineScope = rememberCoroutineScope()
    val reorderableLazyGridState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        onMove = { from, to -> onReorder(from.index, to.index) }
    )
    LaunchedEffect(reorderableLazyGridState.isAnyItemDragging) {
        onReorderDragStateChange(reorderableLazyGridState.isAnyItemDragging)
    }

    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {

            items(books, key = { it.id.value }) { book ->
                val isSelected = remember(selectedBooks) { selectedBooks.any { it.id == book.id } }
                DraggableImageCard(
                    key = book.id.value,
                    dragEnabled = reorderable,
                    reorderableState = reorderableLazyGridState
                ) {
                    BookImageCard(
                        book = book,
                        onBookClick = onBookClick?.let { { onBookClick(book) } },
                        bookMenuActions = bookMenuActions,
                        onBookReadClick = onBookReadClick?.let { { onBookReadClick(book) } },
                        isSelected = isSelected,
                        onSelect = onBookSelect?.let { { onBookSelect(book) } },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp),
                    )

                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Pagination(
                    totalPages = totalPages,
                    currentPage = currentPage,
                    onPageChange = {
                        coroutineScope.launch {
                            onPageChange(it)
                            gridState.scrollToItem(0)
                        }
                    }
                )
            }
        }
        VerticalScrollbar(gridState, Modifier.align(Alignment.TopEnd))
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BooksGrid(
    books: List<KomgaBook>,
    cardWidth: Dp,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,

    loadPlaceholder: @Composable () -> Unit,
    isLoading: Boolean,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 30.dp)
    ) {
        if (isLoading) {
            loadPlaceholder()
        } else {
            books.forEach {
                BookImageCard(
                    book = it,
                    onBookClick = { onBookClick(it.id) },
                    onBookReadClick = { onBookReadClick(it.id) },
                    bookMenuActions = bookMenuActions,
                    modifier = Modifier.width(cardWidth)
                )

            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyGridItemScope.DraggableBookCard(
    book: KomgaBook,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,

    isSelected: Boolean,
    onBookSelect: ((KomgaBook) -> Unit)?,
    reorderableState: ReorderableLazyGridState
) {
    val platform = LocalPlatform.current
    ReorderableItem(reorderableState, key = book.id.value) {
        if (platform == PlatformType.MOBILE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BookImageCard(
                    book = book,
                    onBookClick = { onBookClick(book.id) },
                    onBookReadClick = { onBookReadClick(book.id) },
                    bookMenuActions = bookMenuActions,
                    isSelected = isSelected,
                    onSelect = onBookSelect?.let { { onBookSelect(book) } },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                )

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .draggableHandle()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.DragHandle, null) }
            }
        } else {
            BookImageCard(
                book = book,
                onBookClick = { onBookClick(book.id) },
                onBookReadClick = { onBookReadClick(book.id) },
                bookMenuActions = bookMenuActions,
                isSelected = isSelected,
                onSelect = onBookSelect?.let { { onBookSelect(book) } },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            )
        }
    }
}


@Composable
fun BooksList(
    books: List<KomgaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,

    isLoading: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start,
    ) {

        if (!isLoading) {
            books.forEach { book ->
                BookDetailedListCard(
                    book = book,
                    onClick = { onBookClick(book.id) },
                    bookMenuActions = bookMenuActions,
                    onBookReadClick = { onBookReadClick(book.id) },
                )
            }
        }
    }
}
