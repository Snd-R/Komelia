package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.OutlinedText
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState

const val defaultCardWidth = 240

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    image: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
        modifier = modifier
            .combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick)
            .then(if (onClick != null || onLongClick != null) Modifier.cursorForHand() else Modifier),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Box(modifier = Modifier.aspectRatio(0.703f)) { image() }
        content()
    }
}

@Composable
fun ItemCardWithContent(
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.aspectRatio(0.703f)) { image() }
        content()
    }
}

@Composable
fun CardGradientOverlay() {
    val colorStops = arrayOf(
        0.0f to Color.Black.copy(alpha = .5f),
        0.05f to Color.Transparent,
        0.6f to Color.Transparent,
        0.90f to Color.Black.copy(alpha = .8f),
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colorStops = colorStops))
    )
}

@Composable
fun overlayBorderModifier() =
    Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.tertiary), RoundedCornerShape(5.dp))


@Composable
fun CardOutlinedText(
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
    outlineDrawStyle: Stroke = Stroke(4f),
) {
    OutlinedText(
        text = text,
        maxLines = maxLines,
        outlineColor = Color.Black,
        style = style,
        overflow = TextOverflow.Ellipsis,
        outlineDrawStyle = outlineDrawStyle,
    )
}

@Composable
fun SelectionRadioButton(
    isSelected: Boolean,
    onSelect: () -> Unit,
) {

    RadioButton(
        selected = isSelected,
        onClick = onSelect,
        colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.tertiary,
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(topEnd = 17.dp, bottomEnd = 17.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = .4f))
            .selectable(selected = isSelected, onClick = onSelect)
    )
}

@Composable
fun LazyGridItemScope.DraggableImageCard(
    key: String,
    dragEnabled: Boolean,
    reorderableState: ReorderableLazyGridState,
    content: @Composable () -> Unit
) {
    val platform = LocalPlatform.current
    if (dragEnabled) {
        ReorderableItem(reorderableState, key = key) {
            if (platform == PlatformType.MOBILE) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    content()
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
                Box(Modifier.draggableHandle()) { content() }
            }

        }
    } else content()
}