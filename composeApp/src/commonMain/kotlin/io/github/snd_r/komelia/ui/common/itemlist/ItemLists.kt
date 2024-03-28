package io.github.snd_r.komelia.ui.common.itemlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.ui.common.cards.ItemCard

@Composable
fun PlaceHolderLazyCardGrid(
    elements: Int,
    minSize: Dp = 200.dp,
    scrollState: LazyGridState = rememberLazyGridState(),
) {
    Box {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            for (i in 0 until elements) {
                item { ItemCard(modifier = Modifier.padding(5.dp), onClick = {}) {} }
            }
        }
        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }

}
