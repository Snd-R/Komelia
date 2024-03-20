package io.github.snd_r.komelia.ui.common.itemlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.cards.ReadListImageCard
import io.github.snd_r.komelia.ui.platform.VerticalScrollbar
import io.github.snd_r.komga.readlist.KomgaReadList
import io.github.snd_r.komga.readlist.KomgaReadListId

@Composable
fun ReadListLazyCardGrid(
    readLists: List<KomgaReadList>,
    onReadListClick: (KomgaReadListId) -> Unit,
    onReadListDelete: (KomgaReadListId) -> Unit,
    minSize: Dp = 200.dp,
    scrollState: LazyGridState = rememberLazyGridState(),
) {
    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            items(readLists) {
                ReadListImageCard(
                    readLists = it,
                    onCollectionClick = { onReadListClick(it.id) },
                    onCollectionDelete = { onReadListDelete(it.id) },
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                )
            }
        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}
