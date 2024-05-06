package io.github.snd_r.komelia.ui.common.itemlist

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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.HorizontalScrollbar
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.cards.BookDetailedListCard
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId


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
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,
    minSize: Dp = 200.dp,
    scrollState: LazyGridState = rememberLazyGridState(),
) {
    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            items(books) {
                BookImageCard(
                    book = it,
                    onBookClick = { onBookClick(it.id) },
                    bookMenuActions = bookMenuActions,
                    onBookReadClick = { onBookReadClick(it.id) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                )
            }
        }
        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
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
