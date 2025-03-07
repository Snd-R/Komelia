package io.github.snd_r.komelia.ui.common.itemlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.cards.DraggableImageCard
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import kotlinx.coroutines.launch
import sh.calvin.reorderable.rememberReorderableLazyGridState
import snd.komga.client.book.KomgaBook

@Composable
fun BookLazyCardGrid(
    books: List<KomgaBook>,
    onBookClick: ((KomgaBook) -> Unit)?,
    onBookReadClick: ((KomgaBook, Boolean) -> Unit)?,
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
                        onBookReadClick = onBookReadClick?.let { { onBookReadClick(book, it) } },
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
