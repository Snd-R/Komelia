package snd.komelia.ui.platform

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

@Composable
actual fun VerticalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier,
) = CustomVerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
        hoverColor = MaterialTheme.colorScheme.secondary,
    ),
)

@Composable
actual fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
) = CustomVerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
        hoverColor = MaterialTheme.colorScheme.secondary,
    ),
)

@Composable
actual fun HorizontalScrollbar(
    scrollState: LazyListState,
    modifier: Modifier,
) = HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
        hoverColor = MaterialTheme.colorScheme.secondary,
    ),
)


@Composable
actual fun HorizontalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier,
) = HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
        hoverColor = MaterialTheme.colorScheme.secondary,
    ),
)

@Composable
actual fun HorizontalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier,
) = HorizontalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
        hoverColor = MaterialTheme.colorScheme.secondary,
    ),
)

@Composable
actual fun VerticalScrollbar(
    scrollState: LazyGridState,
    modifier: Modifier,
) = CustomVerticalScrollbar(
    adapter = rememberScrollbarAdapter(scrollState),
    modifier = modifier,
    style = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
        hoverColor = MaterialTheme.colorScheme.secondary,
    ),
)

@Composable
private fun CustomVerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    style: ScrollbarStyle = LocalScrollbarStyle.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val isDragged = interactionSource.collectIsDraggedAsState().value
    val scaleModifier = if (isHovered || isDragged) Modifier.scale(1.5f, 1f) else Modifier

    VerticalScrollbar(
        adapter = adapter,
        modifier = modifier.then(scaleModifier),
        reverseLayout = reverseLayout,
        style = style,
        interactionSource = interactionSource
    )
}

@Composable
actual fun VerticalScrollbarWithFullSpans(
    scrollState: LazyGridState,
    modifier: Modifier,
    fullSpanLines: Int
) = VerticalScrollbar(scrollState, modifier)