package io.github.snd_r.komelia.platform

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier,
) = Unit

@Composable
actual fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
) = Unit


@Composable
actual fun VerticalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier,
) = Unit

@Composable
actual fun HorizontalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier,
) = Unit

@Composable
actual fun HorizontalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier,
) = Unit

@Composable
actual fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
) = Unit

@Composable
actual fun VerticalScrollbarWithFullSpans(
    scrollState: LazyGridState,
    modifier: Modifier,
    fullSpanLines: Int
) = Unit