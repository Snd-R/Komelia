package io.github.snd_r.komelia.platform

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VerticalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
)

@Composable
expect fun VerticalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier = Modifier,
)

@Composable
expect fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
)

@Composable
expect fun HorizontalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
)

@Composable
expect fun HorizontalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier = Modifier,
)

@Composable
expect fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
)
