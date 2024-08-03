package io.github.snd_r.komelia.platform

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
actual fun VerticalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier,
) = androidx.compose.foundation.VerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
        hoverColor = MaterialTheme.colorScheme.background,
    ),
)

@Composable
actual fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
) = androidx.compose.foundation.VerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
        hoverColor = MaterialTheme.colorScheme.background,
    ),
)

@Composable
actual fun HorizontalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier,
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
        hoverColor = MaterialTheme.colorScheme.background,
    ),
)


@Composable
actual fun HorizontalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier,
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
        hoverColor = MaterialTheme.colorScheme.background,
    ),
)

@Composable
actual fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
        hoverColor = MaterialTheme.colorScheme.background,
    ),
)

@Composable
actual fun VerticalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier,
) = androidx.compose.foundation.VerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
        hoverColor = MaterialTheme.colorScheme.background,
    ),
)
