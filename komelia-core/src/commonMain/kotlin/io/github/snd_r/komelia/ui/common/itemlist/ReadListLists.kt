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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.Pagination
import io.github.snd_r.komelia.ui.common.cards.ReadListImageCard
import kotlinx.coroutines.launch
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId

@Composable
fun ReadListLazyCardGrid(
    readLists: List<KomgaReadList>,
    onReadListClick: (KomgaReadListId) -> Unit,
    onReadListDelete: (KomgaReadListId) -> Unit,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    minSize: Dp = 200.dp,
    scrollState: LazyGridState = rememberLazyGridState(),
) {
    val coroutineScope = rememberCoroutineScope()
    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            item(
                span = { GridItemSpan(maxLineSpan) },
            ) {
                if (scrollState.canScrollForward || scrollState.canScrollBackward)
                    Pagination(
                        totalPages = totalPages,
                        currentPage = currentPage,
                        onPageChange = onPageChange
                    )
            }

            items(readLists) {
                ReadListImageCard(
                    readLists = it,
                    onCollectionClick = { onReadListClick(it.id) },
                    onCollectionDelete = { onReadListDelete(it.id) },
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                )
            }
            item(
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Pagination(
                    totalPages = totalPages,
                    currentPage = currentPage,
                    onPageChange = {
                        coroutineScope.launch {
                            onPageChange(it)
                            scrollState.scrollToItem(0)
                        }
                    }
                )
            }

        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}
