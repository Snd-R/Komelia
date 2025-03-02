package io.github.snd_r.komelia.ui.common.menus.bulk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import io.github.snd_r.komelia.platform.cursorForHand

@Composable
fun BulkActionsContainer(
    onCancel: () -> Unit,
    selectedCount: Int,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = .3f))
    ) {
        IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null) }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .clickable { onSelectAll() }
                .cursorForHand()
                .padding(end = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = allSelected,
                onClick = { onSelectAll() }
            )
            Text("Select All")
        }
        Text("$selectedCount selected", modifier = Modifier.width(110.dp).padding(start = 10.dp))

        content()
    }
}

@Composable
fun BottomPopupBulkActionsPanel(content: @Composable RowScope.() -> Unit) {
    Popup(popupPositionProvider = BottomScreenPopupPositionProvider) {
        Surface {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = .3f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp)
            ) {
                content()
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkActionButton(
    icon: ImageVector,
    description: String,
    iconOnly: Boolean,
    onClick: () -> Unit,
) {

    if (iconOnly) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
            tooltip = {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(description)
                }
            },
            state = rememberTooltipState()
        ) {
            IconButton(onClick) { Icon(icon, null) }
        }
    } else {
        Column(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .clickable { onClick() }
                .cursorForHand(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null)
            Text(description, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}


object BottomScreenPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ) = IntOffset(0, windowSize.height)
}
